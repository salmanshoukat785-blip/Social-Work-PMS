package com.islamicstudies.academy.ui.saved

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.islamicstudies.academy.AcademyApp
import com.islamicstudies.academy.data.db.NoteItem
import com.islamicstudies.academy.data.db.SavedItem
import com.islamicstudies.academy.ui.LocalAppLanguage
import com.islamicstudies.academy.ui.LocalStrings
import com.islamicstudies.academy.ui.isEnglishPrimary
import com.islamicstudies.academy.ui.pickTitle
import com.islamicstudies.academy.ui.screenTitleStyle
import com.islamicstudies.academy.ui.theme.UrduText
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SavedViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = (application as AcademyApp).database.userDao()

    val bookmarks: StateFlow<List<SavedItem>> = userDao.observeBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteItem>> = userDao.observeNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

private data class SavedRow(
    val headingId: String,
    val titleUr: String,
    val titleEn: String,
    val chapterUr: String,
    val chapterEn: String,
    val extra: String
)

@Composable
fun BookmarksScreen(
    onBack: () -> Unit,
    onOpenLesson: (String) -> Unit,
    viewModel: SavedViewModel = viewModel()
) {
    val s = LocalStrings.current
    val items by viewModel.bookmarks.collectAsStateWithLifecycle()
    val rows = items.map { i -> SavedRow(i.headingId, i.titleUr, i.titleEn, i.chapterTitleUr, i.chapterTitleEn, "") }
    SavedList(
        title = s.tileBookmarks,
        emptyText = s.emptyBookmarks,
        rows = rows,
        onBack = onBack,
        onOpenLesson = onOpenLesson
    )
}

@Composable
fun NotesScreen(
    onBack: () -> Unit,
    onOpenLesson: (String) -> Unit,
    viewModel: SavedViewModel = viewModel()
) {
    val s = LocalStrings.current
    val items by viewModel.notes.collectAsStateWithLifecycle()
    val rows = items.map { i ->
        SavedRow(i.headingId, i.titleUr, i.titleEn, i.chapterTitleUr, i.chapterTitleEn, i.noteText)
    }
    SavedList(
        title = s.tileNotes,
        emptyText = s.emptyNotes,
        rows = rows,
        onBack = onBack,
        onOpenLesson = onOpenLesson
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedList(
    title: String,
    emptyText: String,
    rows: List<SavedRow>,
    onBack: () -> Unit,
    onOpenLesson: (String) -> Unit
) {
    val s = LocalStrings.current
    val uiLang = LocalAppLanguage.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title, style = screenTitleStyle()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = s.back
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (rows.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rows, key = { r -> r.headingId }) { row ->
                    val englishFirst = isEnglishPrimary(row.titleEn, uiLang)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenLesson(row.headingId) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = pickTitle(row.titleUr, row.titleEn, uiLang),
                                style = if (englishFirst) MaterialTheme.typography.titleMedium else UrduText.subheading,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = pickTitle(row.chapterUr, row.chapterEn, uiLang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (row.extra.isNotBlank()) {
                                Text(
                                    text = row.extra,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
