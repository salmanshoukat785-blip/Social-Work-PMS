package com.islamicstudies.academy.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom

data class AppSettings(
    val loaded: Boolean = false,
    val language: String = "ur",       // "ur" or "en"
    val onboardingDone: Boolean = false,
    val name: String = "",
    val level: String = "basic",       // "basic", "intermediate", "advanced"
    val pinEnabled: Boolean = false,
    val theme: String = "system",       // "system", "light", "dark"
    val textScalePercent: Int = 100,    // 80..160
    val contentLanguage: String = "",   // "" = follow app language, or "ur", "en", "both"
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0
)

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

private object Keys {
    val LANGUAGE = stringPreferencesKey("language")
    val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    val NAME = stringPreferencesKey("name")
    val LEVEL = stringPreferencesKey("level")
    val PIN_SALT = stringPreferencesKey("pin_salt")
    val PIN_HASH = stringPreferencesKey("pin_hash")
    val THEME = stringPreferencesKey("theme")
    val TEXT_SCALE = intPreferencesKey("text_scale")
    val CONTENT_LANGUAGE = stringPreferencesKey("content_language")
    val STUDY_DAYS = stringSetPreferencesKey("study_days")
    val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
    val REMINDER_HOUR = intPreferencesKey("reminder_hour")
    val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
}

/** Local app lock helper. The PIN is only a privacy lock on the phone; it is stored salted and hashed. */
object PinHasher {
    fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return toHex(bytes)
    }

    fun hash(pin: String, salt: String): String {
        var current = (salt + pin).toByteArray(Charsets.UTF_8)
        repeat(10_000) {
            current = MessageDigest.getInstance("SHA-256").digest(current)
        }
        return toHex(current)
    }

    private fun toHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }
}

class SettingsRepository(context: Context) {

    private val store: DataStore<Preferences> = context.applicationContext.settingsDataStore

    val settings: Flow<AppSettings> = store.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { p ->
            AppSettings(
                loaded = true,
                language = p[Keys.LANGUAGE] ?: "ur",
                onboardingDone = p[Keys.ONBOARDING_DONE] ?: false,
                name = p[Keys.NAME] ?: "",
                level = p[Keys.LEVEL] ?: "basic",
                pinEnabled = p[Keys.PIN_HASH] != null,
                theme = p[Keys.THEME] ?: "system",
                textScalePercent = p[Keys.TEXT_SCALE] ?: 100,
                contentLanguage = p[Keys.CONTENT_LANGUAGE] ?: "",
                reminderEnabled = p[Keys.REMINDER_ENABLED] ?: false,
                reminderHour = p[Keys.REMINDER_HOUR] ?: 20,
                reminderMinute = p[Keys.REMINDER_MINUTE] ?: 0
            )
        }

    val studyDays: Flow<Set<Long>> = store.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { p ->
            val out = HashSet<Long>()
            for (text in (p[Keys.STUDY_DAYS] ?: emptySet())) {
                val v = text.toLongOrNull()
                if (v != null) out.add(v)
            }
            out
        }

    suspend fun setLanguage(language: String) {
        store.edit { p -> p[Keys.LANGUAGE] = language }
    }

    suspend fun setLevel(level: String) {
        store.edit { p -> p[Keys.LEVEL] = level }
    }

    suspend fun setTheme(theme: String) {
        store.edit { p -> p[Keys.THEME] = theme }
    }

    suspend fun setTextScale(percent: Int) {
        store.edit { p -> p[Keys.TEXT_SCALE] = percent }
    }

    suspend fun setContentLanguage(language: String) {
        store.edit { p -> p[Keys.CONTENT_LANGUAGE] = language }
    }

    suspend fun setName(name: String) {
        store.edit { p -> p[Keys.NAME] = name }
    }

    suspend fun setReminder(enabled: Boolean, hour: Int, minute: Int) {
        store.edit { p ->
            p[Keys.REMINDER_ENABLED] = enabled
            p[Keys.REMINDER_HOUR] = hour
            p[Keys.REMINDER_MINUTE] = minute
        }
    }

    /** Remembers that the learner studied today (lesson opened or quiz finished). */
    suspend fun recordStudyDay() {
        val today = localEpochDay().toString()
        store.edit { p ->
            val current = p[Keys.STUDY_DAYS] ?: emptySet()
            if (!current.contains(today)) {
                val updated = (current + today).sortedBy { d -> d.toLongOrNull() ?: 0L }.takeLast(400)
                p[Keys.STUDY_DAYS] = updated.toSet()
            }
        }
    }

