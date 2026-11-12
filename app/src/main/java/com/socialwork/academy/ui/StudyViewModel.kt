package com.socialwork.academy.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.socialwork.academy.data.GlossaryRepository
import com.socialwork.academy.data.GlossaryState
import com.socialwork.academy.data.StudyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Personal notes, glossary and flashcard progress (all stored on the phone). */
class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val store = StudyStore(application)

    val notes: StateFlow<Map<String, String>> =
        store.notes.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val knownCards: StateFlow<Set<String>> =
        store.knownCards.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    private val _glossary = MutableStateFlow<GlossaryState>(GlossaryState.Loading)
    val glossary: StateFlow<GlossaryState> = _glossary.asStateFlow()

    init {
        viewModelScope.launch {
            val bundle = withContext(Dispatchers.IO) { GlossaryRepository.load(application) }
            _glossary.value = if (bundle != null) GlossaryState.Ready(bundle) else GlossaryState.Failed
        }
    }

    fun saveNote(topicId: String, text: String) {
        viewModelScope.launch { store.setNote(topicId, text) }
    }

    fun setKnown(cardId: String, known: Boolean) {
        viewModelScope.launch { store.setKnown(cardId, known) }
    }

    fun clearKnown() {
        viewModelScope.launch { store.clearKnown() }
    }
}
