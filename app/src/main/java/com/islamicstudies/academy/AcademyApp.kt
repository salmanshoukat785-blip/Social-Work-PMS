package com.islamicstudies.academy

import android.app.Application
import com.islamicstudies.academy.data.db.AppDatabase
import com.islamicstudies.academy.data.importer.ContentImporter
import com.islamicstudies.academy.data.importer.OutlineImporter
import com.islamicstudies.academy.data.importer.QuestionImporter
import com.islamicstudies.academy.data.settings.SettingsRepository
import com.islamicstudies.academy.reminder.ReminderScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AcademyApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.build(this) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }

    /** True after the PIN was entered once in this app process. */
    var sessionUnlocked: Boolean = false

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _importState = MutableStateFlow<ImportState>(ImportState.Loading)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            try {
                val settings = settingsRepository.settings.first()
                if (settings.reminderEnabled) {
                    ReminderScheduler.schedule(applicationContext, settings.reminderHour, settings.reminderMinute)
                }
            } catch (e: Exception) {
                // reminders are optional; ignore
            }
        }
        appScope.launch {
            try {
                OutlineImporter.importIfNeeded(applicationContext, database)
                ContentImporter.importIfNeeded(applicationContext, database)
                QuestionImporter.importIfNeeded(applicationContext, database)
                _importState.value = ImportState.Ready
            } catch (e: Exception) {
                _importState.value = ImportState.Failed(e.message ?: e.javaClass.simpleName)
            }
        }
    }
}
