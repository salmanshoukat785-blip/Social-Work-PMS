package com.islamicstudies.academy.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.islamicstudies.academy.AcademyApp
import com.islamicstudies.academy.data.settings.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AppViewModel(application: Application) : AndroidViewModel(application) {
    val settings: StateFlow<AppSettings> = (application as AcademyApp).settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())
}
