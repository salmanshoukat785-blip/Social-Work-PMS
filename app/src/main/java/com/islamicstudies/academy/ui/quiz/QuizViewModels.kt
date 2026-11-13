package com.islamicstudies.academy.ui.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.islamicstudies.academy.AcademyApp
import com.islamicstudies.academy.data.db.ChapterSummary
import com.islamicstudies.academy.data.db.QuestionEntity
import com.islamicstudies.academy.data.db.QuizAttemptEntity
import com.islamicstudies.academy.data.settings.AppSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

private const val PER_QUESTION_SECONDS = 30
private const val MOCK_SECONDS_PER_QUESTION = 45

/** Reads the options of one language out of the stored JSON array. */
fun QuestionEntity.optionsFor(lang: String): List<String> {
    val json = if (lang == "ur") optionsUrJson else optionsEnJson
    return try {
        val arr = JSONArray(json)
        val out = ArrayList<String>()
        for (i in 0 until arr.length()) out.add(arr.getString(i))
        out
    } catch (e: Exception) {
        emptyList()
    }
}

fun formatSeconds(totalSeconds: Int): String {
    val safe = if (totalSeconds < 0) 0 else totalSeconds
    return String.format("%d:%02d", safe / 60, safe % 60)
}

// ---------------------------------------------------------------- quiz home

class QuizHomeViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as AcademyApp
    private val keepAlive = SharingStarted.WhileSubscribed(5000)

    val chapters: StateFlow<List<ChapterSummary>> = app.database.contentDao().observeChapterSummaries()
        .stateIn(viewModelScope, keepAlive, emptyList())

    val attempts: StateFlow<List<QuizAttemptEntity>> = app.database.quizDao().observeAttempts()
        .stateIn(viewModelScope, keepAlive, emptyList())

    val questionCount: StateFlow<Int> = app.database.quizDao().observeQuestionCount()
        .stateIn(viewModelScope, keepAlive, 0)
}

// ---------------------------------------------------------------- playing a quiz

data class QuizUi(
    val loading: Boolean = true,
    val mode: String = "practice",
    val questions: List<QuestionEntity> = emptyList(),
    val index: Int = 0,
    val selected: Int = -1,
    val revealed: Boolean = false,
    val secondsLeft: Int = 0,
    val finishedAttemptId: Long = -1L
)

class QuizPlayViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val app = application as AcademyApp
    private val quizDao = app.database.quizDao()

    private val mode: String = savedStateHandle.get<String>("mode") ?: "practice"
    private val chapterId: String = savedStateHandle.get<String>("chapter") ?: "all"
    private val difficulty: String = savedStateHandle.get<String>("difficulty") ?: "all"
    private val requestedCount: Int = savedStateHandle.get<Int>("count") ?: 10

    // practice and mistakes show the right answer straight after each choice
    private val immediateFeedback = mode == "practice" || mode == "mistakes"

    private val _ui = MutableStateFlow(QuizUi(mode = mode))
    val ui: StateFlow<QuizUi> = _ui.asStateFlow()

    val settings: StateFlow<AppSettings> = app.settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private var answers = IntArray(0)
    private var startedAt = System.currentTimeMillis()
    private var timerJob: Job? = null
    private var finishing = false

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        var pool: List<QuestionEntity> = when (mode) {
            "mistakes" -> {
                val ids = wrongQuestionIds()
                if (ids.isEmpty()) emptyList() else quizDao.getQuestionsByIds(ids)
            }
            "mock" -> quizDao.getQuestions("all", "all")
            else -> quizDao.getQuestions(chapterId, difficulty)
        }
        pool = pool.shuffled()
        if (requestedCount > 0 && pool.size > requestedCount) {
            pool = pool.take(requestedCount)
        }
        answers = IntArray(pool.size) { -1 }
        startedAt = System.currentTimeMillis()
        _ui.update { it.copy(loading = false, questions = pool) }

        if (pool.isNotEmpty()) {
            if (mode == "timed") {
                startTimer(PER_QUESTION_SECONDS) { advance() }
            } else if (mode == "mock") {
                startTimer(pool.size * MOCK_SECONDS_PER_QUESTION) { finishQuiz() }
            }
        }
    }

    /** Questions whose most recent answer (over all attempts) was wrong or missing. */
    private suspend fun wrongQuestionIds(): List<String> {
        val latest = LinkedHashMap<String, Boolean>()
        for (attempt in quizDao.getAttemptsChronological()) {
            val arr = try {
                JSONArray(attempt.detailsJson)
            } catch (e: Exception) {
                continue
            }
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                latest[o.getString("q")] = o.getInt("a") == o.getInt("c")
            }
        }
        val wrong = ArrayList<String>()
        for (entry in latest.entries) {
            if (!entry.value) wrong.add(entry.key)
        }
        return wrong
    }

    private fun startTimer(seconds: Int, onExpire: () -> Unit) {
        timerJob?.cancel()
        _ui.update { it.copy(secondsLeft = seconds) }
        timerJob = viewModelScope.launch {
            var left = seconds
            while (left > 0) {
                delay(1000)
                left -= 1
                _ui.update { it.copy(secondsLeft = left) }
            }
            onExpire()
        }
    }

    fun select(option: Int) {
        val s = _ui.value
        if (s.loading || s.revealed || s.finishedAttemptId >= 0 || s.questions.isEmpty()) return
        if (immediateFeedback) {
            answers[s.index] = option
            _ui.update { it.copy(selected = option, revealed = true) }
        } else {
            _ui.update { it.copy(selected = option) }
        }
    }

    fun next() {
        advance()
    }

    private fun recordCurrent() {
        val s = _ui.value
        if (!immediateFeedback && s.index < answers.size) {
            answers[s.index] = s.selected
        }
    }

    private fun advance() {
        val s = _ui.value
        if (s.questions.isEmpty() || finishing) return
        recordCurrent()
        if (s.index + 1 < s.questions.size) {
            _ui.update { it.copy(index = it.index + 1, selected = -1, revealed = false) }
            if (mode == "timed") {
                startTimer(PER_QUESTION_SECONDS) { advance() }
            }
        } else {
            finishQuiz()
        }
    }

    private fun finishQuiz() {
        val s = _ui.value
        if (finishing || s.questions.isEmpty()) return
        finishing = true
        timerJob?.cancel()
        recordCurrent()

        val qs = s.questions
        var correct = 0
        val details = JSONArray()
        for (i in qs.indices) {
            if (answers[i] == qs[i].correctIndex) correct += 1
            details.put(
                JSONObject()
                    .put("q", qs[i].id)
                    .put("a", answers[i])
                    .put("c", qs[i].correctIndex)
            )
        }
        val duration = ((System.currentTimeMillis() - startedAt) / 1000).toInt()
        viewModelScope.launch {
            val id = quizDao.insertAttempt(
                QuizAttemptEntity(
                    startedAt = startedAt,
                    mode = mode,
                    totalQuestions = qs.size,
                    correctCount = correct,
                    durationSec = duration,
                    detailsJson = details.toString()
                )
            )
            app.settingsRepository.recordStudyDay()
            _ui.update { it.copy(finishedAttemptId = id) }
        }
    }
}

// ---------------------------------------------------------------- result / review

data class ReviewItem(val question: QuestionEntity, val selected: Int)

data class ResultUi(
    val loading: Boolean = true,
    val attempt: QuizAttemptEntity? = null,
    val items: List<ReviewItem> = emptyList()
)

class QuizResultViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val app = application as AcademyApp
    private val quizDao = app.database.quizDao()
    private val attemptId: Long = savedStateHandle.get<Long>("attemptId") ?: -1L

    private val _ui = MutableStateFlow(ResultUi())
    val ui: StateFlow<ResultUi> = _ui.asStateFlow()

    val settings: StateFlow<AppSettings> = app.settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    init {
        viewModelScope.launch {
            val attempt = quizDao.getAttempt(attemptId)
            if (attempt == null) {
                _ui.value = ResultUi(loading = false)
            } else {
                val ids = ArrayList<String>()
                val picked = ArrayList<Int>()
                try {
                    val arr = JSONArray(attempt.detailsJson)
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        ids.add(o.getString("q"))
                        picked.add(o.getInt("a"))
                    }
                } catch (e: Exception) {
                    // damaged details: show the score only
                }
                val byId = HashMap<String, QuestionEntity>()
                if (ids.isNotEmpty()) {
                    for (q in quizDao.getQuestionsByIds(ids)) byId[q.id] = q
                }
                val items = ArrayList<ReviewItem>()
                for (i in ids.indices) {
                    val q = byId[ids[i]]
                    if (q != null) items.add(ReviewItem(q, picked[i]))
                }
                _ui.value = ResultUi(loading = false, attempt = attempt, items = items)
            }
        }
    }
}
