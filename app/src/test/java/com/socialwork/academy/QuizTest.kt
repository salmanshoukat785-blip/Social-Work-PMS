package com.socialwork.academy

import com.socialwork.academy.data.Mcq
import com.socialwork.academy.data.QuizBuilder
import com.socialwork.academy.data.QuizConfig
import com.socialwork.academy.data.QuizJson
import com.socialwork.academy.data.QuizScope
import com.socialwork.academy.data.QuizSession
import com.socialwork.academy.data.TopicScore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class QuizTest {

    private fun q(id: String, topic: String, paper: Int, lang: String = "en", answer: Int = 0) = Mcq(
        id = id, lang = lang, kind = "which_topic", topicId = topic, paper = paper,
        question = "Q $id", options = listOf("a", "b", "c", "d"), answerIndex = answer,
        explanation = "because"
    )

    private val pool = listOf(
        q("1", "p1_family", 1), q("2", "p1_family", 1), q("3", "p2_case_work", 2),
        q("4", "p2_case_work", 2), q("5", "p2_case_work", 2), q("6", "p1_family", 1, lang = "ur")
    )

    private fun config(scope: QuizScope = QuizScope.Everything, count: Int = 20,
                       timer: Int = 0, feedback: Boolean = true, urdu: Boolean = false) =
        QuizConfig(scope, count, timer, feedback, urdu)

    private fun session(c: QuizConfig = config(), questions: List<Mcq> = pool.take(3)) =
        QuizSession(c, questions, startedAtMillis = 1_000L)

    @Test
    fun builderFiltersByLanguageAndScope() {
        assertEquals(5, QuizBuilder.available(pool, QuizScope.Everything, false))
        assertEquals(1, QuizBuilder.available(pool, QuizScope.Everything, true))
        assertEquals(3, QuizBuilder.available(pool, QuizScope.ByPaper(2), false))
        assertEquals(2, QuizBuilder.available(pool, QuizScope.ByTopic("p1_family"), false))
    }

    @Test
    fun builderNeverRepeatsAndCapsAtAvailable() {
        val built = QuizBuilder.build(pool, config(count = 100), Random(1))
        assertEquals(5, built.size)
        assertEquals(5, built.map { it.id }.toSet().size)
        assertEquals(2, QuizBuilder.build(pool, config(count = 2), Random(1)).size)
    }

    @Test
    fun scopeKeysRoundTrip() {
        listOf(QuizScope.Everything, QuizScope.ByPaper(2), QuizScope.ByTopic("p1_family")).forEach {
            assertEquals(it, QuizScope.fromKey(it.key))
        }
        assertNull(QuizScope.fromKey("nonsense"))
    }

    @Test
    fun instantFeedbackLocksAnAnswer() {
        val s = session(config(feedback = true)).answer(0, 1).answer(0, 0)
        assertEquals(1, s.answers[0])
    }

    @Test
    fun withoutFeedbackAnswerCanBeChanged() {
        val s = session(config(feedback = false)).answer(0, 1).answer(0, 0)
        assertEquals(0, s.answers[0])
    }

    @Test
    fun invalidAnswerAndIndexAreIgnored() {
        val s = session()
        assertEquals(s, s.answer(0, 9))
        assertEquals(s, s.answer(99, 0))
        assertEquals(s, s.goTo(99))
    }

    @Test
    fun resultCountsCorrectWrongAndUnanswered() {
        val s = session(questions = listOf(pool[0], pool[1], pool[2], pool[3]))
            .answer(0, 0).answer(1, 2).answer(2, 0)
            .finish(11_000L)
        val r = s.result()
        assertEquals(4, r.total)
        assertEquals(2, r.correct)
        assertEquals(1, r.wrong)
        assertEquals(1, r.unanswered)
        assertEquals(50, r.percent)
        assertEquals(10L, r.durationSeconds)
        assertEquals(TopicScore(2, 1), r.byTopic["p1_family"])
        assertEquals(TopicScore(1, 1), r.byTopic["p2_case_work"])
    }

    @Test
    fun finishedSessionCannotBeChanged() {
        val s = session(config(feedback = false)).finish(2_000L)
        assertEquals(s, s.answer(0, 1))
        assertEquals(s, s.finish(9_000L))
    }

    @Test
    fun incorrectQuestionsAreOnlyTheWrongOnes() {
        val s = session().answer(0, 0).answer(1, 3)
        assertEquals(listOf("2"), s.incorrectQuestions().map { it.id })
    }

    @Test
    fun timerDeadlineOnlyWhenTimerIsSet() {
        assertNull(session(config(timer = 0)).deadlineMillis)
        assertEquals(1_000L + 10 * 60_000L, session(config(timer = 10)).deadlineMillis)
    }

    @Test
    fun historyAndStatsSurviveSaveAndLoad() {
        val attempt = session().answer(0, 0).answer(1, 2).finish(5_000L).toAttempt()
        val back = QuizJson.decodeAttempts(QuizJson.encodeAttempts(listOf(attempt)))
        assertEquals(listOf(attempt), back)
        assertEquals(1, attempt.correct)
        assertEquals(1, attempt.wrong)
        assertEquals(listOf(0, 2, -1), attempt.chosen)

        val stats = mapOf("p1_family" to TopicScore(4, 3))
        assertEquals(stats, QuizJson.decodeStats(QuizJson.encodeStats(stats)))
    }

    @Test
    fun brokenSavedTextGivesEmptyResults() {
        assertTrue(QuizJson.decodeAttempts("not json").isEmpty())
        assertTrue(QuizJson.decodeAttempts(null).isEmpty())
        assertTrue(QuizJson.decodeStats("{{").isEmpty())
        assertFalse(QuizJson.decodeStats("{\"a\":[1,1]}").isEmpty())
    }

    @Test
    fun markingForReviewTogglesAndStopsAfterFinish() {
        val s = session().toggleMark(1)
        assertEquals(setOf(1), s.marked)
        assertTrue(s.toggleMark(1).marked.isEmpty())
        assertEquals(s, s.toggleMark(99))
        val done = s.finish(2_000L)
        assertEquals(done, done.toggleMark(0))
    }

    @Test
    fun mockFlagIsSavedAndOldHistoryStillLoads() {
        val mock = session(config(timer = 60, feedback = false).copy(mock = true))
            .answer(0, 0).finish(3_000L).toAttempt()
        assertTrue(mock.mock)
        val back = QuizJson.decodeAttempts(QuizJson.encodeAttempts(listOf(mock)))
        assertTrue(back.single().mock)

        // History saved by Phase 6 has no "m" field: it must load as a normal quiz.
        val old = """[{"t":1,"s":"all","u":false,"ids":["1"],"ch":[0],"c":1,"d":5}]"""
        val loaded = QuizJson.decodeAttempts(old)
        assertEquals(1, loaded.size)
        assertFalse(loaded.single().mock)
    }

    @Test
    fun retryAfterMockIsARelaxedPracticeQuiz() {
        val c = config(timer = 60, feedback = false).copy(mock = true).forRetry(7)
        assertEquals(7, c.count)
        assertEquals(0, c.timerMinutes)
        assertTrue(c.instantFeedback)
        assertFalse(c.mock)
    }

    @Test
    fun mockOptionsAreSensible() {
        assertTrue(com.socialwork.academy.data.QuizOptions.mockCounts.all { it > 0 })
        assertTrue(com.socialwork.academy.data.QuizOptions.mockDurations.all { it > 0 })
    }
}
