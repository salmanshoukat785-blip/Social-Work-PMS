package com.islamicstudies.academy.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.islamicstudies.academy.AcademyApp
import com.islamicstudies.academy.data.settings.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as AcademyApp
    private val repo = app.settingsRepository

    val settings: StateFlow<AppSettings> = repo.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    fun setLanguage(language: String) {
        viewModelScope.launch { repo.setLanguage(language) }
    }

    fun finish(name: String, level: String, pin: String?, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.completeOnboarding(name.trim(), level, pin)
            if (pin != null) app.sessionUnlocked = true
            onDone()
        }
    }
}
