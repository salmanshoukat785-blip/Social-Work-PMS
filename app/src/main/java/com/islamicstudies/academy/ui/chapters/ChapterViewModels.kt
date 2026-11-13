package com.islamicstudies.academy.ui.chapters

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.islamicstudies.academy.AcademyApp
import com.islamicstudies.academy.data.db.ChapterEntity
import com.islamicstudies.academy.data.db.ChapterSummary
import com.islamicstudies.academy.data.db.HeadingEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ChaptersViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = (application as AcademyApp).database.contentDao()

    val chapters: StateFlow<List<ChapterSummary>> = dao.observeChapterSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

class ChapterDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val dao = (application as AcademyApp).database.contentDao()
    private val chapterId: String = savedStateHandle.get<String>("chapterId") ?: ""

    val chapter: StateFlow<ChapterEntity?> = dao.observeChapter(chapterId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val headings: StateFlow<List<HeadingEntity>> = dao.observeHeadings(chapterId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val userDao = (application as AcademyApp).database.userDao()

    val bookmarkedIds: StateFlow<List<String>> = userDao.observeBookmarkIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedIds: StateFlow<List<String>> = userDao.observeCompletedIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
