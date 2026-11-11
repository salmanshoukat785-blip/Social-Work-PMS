package com.socialwork.academy.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** Everything the app remembers about the user's choices. All stored on the phone only. */
data class UserSettings(
    val isUrdu: Boolean = false,
    /** null = follow the phone's light/dark setting until the user picks one. */
    val darkMode: Boolean? = null,
    val setupDone: Boolean = false,
    val profileName: String = "",
    val institution: String = ""
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
                institution = prefs[KEY_INSTITUTION] ?: ""
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

    private companion object {
        val KEY_URDU = booleanPreferencesKey("is_urdu")
        val KEY_DARK = booleanPreferencesKey("dark_mode")
        val KEY_SETUP_DONE = booleanPreferencesKey("setup_done")
        val KEY_NAME = stringPreferencesKey("profile_name")
        val KEY_INSTITUTION = stringPreferencesKey("profile_institution")
    }
}
