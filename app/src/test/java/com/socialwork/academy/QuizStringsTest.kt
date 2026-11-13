package com.socialwork.academy

import com.socialwork.academy.ui.QuizStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizStringsTest {

    @Test
    fun textsAreNotBlankAndMatchInBothLanguages() {
        assertTrue(QuizStrings.English.all().all { it.isNotBlank() })
        assertTrue(QuizStrings.Urdu.all().all { it.isNotBlank() })
        assertEquals(QuizStrings.English.all().size, QuizStrings.Urdu.all().size)
    }

    @Test
    fun numbersAreInserted() {
        listOf(QuizStrings.English, QuizStrings.Urdu).forEach {
            val texts = listOf(
                it.minutes(20), it.available(57), it.questionOf(3, 20), it.unanswered(4),
                it.score(15, 20), it.accuracy(3, 4, 75)
            )
            texts.forEach { t -> assertTrue(t, !t.contains("{") && t.any { c -> c.isDigit() }) }
        }
    }
}
