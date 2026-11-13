package com.socialwork.academy.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.socialwork.academy.data.ActivityStore
import com.socialwork.academy.data.BackupData
import com.socialwork.academy.data.BackupJson
import com.socialwork.academy.data.DayActivity
import com.socialwork.academy.data.DayKey
import com.socialwork.academy.data.QuizStore
import com.socialwork.academy.data.SettingsRepository
import com.socialwork.academy.data.StudyStore
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

/** What happened the last time the student used backup, restore or reset. */
enum class DataStatus { BackupSaved, BackupFailed, RestoreDone, RestoreInvalid, RestoreFailed, ResetDone }

/** Study activity (for the weekly chart), backup and restore, and "reset progress". */
class DataViewModel(application: Application) : AndroidViewModel(application) {

    private val settings = SettingsRepository(application)
    private val quiz = QuizStore(application)
    private val study = StudyStore(application)
    private val activityStore = ActivityStore(application)

    val activity: StateFlow<Map<String, DayActivity>> =
        activityStore.activity.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val _status = MutableStateFlow<DataStatus?>(null)
    val status: StateFlow<DataStatus?> = _status.asStateFlow()

    fun clearStatus() {
        _status.value = null
    }

    fun recordTopicOpened(topicId: String) {
        viewModelScope.launch { activityStore.recordTopic(DayKey.of(System.currentTimeMillis()), topicId) }
    }

    fun backupTo(uri: Uri) {
        viewModelScope.launch {
            _status.value = try {
                val data = BackupData(
                    createdAtMillis = System.currentTimeMillis(),
                    settings = settings.settings.first(),
                    notes = study.notes.first(),
                    knownCards = study.knownCards.first(),
                    attempts = quiz.attempts.first(),
                    topicStats = quiz.topicStats.first(),
                    activity = activityStore.activity.first()
                )
                val bytes = BackupJson.encode(data).toByteArray(Charsets.UTF_8)
                withContext(Dispatchers.IO) {
                    val out = getApplication<Application>().contentResolver.openOutputStream(uri, "w")
                        ?: throw IOException("cannot open file")
                    out.use { it.write(bytes) }
                }
                DataStatus.BackupSaved
            } catch (e: Exception) {
                DataStatus.BackupFailed
            }
        }
    }

    fun restoreFrom(uri: Uri) {
        viewModelScope.launch {
            val text = try {
                withContext(Dispatchers.IO) {
                    val input = getApplication<Application>().contentResolver.openInputStream(uri)
                        ?: throw IOException("cannot open file")
                    input.use { stream ->
                        val bytes = stream.readBytes()
                        if (bytes.size > MAX_BACKUP_BYTES) throw IOException("file too large")
                        String(bytes, Charsets.UTF_8)
                    }
                }
            } catch (e: Exception) {
                _status.value = DataStatus.RestoreFailed
                return@launch
            }
            val data = BackupJson.decode(text)
            if (data == null) {
                _status.value = DataStatus.RestoreInvalid
                return@launch
            }
            _status.value = try {
                settings.restore(data.settings)
                study.replaceAll(data.notes, data.knownCards)
                quiz.replaceAll(data.attempts, data.topicStats)
                activityStore.replaceAll(data.activity)
                DataStatus.RestoreDone
            } catch (e: Exception) {
                DataStatus.RestoreFailed
            }
        }
    }

    /** Clears completed topics, quiz history and accuracy, activity and flashcard progress. */
    fun resetProgress() {
        viewModelScope.launch {
            settings.resetProgress()
            quiz.clear()
            activityStore.clear()
            study.clearKnown()
            _status.value = DataStatus.ResetDone
        }
    }

    private companion object {
        const val MAX_BACKUP_BYTES = 8 * 1024 * 1024
    }
}
