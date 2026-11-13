package com.islamicstudies.academy.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.islamicstudies.academy.AcademyApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Fires at the reminder time: shows the notification and schedules the next day. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val app = context.applicationContext as AcademyApp
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val settings = app.settingsRepository.settings.first()
                if (settings.reminderEnabled) {
                    ReminderScheduler.showNotification(app, settings.language)
                    ReminderScheduler.schedule(app, settings.reminderHour, settings.reminderMinute)
                }
            } finally {
                pending.finish()
            }
        }
    }
}

/** After a phone restart or an app update the alarm is gone, so schedule it again. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val app = context.applicationContext as AcademyApp
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val settings = app.settingsRepository.settings.first()
                if (settings.reminderEnabled) {
                    ReminderScheduler.schedule(app, settings.reminderHour, settings.reminderMinute)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
