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

/** Turns personal notes and "known" flashcards into text and back. Bad text gives empty data. */
object StudyJson {

    fun encodeNotes(notes: Map<String, String>): String {
        val o = JSONObject()
        notes.forEach { (id, text) -> o.put(id, text) }
        return o.toString()
    }

    fun decodeNotes(text: String?): Map<String, String> {
        if (text.isNullOrBlank()) return emptyMap()
        return try {
            val o = JSONObject(text)
            val out = LinkedHashMap<String, String>()
            o.keys().forEach { id -> out[id] = o.getString(id) }
            out
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun encodeIds(ids: Set<String>): String = JSONArray(ids.toList()).toString()

    fun decodeIds(text: String?): Set<String> {
        if (text.isNullOrBlank()) return emptySet()
        return try {
            val a = JSONArray(text)
            (0 until a.length()).map { a.getString(it) }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    /** A note that is only spaces is the same as no note. */
    fun withNote(notes: Map<String, String>, topicId: String, text: String): Map<String, String> =
        if (text.isBlank()) notes - topicId else notes + (topicId to text)
}

private val Context.studyDataStore: DataStore<Preferences> by preferencesDataStore(name = "study")

class StudyStore(context: Context) {

    private val store = context.applicationContext.studyDataStore

    private val data: Flow<Preferences> = store.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    val notes: Flow<Map<String, String>> = data.map { StudyJson.decodeNotes(it[KEY_NOTES]) }

    val knownCards: Flow<Set<String>> = data.map { StudyJson.decodeIds(it[KEY_KNOWN]) }

    suspend fun setNote(topicId: String, text: String) {
        store.edit { prefs ->
            val notes = StudyJson.decodeNotes(prefs[KEY_NOTES])
            prefs[KEY_NOTES] = StudyJson.encodeNotes(StudyJson.withNote(notes, topicId, text))
        }
    }

    suspend fun setKnown(cardId: String, known: Boolean) {
        store.edit { prefs ->
            val ids = StudyJson.decodeIds(prefs[KEY_KNOWN])
            prefs[KEY_KNOWN] = StudyJson.encodeIds(if (known) ids + cardId else ids - cardId)
        }
    }

    suspend fun clearKnown() {
        store.edit { prefs -> prefs[KEY_KNOWN] = StudyJson.encodeIds(emptySet()) }
    }

    private companion object {
        val KEY_NOTES = stringPreferencesKey("notes")
        val KEY_KNOWN = stringPreferencesKey("known_cards")
    }
}
