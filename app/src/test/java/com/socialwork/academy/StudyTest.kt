package com.socialwork.academy

import com.socialwork.academy.data.StudyJson
import com.socialwork.academy.ui.StudyStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StudyTest {

    @Test
    fun notesSurviveSaveAndLoadIncludingUrduAndNewLines() {
        val notes = mapOf("p1_family" to "یاد رکھنا\nFamily types", "p2_case_work" to "Case work steps")
        assertEquals(notes, StudyJson.decodeNotes(StudyJson.encodeNotes(notes)))
    }

    @Test
    fun blankNoteRemovesTheNote() {
        val notes = mapOf("a" to "text")
        assertTrue(StudyJson.withNote(notes, "a", "   ").isEmpty())
        assertEquals("new", StudyJson.withNote(notes, "a", "new")["a"])
        assertEquals(2, StudyJson.withNote(notes, "b", "x").size)
    }

    @Test
    fun knownCardsSurviveSaveAndLoad() {
        val ids = setOf("g0001", "g0042")
        assertEquals(ids, StudyJson.decodeIds(StudyJson.encodeIds(ids)))
    }

    @Test
    fun brokenSavedTextGivesEmptyData() {
        assertTrue(StudyJson.decodeNotes("{{").isEmpty())
        assertTrue(StudyJson.decodeNotes(null).isEmpty())
        assertTrue(StudyJson.decodeIds("nope").isEmpty())
        assertTrue(StudyJson.decodeIds("").isEmpty())
    }

    @Test
    fun studyTextsAreCompleteInBothLanguages() {
        assertTrue(StudyStrings.English.all().all { it.isNotBlank() })
        assertTrue(StudyStrings.Urdu.all().all { it.isNotBlank() })
        assertEquals(StudyStrings.English.all().size, StudyStrings.Urdu.all().size)
    }

    @Test
    fun studyNumbersAreInserted() {
        listOf(StudyStrings.English, StudyStrings.Urdu).forEach {
            listOf(it.termsCount(12), it.notesCount(3), it.knownCount(4, 346), it.cardOf(2, 9),
                it.roundSummary(5, 2), it.pageOf(3, 40)).forEach { t ->
                assertTrue(t, !t.contains("{") && t.any { c -> c.isDigit() })
            }
        }
    }
}
