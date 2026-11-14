package com.socialwork.academy

import com.socialwork.academy.data.ActivityJson
import com.socialwork.academy.data.BackupData
import com.socialwork.academy.data.BackupJson
import com.socialwork.academy.data.DayActivity
import com.socialwork.academy.data.DayKey
import com.socialwork.academy.data.ProgressCalc
import com.socialwork.academy.data.QuizAttempt
import com.socialwork.academy.data.TopicScore
import com.socialwork.academy.data.UserSettings
import com.socialwork.academy.data.WeekChart
import com.socialwork.academy.ui.DataStatus
import com.socialwork.academy.ui.DataStrings
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

class ProgressBackupTest {

    private val utc = TimeZone.getTimeZone("UTC")

    private fun millis(y: Int, m: Int, d: Int, h: Int = 12): Long =
        GregorianCalendar(utc).apply { clear(); set(y, m, d, h, 0, 0) }.timeInMillis

    private fun attempt(correct: Int, total: Int, mock: Boolean, t: Long) = QuizAttempt(
        timestampMillis = t, scopeKey = "all", isUrdu = false,
        questionIds = List(total) { "q$it" }, chosen = List(total) { 0 },
        correct = correct, durationSeconds = 60, mock = mock
    )

    @Test
    fun summaryUsesOnlyRealActivity() {
        val empty = ProgressCalc.summarize(emptyList(), emptyMap())
        assertEquals(0, empty.mcqsAttempted)
        assertNull(empty.accuracyPercent)
        assertNull(empty.bestMockPercent)
        assertNull(empty.bestQuizPercent)
        assertTrue(empty.weak.isEmpty() && empty.recent.isEmpty())
    }

    @Test
    fun summaryCountsAccuracyBestScoresRecentAndWeakTopics() {
        val attempts = listOf(
            attempt(5, 10, mock = false, t = 1), attempt(9, 10, mock = false, t = 2),
            attempt(6, 10, mock = true, t = 3), attempt(7, 10, mock = true, t = 4)
        )
        val stats = mapOf(
            "p1_family" to TopicScore(10, 4),   // weak
            "p2_case_work" to TopicScore(10, 9), // fine
            "p1_culture" to TopicScore(3, 0)     // too few answers to call weak
        )
        val s = ProgressCalc.summarize(attempts, stats)
        assertEquals(23, s.mcqsAttempted)
        assertEquals(13, s.mcqsCorrect)
        assertEquals(57, s.accuracyPercent)
        assertEquals(70, s.bestMockPercent)
        assertEquals(90, s.bestQuizPercent)
        assertEquals(listOf(4L, 3L, 2L, 1L), s.recent.map { it.timestampMillis })
        assertEquals(listOf("p1_family"), s.weak.map { it.topicId })
    }

    @Test
    fun dayKeyUsesTheGivenTimeZone() {
        val t = millis(2026, Calendar.NOVEMBER, 13, 23)
        assertEquals("2026-11-13", DayKey.of(t, utc))
        assertEquals("2026-11-14", DayKey.of(t, TimeZone.getTimeZone("GMT+05:00")))
    }

    @Test
    fun weekChartHasSevenDaysOldestFirstWithZerosForQuietDays() {
        val end = millis(2026, Calendar.NOVEMBER, 13)
        val activity = mapOf(
            "2026-11-13" to DayActivity(questions = 20, quizSeconds = 300, topics = setOf("a", "b")),
            "2026-11-10" to DayActivity(questions = 5),
            "2026-10-01" to DayActivity(questions = 99) // outside the week
        )
        val bars = WeekChart.build(activity, end, utc)
        assertEquals(7, bars.size)
        assertEquals("2026-11-07", bars.first().dayKey)
        assertEquals("2026-11-13", bars.last().dayKey)
        assertEquals(20, bars.last().questions)
        assertEquals(2, bars.last().topics)
        assertEquals(5, bars.first { it.dayKey == "2026-11-10" }.questions)
        assertEquals(25, bars.sumOf { it.questions })
    }

