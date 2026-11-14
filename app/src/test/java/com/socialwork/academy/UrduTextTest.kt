package com.socialwork.academy

import com.socialwork.academy.ui.AppStrings
import com.socialwork.academy.ui.DataStrings
import com.socialwork.academy.ui.QuizStrings
import com.socialwork.academy.ui.StudyStrings
import org.junit.Assert.assertTrue
import org.junit.Test

/** The Urdu interface must really be Urdu: no text left in English by mistake. */
class UrduTextTest {

    private fun hasUrduLetters(s: String) = s.any { it in '؀'..'ۿ' }

    @Test
    fun everyUrduInterfaceTextContainsUrduLetters() {
        val all = AppStrings.Urdu.all() + QuizStrings.Urdu.all() + StudyStrings.Urdu.all() + DataStrings.Urdu.all()
        val missing = all.filterNot { hasUrduLetters(it) }
        assertTrue("Urdu texts without Urdu letters: $missing", missing.isEmpty())
    }

    @Test
    fun placeholdersAreKeptInBothLanguages() {
        listOf(QuizStrings.English.questionOfFormat, QuizStrings.Urdu.questionOfFormat).forEach {
            assertTrue(it.contains("{i}") && it.contains("{n}"))
        }
        listOf(StudyStrings.English.knownCountFormat, StudyStrings.Urdu.knownCountFormat).forEach {
            assertTrue(it.contains("{n}") && it.contains("{t}"))
        }
    }
}
