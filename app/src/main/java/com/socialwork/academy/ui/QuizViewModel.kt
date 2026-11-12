package com.socialwork.academy.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.socialwork.academy.data.McqRepository
import com.socialwork.academy.data.McqState
import com.socialwork.academy.data.QuizAttempt
import com.socialwork.academy.data.QuizBuilder
import com.socialwork.academy.data.QuizConfig
import com.socialwork.academy.data.QuizSession
import com.socialwork.academy.data.QuizStore
import com.socialwork.academy.data.ReviewItem
import com.socialwork.academy.data.TopicScore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Holds the quiz that is running, so it survives screen rotation, and talks to
 * the quiz history / accuracy storage.
 */
class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val store = QuizStore(application)

    private val _mcq = MutableStateFlow<McqState>(McqState.Loading)
    val mcq: StateFlow<McqState> = _mcq.asStateFlow()

    val attempts: StateFlow<List<QuizAttempt>> =
        store.attempts.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val topicStats: StateFlow<Map<String, TopicScore>> =
        store.topicStats.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val _session = MutableStateFlow<QuizSession?>(null)
    val session: StateFlow<QuizSession?> = _session.asStateFlow()

    private val _review = MutableStateFlow<List<ReviewItem>>(emptyList())
    val review: StateFlow<List<ReviewItem>> = _review.asStateFlow()

    init {
        viewModelScope.launch {
            val bundle = withContext(Dispatchers.IO) { McqRepository.load(application) }
            _mcq.value = if (bundle != null) McqState.Ready(bundle) else McqState.Failed
        }
    }

    /** Starts a new quiz. Returns false if there are no questions for this choice. */
    fun start(config: QuizConfig): Boolean {
        val ready = _mcq.value as? McqState.Ready ?: return false
        val questions = QuizBuilder.build(ready.bundle.questions, config)
        if (questions.isEmpty()) return false
        _session.value = QuizSession(config, questions, startedAtMillis = System.currentTimeMillis())
        return true
    }

    fun answer(index: Int, option: Int) {
        _session.value = _session.value?.answer(index, option)
    }

    fun goTo(index: Int) {
        _session.value = _session.value?.goTo(index)
    }

    /** Ends the quiz and saves it. Safe to call twice (e.g. timer and button together). */
    fun finish() {
        val current = _session.value ?: return
        if (current.isFinished) return
        val done = current.finish(System.currentTimeMillis())
        _session.value = done
        viewModelScope.launch { store.record(done.toAttempt(), done.result().byTopic) }
    }

    /** Starts a new quiz with only the questions that were answered wrongly. */
    fun retryIncorrect(): Boolean {
        val old = _session.value ?: return false
        val wrong = old.incorrectQuestions()
        if (wrong.isEmpty()) return false
        _session.value = QuizSession(
            config = old.config.copy(count = wrong.size),
            questions = wrong.shuffled(),
            startedAtMillis = System.currentTimeMillis()
        )
        return true
    }

    fun openReviewOfCurrent() {
        _review.value = _session.value?.reviewItems().orEmpty()
    }

    fun openReviewOf(attempt: QuizAttempt) {
        val ready = _mcq.value as? McqState.Ready ?: return
        _review.value = attempt.reviewItems(ready.bundle)
    }

    fun discard() {
        _session.value = null
    }
}
