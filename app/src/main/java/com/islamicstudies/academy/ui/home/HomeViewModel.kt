package com.islamicstudies.academy.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.islamicstudies.academy.AcademyApp
import com.islamicstudies.academy.data.db.SavedItem
import com.islamicstudies.academy.data.settings.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUi(
    val settings: AppSettings = AppSettings(),
    val completed: Int = 0,
    val total: Int = 0,
    val lastOpened: SavedItem? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as AcademyApp
    private val repo = app.settingsRepository
    private val userDao = app.database.userDao()

    val ui: StateFlow<HomeUi> = combine(
        repo.settings,
        userDao.observeCompletedCount(),
        userDao.observeTopicCount(),
        userDao.observeLastOpened()
    ) { settings, completed, total, last ->
        HomeUi(settings = settings, completed = completed, total = total, lastOpened = last)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUi())

    fun toggleLanguage() {
        viewModelScope.launch {
            val next = if (ui.value.settings.language == "en") "ur" else "en"
            repo.setLanguage(next)
        }
    }
}
