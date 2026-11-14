package com.socialwork.academy.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** What the student did on one day. Only things the app measured itself. */
data class DayActivity(
    val questions: Int = 0,
    val correct: Int = 0,
    /** Time spent inside finished quizzes and mock tests. */
    val quizSeconds: Long = 0,
    /** Topics opened that day (each topic counted once). */
    val topics: Set<String> = emptySet()
) {
    operator fun plus(o: DayActivity) = DayActivity(
        questions + o.questions, correct + o.correct, quizSeconds + o.quizSeconds, topics + o.topics
    )
}

object DayKey {
    /** "2026-11-13" for a moment in time, in the given time zone. */
    fun of(millis: Long, zone: TimeZone = TimeZone.getDefault()): String {
        val f = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        f.timeZone = zone
        return f.format(Date(millis))
    }
}

object ActivityJson {

    fun encode(activity: Map<String, DayActivity>): String {
        val o = JSONObject()
        activity.forEach { (day, a) ->
            o.put(
                day,
                JSONObject()
                    .put("q", a.questions)
                    .put("c", a.correct)
                    .put("s", a.quizSeconds)
                    .put("t", JSONArray(a.topics.toList()))
            )
        }
        return o.toString()
    }

    fun decode(text: String?): Map<String, DayActivity> {
        if (text.isNullOrBlank()) return emptyMap()
        return try {
            val o = JSONObject(text)
            val out = LinkedHashMap<String, DayActivity>()
            o.keys().forEach { day ->
                val a = o.getJSONObject(day)
                val t = a.optJSONArray("t")
                out[day] = DayActivity(
                    questions = a.optInt("q"),
                    correct = a.optInt("c"),
                    quizSeconds = a.optLong("s"),
                    topics = if (t == null) emptySet() else (0 until t.length()).map { t.getString(it) }.toSet()
                )
            }
            out
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /** Keeps the newest [maxDays] days. */
    fun trim(activity: Map<String, DayActivity>, maxDays: Int = 120): Map<String, DayActivity> =
        activity.entries.sortedByDescending { it.key }.take(maxDays).associate { it.key to it.value }
}

private val Context.activityDataStore: DataStore<Preferences> by preferencesDataStore(name = "activity")

class ActivityStore(context: Context) {

    private val store = context.applicationContext.activityDataStore

    val activity: Flow<Map<String, DayActivity>> = store.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { ActivityJson.decode(it[KEY]) }

    private suspend fun update(change: (Map<String, DayActivity>) -> Map<String, DayActivity>) {
        store.edit { prefs ->
            val now = ActivityJson.decode(prefs[KEY])
            prefs[KEY] = ActivityJson.encode(ActivityJson.trim(change(now)))
        }
    }

    suspend fun recordQuiz(day: String, answered: Int, correct: Int, seconds: Long) {
        if (answered <= 0) return
        update { it + (day to ((it[day] ?: DayActivity()) + DayActivity(answered, correct, seconds))) }
    }

    suspend fun recordTopic(day: String, topicId: String) {
        update { it + (day to ((it[day] ?: DayActivity()) + DayActivity(topics = setOf(topicId)))) }
    }

    suspend fun replaceAll(activity: Map<String, DayActivity>) = update { activity }

    suspend fun clear() = update { emptyMap() }

    private companion object {
        val KEY = stringPreferencesKey("days")
    }
}

/** One bar of the weekly chart. */
data class DayBar(val dayKey: String, val millis: Long, val questions: Int, val quizSeconds: Long, val topics: Int)

object WeekChart {

    /** The last [days] days ending on [endMillis], oldest first. Days without activity have zeros. */
    fun build(
        activity: Map<String, DayActivity>,
        endMillis: Long,
        zone: TimeZone = TimeZone.getDefault(),
        days: Int = 7
    ): List<DayBar> = (days - 1 downTo 0).map { back ->
        val cal = Calendar.getInstance(zone)
        cal.timeInMillis = endMillis
        cal.add(Calendar.DAY_OF_YEAR, -back)
        val key = DayKey.of(cal.timeInMillis, zone)
        val a = activity[key]
        DayBar(key, cal.timeInMillis, a?.questions ?: 0, a?.quizSeconds ?: 0, a?.topics?.size ?: 0)
    }
}
