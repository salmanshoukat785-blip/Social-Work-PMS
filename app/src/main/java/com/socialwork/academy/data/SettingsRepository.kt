package com.socialwork.academy.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** Everything the app remembers about the user's choices and activity. All stored on the phone only. */
data class UserSettings(
    val isUrdu: Boolean = false,
    /** null = follow the phone's light/dark setting until the user picks one. */
    val darkMode: Boolean? = null,
    val setupDone: Boolean = false,
    val profileName: String = "",
    val institution: String = "",
    /** IDs of topics the user marked as completed. */
    val completedTopics: Set<String> = emptySet(),
    /** The topic the user opened most recently (for "Continue learning"). */
    val lastTopicId: String? = null
)

class SettingsRepository(context: Context) {

    private val store = context.applicationContext.settingsDataStore

    val settings: Flow<UserSettings> = store.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs ->
            UserSettings(
                isUrdu = prefs[KEY_URDU] ?: false,
                darkMode = prefs[KEY_DARK],
                setupDone = prefs[KEY_SETUP_DONE] ?: false,
                profileName = prefs[KEY_NAME] ?: "",
                institution = prefs[KEY_INSTITUTION] ?: "",
                completedTopics = prefs[KEY_COMPLETED] ?: emptySet(),
                lastTopicId = prefs[KEY_LAST_TOPIC]
            )
        }

    suspend fun setUrdu(value: Boolean) {
        store.edit { it[KEY_URDU] = value }
    }

    suspend fun setDarkMode(value: Boolean) {
        store.edit { it[KEY_DARK] = value }
    }

    /** Saves the profile and marks the first-run setup as finished. */
    suspend fun saveProfile(name: String, institution: String) {
        store.edit {
            it[KEY_NAME] = name.trim()
            it[KEY_INSTITUTION] = institution.trim()
            it[KEY_SETUP_DONE] = true
        }
    }

    /** First-run setup finished without entering a profile. */
    suspend fun finishSetupWithoutProfile() {
        store.edit { it[KEY_SETUP_DONE] = true }
    }

    suspend fun setTopicCompleted(topicId: String, completed: Boolean) {
        store.edit {
            val current = it[KEY_COMPLETED] ?: emptySet()
            it[KEY_COMPLETED] = if (completed) current + topicId else current - topicId
        }
    }

    suspend fun setLastTopic(topicId: String) {
        store.edit { it[KEY_LAST_TOPIC] = topicId }
    }

    private companion object {
        val KEY_URDU = booleanPreferencesKey("is_urdu")
        val KEY_DARK = booleanPreferencesKey("dark_mode")
        val KEY_SETUP_DONE = booleanPreferencesKey("setup_done")
        val KEY_NAME = stringPreferencesKey("profile_name")
        val KEY_INSTITUTION = stringPreferencesKey("profile_institution")
        val KEY_COMPLETED = stringSetPreferencesKey("completed_topics")
        val KEY_LAST_TOPIC = stringPreferencesKey("last_topic")
    }
}
