package com.islamicstudies.academy.ui.lesson

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.islamicstudies.academy.AcademyApp
import com.islamicstudies.academy.data.db.BookmarkEntity
import com.islamicstudies.academy.data.db.ChapterEntity
import com.islamicstudies.academy.data.db.HeadingEntity
import com.islamicstudies.academy.data.db.LessonSectionEntity
import com.islamicstudies.academy.data.db.PersonalNoteEntity
import com.islamicstudies.academy.data.db.UserProgressEntity
import com.islamicstudies.academy.data.settings.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LessonViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val app = application as AcademyApp
    private val contentDao = app.database.contentDao()
    private val userDao = app.database.userDao()
    private val repo = app.settingsRepository

    val headingId: String = savedStateHandle.get<String>("headingId") ?: ""

    // Heading ids look like "ch06-s02-t03": the chapter id is the part before the first "-".
    private val chapterId: String = headingId.substringBefore("-")

    private val keepAlive = SharingStarted.WhileSubscribed(5000)

    val heading: StateFlow<HeadingEntity?> = contentDao.observeHeading(headingId)
        .stateIn(viewModelScope, keepAlive, null)

    val chapter: StateFlow<ChapterEntity?> = contentDao.observeChapter(chapterId)
        .stateIn(viewModelScope, keepAlive, null)

    val siblings: StateFlow<List<HeadingEntity>> = contentDao.observeHeadings(chapterId)
        .stateIn(viewModelScope, keepAlive, emptyList())

    val sections: StateFlow<List<LessonSectionEntity>> = contentDao.observeSections(headingId)
        .stateIn(viewModelScope, keepAlive, emptyList())

    val bookmarked: StateFlow<Boolean> = userDao.observeBookmarkCount(headingId)
        .map { n -> n > 0 }
        .stateIn(viewModelScope, keepAlive, false)

    val completed: StateFlow<Boolean> = userDao.observeCompletedFlag(headingId)
        .map { n -> n > 0 }
        .stateIn(viewModelScope, keepAlive, false)

    val note: StateFlow<String> = userDao.observeNote(headingId)
        .map { text -> text ?: "" }
        .stateIn(viewModelScope, keepAlive, "")

    val settings: StateFlow<AppSettings> = repo.settings
        .stateIn(viewModelScope, keepAlive, AppSettings())

    init {
        // Remember the last opened lesson (used by "Continue where you left off").
        viewModelScope.launch {
            repo.recordStudyDay()
            val existing = userDao.getProgress(headingId)
            userDao.upsertProgress(
                UserProgressEntity(
                    headingId = headingId,
                    completed = existing?.completed ?: false,
                    lastOpenedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun toggleBookmark() {
        viewModelScope.launch {
            if (bookmarked.value) {
                userDao.removeBookmark(headingId)
            } else {
                userDao.addBookmark(BookmarkEntity(headingId, System.currentTimeMillis()))
            }
        }
    }

    fun saveNote(text: String) {
        viewModelScope.launch {
            val trimmed = text.trim()
            if (trimmed.isEmpty()) {
                userDao.deleteNote(headingId)
            } else {
                userDao.upsertNote(PersonalNoteEntity(headingId, trimmed, System.currentTimeMillis()))
            }
        }
    }

    fun toggleCompleted() {
        viewModelScope.launch {
            userDao.upsertProgress(
                UserProgressEntity(headingId, !completed.value, System.currentTimeMillis())
            )
        }
    }

    fun setLevel(level: String) {
        viewModelScope.launch { repo.setLevel(level) }
    }

    fun setContentLanguage(language: String) {
        viewModelScope.launch { repo.setContentLanguage(language) }
    }

    fun setTextScale(percent: Int) {
        viewModelScope.launch { repo.setTextScale(percent) }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch { repo.setTheme(theme) }
    }
}
