package com.islamicstudies.academy.ui.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.islamicstudies.academy.AcademyApp
import com.islamicstudies.academy.data.backup.BackupManager
import com.islamicstudies.academy.data.settings.AppSettings
import com.islamicstudies.academy.reminder.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AcademyApp
    private val repo = app.settingsRepository
    private val db = app.database

    val settings: StateFlow<AppSettings> = repo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    /** One-shot message codes shown by the screen as a snackbar. */
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun setUiLanguage(language: String) {
        viewModelScope.launch { repo.setLanguage(language) }
    }

    fun setContentLanguage(language: String) {
        viewModelScope.launch { repo.setContentLanguage(language) }
    }

    fun setLevel(level: String) {
        viewModelScope.launch { repo.setLevel(level) }
    }

    fun setTextScale(percent: Int) {
        viewModelScope.launch { repo.setTextScale(percent) }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch { repo.setTheme(theme) }
    }

    fun saveName(name: String) {
        viewModelScope.launch { repo.setName(name.trim()) }
    }

    // ---- reminder ----

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            repo.setReminder(enabled, current.reminderHour, current.reminderMinute)
            if (enabled) {
                ReminderScheduler.ensureChannel(app)
                ReminderScheduler.schedule(app, current.reminderHour, current.reminderMinute)
            } else {
                ReminderScheduler.cancel(app)
            }
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            val enabled = settings.value.reminderEnabled
            repo.setReminder(enabled, hour, minute)
            if (enabled) ReminderScheduler.schedule(app, hour, minute)
        }
    }

    fun reportNotificationsDenied() {
        _message.value = "notifications_denied"
    }

    // ---- PIN ----

    fun changePin(oldPin: String?, newPin: String) {
        viewModelScope.launch {
            val ok = repo.changePin(oldPin, newPin)
            if (ok) {
                app.sessionUnlocked = true
                _message.value = "pin_changed"
            } else {
                _message.value = "pin_wrong"
            }
        }
    }

    fun removePin(oldPin: String) {
        viewModelScope.launch {
            val ok = repo.changePin(oldPin, null)
            _message.value = if (ok) "pin_removed" else "pin_wrong"
        }
    }

    // ---- backup / restore / reset ----

    fun backupTo(uri: Uri) {
        viewModelScope.launch {
            val ok = try {
                val text = BackupManager.createBackup(db, repo)
                withContext(Dispatchers.IO) {
                    val out = app.contentResolver.openOutputStream(uri)
                        ?: throw IOException("No output stream")
                    out.use { stream -> stream.write(text.toByteArray(Charsets.UTF_8)) }
                }
                true
            } catch (e: Exception) {
                false
            }
            _message.value = if (ok) "backup_ok" else "backup_failed"
        }
    }

    fun restoreFrom(uri: Uri) {
        viewModelScope.launch {
            val text: String? = try {
                withContext(Dispatchers.IO) {
                    app.contentResolver.openInputStream(uri)
                        ?.bufferedReader(Charsets.UTF_8)
                        ?.use { reader -> reader.readText() }
                }
            } catch (e: Exception) {
                null
            }
            val ok = if (text == null) false else BackupManager.restoreBackup(text, db, repo)
            if (ok) {
                val restored = repo.settings.first()
                if (restored.reminderEnabled) {
                    ReminderScheduler.schedule(app, restored.reminderHour, restored.reminderMinute)
                } else {
                    ReminderScheduler.cancel(app)
                }
            }
            _message.value = if (ok) "restore_ok" else "restore_failed"
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            db.withTransaction {
                db.userDao().clearProgress()
                db.quizDao().clearAttempts()
            }
            repo.clearStudyDays()
            _message.value = "reset_done"
        }
    }
}