    suspend fun clearStudyDays() {
        store.edit { p -> p.remove(Keys.STUDY_DAYS) }
    }

    /**
     * Changes or removes the PIN. oldPin must match when a PIN already exists.
     * newPin = null removes the PIN. Returns false when oldPin is wrong.
     */
    suspend fun changePin(oldPin: String?, newPin: String?): Boolean {
        val current = store.data.first()
        val salt = current[Keys.PIN_SALT]
        val hash = current[Keys.PIN_HASH]
        if (salt != null && hash != null) {
            if (oldPin == null || PinHasher.hash(oldPin, salt) != hash) return false
        }
        store.edit { p ->
            if (newPin == null) {
                p.remove(Keys.PIN_SALT)
                p.remove(Keys.PIN_HASH)
            } else {
                val newSalt = PinHasher.newSalt()
                p[Keys.PIN_SALT] = newSalt
                p[Keys.PIN_HASH] = PinHasher.hash(newPin, newSalt)
            }
        }
        return true
    }

    /** Settings that go into a backup file (the PIN is never included). */
    suspend fun exportSettings(): JSONObject {
        val p = store.data.first()
        val o = JSONObject()
        o.put("language", p[Keys.LANGUAGE] ?: "ur")
        o.put("name", p[Keys.NAME] ?: "")
        o.put("level", p[Keys.LEVEL] ?: "basic")
        o.put("theme", p[Keys.THEME] ?: "system")
        o.put("textScalePercent", p[Keys.TEXT_SCALE] ?: 100)
        o.put("contentLanguage", p[Keys.CONTENT_LANGUAGE] ?: "")
        o.put("reminderEnabled", p[Keys.REMINDER_ENABLED] ?: false)
        o.put("reminderHour", p[Keys.REMINDER_HOUR] ?: 20)
        o.put("reminderMinute", p[Keys.REMINDER_MINUTE] ?: 0)
        return o
    }

    suspend fun importSettings(o: JSONObject?, days: Set<Long>) {
        store.edit { p ->
            if (o != null) {
                val lang = o.optString("language", "")
                if (lang == "ur" || lang == "en") p[Keys.LANGUAGE] = lang
                p[Keys.NAME] = o.optString("name", p[Keys.NAME] ?: "")
                val level = o.optString("level", "")
                if (level == "basic" || level == "intermediate" || level == "advanced") p[Keys.LEVEL] = level
                val theme = o.optString("theme", "")
                if (theme == "system" || theme == "light" || theme == "dark") p[Keys.THEME] = theme
                val scale = o.optInt("textScalePercent", 100)
                p[Keys.TEXT_SCALE] = if (scale < 80) 80 else if (scale > 160) 160 else scale
                val content = o.optString("contentLanguage", "")
                if (content == "" || content == "ur" || content == "en" || content == "both") {
                    p[Keys.CONTENT_LANGUAGE] = content
                }
                p[Keys.REMINDER_ENABLED] = o.optBoolean("reminderEnabled", false)
                val hour = o.optInt("reminderHour", 20)
                val minute = o.optInt("reminderMinute", 0)
                p[Keys.REMINDER_HOUR] = if (hour in 0..23) hour else 20
                p[Keys.REMINDER_MINUTE] = if (minute in 0..59) minute else 0
            }
            val texts = HashSet<String>()
            for (d in days) texts.add(d.toString())
            p[Keys.STUDY_DAYS] = texts
        }
    }

    suspend fun completeOnboarding(name: String, level: String, pin: String?) {
        store.edit { p ->
            p[Keys.NAME] = name
            p[Keys.LEVEL] = level
            if (pin != null) {
                val salt = PinHasher.newSalt()
                p[Keys.PIN_SALT] = salt
                p[Keys.PIN_HASH] = PinHasher.hash(pin, salt)
            } else {
                p.remove(Keys.PIN_SALT)
                p.remove(Keys.PIN_HASH)
            }
            p[Keys.ONBOARDING_DONE] = true
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val p = store.data.first()
        val salt = p[Keys.PIN_SALT]
        val hash = p[Keys.PIN_HASH]
        if (salt == null || hash == null) return true
        return PinHasher.hash(pin, salt) == hash
    }
}
