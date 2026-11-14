package com.socialwork.academy.data

import kotlin.math.roundToInt
import kotlin.random.Random

/** What a quiz is drawn from. */
sealed class QuizScope {
    abstract val key: String
    abstract fun matches(q: Mcq): Boolean

    data object Everything : QuizScope() {
        override val key = "all"
        override fun matches(q: Mcq) = true
    }

    data class ByPaper(val paperId: Int) : QuizScope() {
        override val key = "paper:$paperId"
        override fun matches(q: Mcq) = q.paper == paperId
    }

    data class ByTopic(val topicId: String) : QuizScope() {
        override val key = "topic:$topicId"
        override fun matches(q: Mcq) = q.topicId == topicId
    }

    companion object {
        /** Reads a key written by [key]; returns null for anything unknown. */
        fun fromKey(key: String): QuizScope? = when {
            key == "all" -> Everything
            key.startsWith("paper:") -> key.removePrefix("paper:").toIntOrNull()?.let { ByPaper(it) }
            key.startsWith("topic:") -> ByTopic(key.removePrefix("topic:"))
            else -> null
        }
    }
}

object QuizOptions {
    val counts: List<Int> = listOf(20, 50, 100)

    /** Timer lengths in minutes; 0 means no timer. */
    val timers: List<Int> = listOf(0, 10, 20, 30, 60)

    /** Mock tests: number of questions and duration in minutes. */
    val mockCounts: List<Int> = listOf(30, 50, 100)
    val mockDurations: List<Int> = listOf(30, 60, 90, 120)
}

data class QuizConfig(
    val scope: QuizScope,
    val count: Int,
    /** 0 = no timer. */
    val timerMinutes: Int,
    /** true: the answer and explanation are shown right after each question. */
    val instantFeedback: Boolean,
    val isUrdu: Boolean,
    /** true for an exam-style mock test (timed, answers shown only after submitting). */
    val mock: Boolean = false
) {
    /** Settings for "retry incorrect questions": a relaxed practice quiz, not a timed exam. */
    fun forRetry(count: Int): QuizConfig =
        copy(count = count, timerMinutes = 0, instantFeedback = true, mock = false)
}

object QuizBuilder {

    private fun candidates(pool: List<Mcq>, scope: QuizScope, isUrdu: Boolean): List<Mcq> =
        pool.filter { it.isUrdu == isUrdu && scope.matches(it) }.distinctBy { it.id }

    /** How many questions exist for this choice. */
    fun available(pool: List<Mcq>, scope: QuizScope, isUrdu: Boolean): Int =
        candidates(pool, scope, isUrdu).size

    /**
     * Picks random questions for a quiz. No question appears twice in one quiz.
     * If fewer questions exist than asked for, all of them are used.
     */
    fun build(pool: List<Mcq>, config: QuizConfig, random: Random = Random.Default): List<Mcq> =
        candidates(pool, config.scope, config.isUrdu).shuffled(random).take(config.count)
}

data class TopicScore(val attempted: Int, val correct: Int) {
    val percent: Int get() = if (attempted == 0) 0 else (correct * 100.0 / attempted).roundToInt()

    operator fun plus(other: TopicScore) =
        TopicScore(attempted + other.attempted, correct + other.correct)
}

data class QuizResult(
    val total: Int,
    val correct: Int,
    val wrong: Int,
    val unanswered: Int,
    val percent: Int,
    val durationSeconds: Long,
    /** Answered questions per topic, for "accuracy by topic". */
    val byTopic: Map<String, TopicScore>
)

/** One question with what the user chose (-1 = not answered). */
data class ReviewItem(val question: Mcq, val chosen: Int) {
    val isAnswered: Boolean get() = chosen >= 0
    val isCorrect: Boolean get() = chosen == question.answerIndex
}

