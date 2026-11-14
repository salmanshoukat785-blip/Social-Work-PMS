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

/** Turns quiz history and topic accuracy into text and back (stored on the phone only). */
object QuizJson {

    fun encodeAttempts(attempts: List<QuizAttempt>): String {
        val array = JSONArray()
        attempts.forEach { a ->
            array.put(
                JSONObject()
                    .put("t", a.timestampMillis)
                    .put("s", a.scopeKey)
                    .put("u", a.isUrdu)
                    .put("ids", JSONArray(a.questionIds))
                    .put("ch", JSONArray(a.chosen))
                    .put("c", a.correct)
                    .put("d", a.durationSeconds)
                    .put("m", a.mock)
            )
        }
        return array.toString()
    }

    /** Unreadable or missing text gives an empty list, never a crash. */
    fun decodeAttempts(text: String?): List<QuizAttempt> {
        if (text.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(text)
            (0 until array.length()).map { i ->
                val o = array.getJSONObject(i)
                val ids = o.getJSONArray("ids")
                val ch = o.getJSONArray("ch")
                QuizAttempt(
                    timestampMillis = o.getLong("t"),
                    scopeKey = o.getString("s"),
                    isUrdu = o.getBoolean("u"),
                    questionIds = (0 until ids.length()).map { ids.getString(it) },
                    chosen = (0 until ch.length()).map { ch.getInt(it) },
                    correct = o.getInt("c"),
                    durationSeconds = o.getLong("d"),
                    mock = o.optBoolean("m", false)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun encodeStats(stats: Map<String, TopicScore>): String {
        val o = JSONObject()
        stats.forEach { (id, s) -> o.put(id, JSONArray(listOf(s.attempted, s.correct))) }
        return o.toString()
    }

    fun decodeStats(text: String?): Map<String, TopicScore> {
        if (text.isNullOrBlank()) return emptyMap()
        return try {
            val o = JSONObject(text)
            val out = LinkedHashMap<String, TopicScore>()
            o.keys().forEach { id ->
                val a = o.getJSONArray(id)
                out[id] = TopicScore(a.getInt(0), a.getInt(1))
            }
            out
        } catch (e: Exception) {
            emptyMap()
        }
    }
}

private val Context.quizDataStore: DataStore<Preferences> by preferencesDataStore(name = "quiz")

class QuizStore(context: Context) {

    private val store = context.applicationContext.quizDataStore

    private val data: Flow<Preferences> = store.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    /** Newest first. */
    val attempts: Flow<List<QuizAttempt>> = data.map { QuizJson.decodeAttempts(it[KEY_ATTEMPTS]) }

    val topicStats: Flow<Map<String, TopicScore>> = data.map { QuizJson.decodeStats(it[KEY_STATS]) }

    /** Saves a finished quiz and adds its answers to the per-topic accuracy. */
    suspend fun record(attempt: QuizAttempt, byTopic: Map<String, TopicScore>) {
        store.edit { prefs ->
            val history = QuizJson.decodeAttempts(prefs[KEY_ATTEMPTS])
            prefs[KEY_ATTEMPTS] = QuizJson.encodeAttempts((listOf(attempt) + history).take(MAX_HISTORY))
            val stats = QuizJson.decodeStats(prefs[KEY_STATS])
            prefs[KEY_STATS] = QuizJson.encodeStats(QuizStats.merge(stats, byTopic))
        }
    }

    suspend fun replaceAll(attempts: List<QuizAttempt>, stats: Map<String, TopicScore>) {
        store.edit { prefs ->
            prefs[KEY_ATTEMPTS] = QuizJson.encodeAttempts(attempts.take(MAX_HISTORY))
            prefs[KEY_STATS] = QuizJson.encodeStats(stats)
        }
    }

    suspend fun clear() = replaceAll(emptyList(), emptyMap())

    private companion object {
        const val MAX_HISTORY = 50
        val KEY_ATTEMPTS = stringPreferencesKey("attempts")
        val KEY_STATS = stringPreferencesKey("topic_stats")
    }
}