    @Test
    fun activitySurvivesSaveAndLoadAndTrims() {
        val a = mapOf("2026-11-13" to DayActivity(3, 2, 90, setOf("p1_family")))
        assertEquals(a, ActivityJson.decode(ActivityJson.encode(a)))
        assertTrue(ActivityJson.decode("not json").isEmpty())
        val many = (1..150).associate { "2026-01-%03d".format(it) to DayActivity(1) }
        val kept = ActivityJson.trim(many, 120)
        assertEquals(120, kept.size)
        assertTrue("2026-01-150" in kept && "2026-01-001" !in kept)
    }

    private val data = BackupData(
        createdAtMillis = 123L,
        settings = UserSettings(
            isUrdu = true, darkMode = true, setupDone = false, profileName = "علی",
            institution = "Uni", completedTopics = setOf("p1_family", "bogus_topic"),
            lastTopicId = "p1_family", bookmarks = setOf("p2_case_work"), fontScale = 1.3f
        ),
        notes = mapOf("p1_family" to "my note\nline 2", "bogus_topic" to "x"),
        knownCards = setOf("g0001"),
        attempts = listOf(attempt(7, 10, mock = true, t = 5)),
        topicStats = mapOf("p1_family" to TopicScore(10, 7)),
        activity = mapOf("2026-11-13" to DayActivity(10, 7, 120, setOf("p1_family")))
    )

    @Test
    fun backupRoundTripKeepsEverythingAndDropsUnknownTopics() {
        val back = BackupJson.decode(BackupJson.encode(data))
        assertNotNull(back)
        back!!
        assertEquals(setOf("p1_family"), back.settings.completedTopics)
        assertEquals(setOf("p2_case_work"), back.settings.bookmarks)
        assertEquals("p1_family", back.settings.lastTopicId)
        assertEquals("علی", back.settings.profileName)
        assertTrue(back.settings.isUrdu)
        assertEquals(true, back.settings.darkMode)
        assertEquals(1.3f, back.settings.fontScale)
        assertEquals(mapOf("p1_family" to "my note\nline 2"), back.notes)
        assertEquals(setOf("g0001"), back.knownCards)
        assertEquals(data.attempts, back.attempts)
        assertTrue(back.attempts.single().mock)
        assertEquals(data.topicStats, back.topicStats)
        assertEquals(data.activity, back.activity)
    }

    @Test
    fun backupWithoutThemeChoiceKeepsFollowingThePhone() {
        val back = BackupJson.decode(BackupJson.encode(data.copy(settings = data.settings.copy(darkMode = null))))
        assertNull(back!!.settings.darkMode)
    }

    @Test
    fun filesThatAreNotOurBackupAreRejected() {
        assertNull(BackupJson.decode("hello"))
        assertNull(BackupJson.decode("{}"))
        val good = JSONObject(BackupJson.encode(data))
        assertNull(BackupJson.decode(JSONObject(good.toString()).put("app", "Other").toString()))
        assertNull(BackupJson.decode(JSONObject(good.toString()).put("backupVersion", 99).toString()))
        assertNull(BackupJson.decode(good.toString().replace("\"settings\"", "\"x\"")))
    }

    @Test
    fun oddFontSizeInBackupSnapsToAKnownSize() {
        val json = JSONObject(BackupJson.encode(data))
        json.getJSONObject("settings").put("fontScale", 9.0)
        assertEquals(1.6f, BackupJson.decode(json.toString())!!.settings.fontScale)
    }

    @Test
    fun dataTextsAreCompleteInBothLanguages() {
        assertTrue(DataStrings.English.all().all { it.isNotBlank() })
        assertTrue(DataStrings.Urdu.all().all { it.isNotBlank() })
        assertEquals(DataStrings.English.all().size, DataStrings.Urdu.all().size)
        DataStatus.values().forEach {
            assertTrue(DataStrings.English.statusText(it).isNotBlank())
            assertTrue(DataStrings.Urdu.statusText(it).isNotBlank())
        }
        listOf(DataStrings.English, DataStrings.Urdu).forEach {
            listOf(it.paperProgress("P", 3, 15), it.weakHelp(5, 70), it.weekQuizTime(12),
                it.weekTopics(4), it.questionsShort(9), it.version("0.9.0")).forEach { t ->
                assertFalse(t, t.contains("{"))
            }
        }
    }
}
