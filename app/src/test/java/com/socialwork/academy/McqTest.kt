package com.socialwork.academy

import com.socialwork.academy.data.McqBundle
import com.socialwork.academy.data.McqParser
import com.socialwork.academy.data.TopicCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Checks the bundled practice questions (assets/content/mcqs.json). */
class McqTest {

    private val bundle: McqBundle by lazy {
        val file = File("src/main/assets/content/mcqs.json")
        assertTrue("mcqs.json not found at ${file.absolutePath}", file.exists())
        McqParser.parse(file.readText(Charsets.UTF_8))
    }

    @Test
    fun thereAreAtLeastThreeHundredQuestions() {
        assertTrue(bundle.questions.size >= 300)
        assertTrue(bundle.count(false) > 200)
        assertTrue(bundle.count(true) > 200)
    }

    @Test
    fun idsAreUnique() {
        assertEquals(bundle.questions.size, bundle.questions.map { it.id }.toSet().size)
    }

    @Test
    fun everyQuestionIsWellFormed() {
        bundle.questions.forEach { q ->
            assertEquals(q.id, 4, q.options.size)
            assertEquals(q.id, 4, q.options.toSet().size)
            assertTrue(q.id, q.answerIndex in q.options.indices)
            assertTrue(q.id, q.question.isNotBlank())
            assertTrue(q.id, q.explanation.isNotBlank())
            assertTrue(q.id, q.options.all { it.isNotBlank() })
        }
    }

    @Test
    fun topicsExistAndPaperMatches() {
        bundle.questions.forEach { q ->
            val topic = TopicCatalog.byId(q.topicId)
            assertTrue("${q.id}: unknown topic ${q.topicId}", topic != null)
            assertEquals(q.id, topic!!.paper, q.paper)
        }
    }

    @Test
    fun correctAnswersAreSpreadAcrossPositions() {
        val counts = bundle.questions.groupingBy { it.answerIndex }.eachCount()
        assertEquals(4, counts.size)
        assertTrue(counts.values.all { it > bundle.questions.size / 8 })
    }

    @Test
    fun bundleIsLabelledAsAdditionalPractice() {
        assertTrue(bundle.label.contains("Additional practice", ignoreCase = true))
    }
}
