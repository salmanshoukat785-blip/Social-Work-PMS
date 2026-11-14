package com.socialwork.academy.data

data class WeakTopic(val topicId: String, val score: TopicScore)

data class ProgressSummary(
    val mcqsAttempted: Int,
    val mcqsCorrect: Int,
    /** null until at least one question has been answered. */
    val accuracyPercent: Int?,
    val bestMockPercent: Int?,
    val bestQuizPercent: Int?,
    val recent: List<QuizAttempt>,
    val weak: List<WeakTopic>
)

/** Statistics worked out only from what the student actually did on this phone. */
object ProgressCalc {

    /** A topic counts as "weak" with at least this many answers... */
    const val MIN_ANSWERS_FOR_WEAK = 5

    /** ...and an accuracy below this percentage. */
    const val WEAK_BELOW_PERCENT = 70

    fun summarize(
        attempts: List<QuizAttempt>,
        topicStats: Map<String, TopicScore>,
        recentLimit: Int = 5,
        weakLimit: Int = 5
    ): ProgressSummary {
        val attempted = topicStats.values.sumOf { it.attempted }
        val correct = topicStats.values.sumOf { it.correct }
        val weak = topicStats.entries
            .filter { it.value.attempted >= MIN_ANSWERS_FOR_WEAK && it.value.percent < WEAK_BELOW_PERCENT }
            .sortedWith(compareBy({ it.value.percent }, { it.key }))
            .take(weakLimit)
            .map { WeakTopic(it.key, it.value) }
        return ProgressSummary(
            mcqsAttempted = attempted,
            mcqsCorrect = correct,
            accuracyPercent = if (attempted == 0) null else (correct * 100.0 / attempted).let { Math.round(it).toInt() },
            bestMockPercent = attempts.filter { it.mock }.maxOfOrNull { it.percent },
            bestQuizPercent = attempts.filter { !it.mock }.maxOfOrNull { it.percent },
            recent = attempts.sortedByDescending { it.timestampMillis }.take(recentLimit),
            weak = weak
        )
    }
}
