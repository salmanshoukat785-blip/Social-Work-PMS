package com.socialwork.academy.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.socialwork.academy.data.SettingsRepository
import com.socialwork.academy.data.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    /** null while the saved settings are still being read from the phone. */
    val settings: StateFlow<UserSettings?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setUrdu(value: Boolean) {
        viewModelScope.launch { repository.setUrdu(value) }
    }

    fun setDarkMode(value: Boolean) {
        viewModelScope.launch { repository.setDarkMode(value) }
    }

    fun saveProfile(name: String, institution: String) {
        viewModelScope.launch { repository.saveProfile(name, institution) }
    }

    fun finishSetupWithoutProfile() {
        viewModelScope.launch { repository.finishSetupWithoutProfile() }
    }
}