/** A finished quiz, as saved on the phone. */
data class QuizAttempt(
    val timestampMillis: Long,
    val scopeKey: String,
    val isUrdu: Boolean,
    val questionIds: List<String>,
    /** Chosen option per question; -1 = not answered. */
    val chosen: List<Int>,
    val correct: Int,
    val durationSeconds: Long,
    val mock: Boolean = false
) {
    val total: Int get() = questionIds.size
    val answered: Int get() = chosen.count { it >= 0 }
    val wrong: Int get() = answered - correct
    val percent: Int get() = if (total == 0) 0 else (correct * 100.0 / total).roundToInt()

    fun reviewItems(bundle: McqBundle): List<ReviewItem> =
        questionIds.mapIndexedNotNull { i, id ->
            bundle.byId(id)?.let { ReviewItem(it, chosen.getOrElse(i) { -1 }) }
        }
}

/** A quiz in progress (or just finished). Immutable: every change returns a new session. */
data class QuizSession(
    val config: QuizConfig,
    val questions: List<Mcq>,
    val startedAtMillis: Long,
    /** question index -> chosen option index */
    val answers: Map<Int, Int> = emptyMap(),
    val currentIndex: Int = 0,
    val finishedAtMillis: Long? = null,
    /** Question indexes the student marked for review. */
    val marked: Set<Int> = emptySet()
) {
    val isFinished: Boolean get() = finishedAtMillis != null

    /** When the timer runs out; null if there is no timer. */
    val deadlineMillis: Long? =
        if (config.timerMinutes > 0) startedAtMillis + config.timerMinutes * 60_000L else null

    /** With instant feedback an answer cannot be changed once given. */
    fun isLocked(index: Int): Boolean =
        isFinished || (config.instantFeedback && answers.containsKey(index))

    fun answer(index: Int, option: Int): QuizSession {
        val q = questions.getOrNull(index) ?: return this
        if (option !in q.options.indices || isLocked(index)) return this
        return copy(answers = answers + (index to option))
    }

    fun toggleMark(index: Int): QuizSession {
        if (isFinished || index !in questions.indices) return this
        return copy(marked = if (index in marked) marked - index else marked + index)
    }

    fun goTo(index: Int): QuizSession =
        if (index in questions.indices) copy(currentIndex = index) else this

    fun unansweredCount(): Int = questions.size - answers.size

    fun finish(nowMillis: Long): QuizSession =
        if (isFinished) this else copy(finishedAtMillis = nowMillis)

    fun reviewItems(): List<ReviewItem> =
        questions.mapIndexed { i, q -> ReviewItem(q, answers[i] ?: -1) }

    /** Questions that were answered wrongly (for "retry incorrect questions"). */
    fun incorrectQuestions(): List<Mcq> =
        questions.filterIndexed { i, q -> answers[i]?.let { it != q.answerIndex } ?: false }

    fun result(): QuizResult {
        var correct = 0
        var wrong = 0
        val byTopic = LinkedHashMap<String, TopicScore>()
        questions.forEachIndexed { i, q ->
            val chosen = answers[i] ?: return@forEachIndexed
            val isRight = chosen == q.answerIndex
            if (isRight) correct++ else wrong++
            byTopic[q.topicId] =
                (byTopic[q.topicId] ?: TopicScore(0, 0)) + TopicScore(1, if (isRight) 1 else 0)
        }
        val total = questions.size
        val end = finishedAtMillis ?: startedAtMillis
        return QuizResult(
            total = total,
            correct = correct,
            wrong = wrong,
            unanswered = total - correct - wrong,
            percent = if (total == 0) 0 else (correct * 100.0 / total).roundToInt(),
            durationSeconds = ((end - startedAtMillis) / 1000).coerceAtLeast(0),
            byTopic = byTopic
        )
    }

    /** The record that is saved in the quiz history. Only for a finished quiz. */
    fun toAttempt(): QuizAttempt {
        val r = result()
        return QuizAttempt(
            timestampMillis = finishedAtMillis ?: startedAtMillis,
            scopeKey = config.scope.key,
            isUrdu = config.isUrdu,
            questionIds = questions.map { it.id },
            chosen = questions.indices.map { answers[it] ?: -1 },
            correct = r.correct,
            durationSeconds = r.durationSeconds,
            mock = config.mock
        )
    }
}

object QuizStats {
    fun merge(old: Map<String, TopicScore>, add: Map<String, TopicScore>): Map<String, TopicScore> {
        val out = LinkedHashMap(old)
        add.forEach { (id, s) -> out[id] = (out[id] ?: TopicScore(0, 0)) + s }
        return out
    }
}
