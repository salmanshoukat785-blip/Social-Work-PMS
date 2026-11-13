package com.islamicstudies.academy.ui.chapters

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.islamicstudies.academy.data.db.HeadingEntity
import com.islamicstudies.academy.ui.LocalAppLanguage
import com.islamicstudies.academy.ui.LocalStrings
import com.islamicstudies.academy.ui.isEnglishPrimary
import com.islamicstudies.academy.ui.pickTitle
import com.islamicstudies.academy.ui.screenTitleStyle
import com.islamicstudies.academy.ui.theme.UrduText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterDetailScreen(
    onBack: () -> Unit,
    onOpenLesson: (String) -> Unit,
    viewModel: ChapterDetailViewModel = viewModel()
) {
    val strings = LocalStrings.current
    val uiLang = LocalAppLanguage.current
    val chapter by viewModel.chapter.collectAsStateWithLifecycle()
    val headings by viewModel.headings.collectAsStateWithLifecycle()
    val bookmarkedList by viewModel.bookmarkedIds.collectAsStateWithLifecycle()
    val completedList by viewModel.completedIds.collectAsStateWithLifecycle()
    val bookmarked = remember(bookmarkedList) { bookmarkedList.toSet() }
    val completed = remember(completedList) { completedList.toSet() }

    val titleText = pickTitle(chapter?.titleUr.orEmpty(), chapter?.titleEn.orEmpty(), uiLang)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = titleText,
                        style = screenTitleStyle(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.back
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            items(headings, key = { h -> h.id }) { heading ->
                HeadingRow(
                    h = heading,
                    isBookmarked = bookmarked.contains(heading.id),
                    isCompleted = completed.contains(heading.id),
                    onClick = { onOpenLesson(heading.id) }
                )
            }
        }
    }
}

@Composable
private fun Marks(isBookmarked: Boolean, isCompleted: Boolean) {
    if (isBookmarked) {
        Text(text = "⭐")
    }
    if (isCompleted) {
        Text(
            text = "✓",
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun HeadingRow(
    h: HeadingEntity,
    isBookmarked: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val uiLang = LocalAppLanguage.current
    val englishFirst = isEnglishPrimary(h.titleEn, uiLang)
    val primary = if (englishFirst) h.titleEn else h.titleUr
    val secondary = if (englishFirst) h.titleUr else h.titleEn
    val indent = ((h.depth - 1).coerceAtLeast(0) * 16).dp

    if (h.type == "subheading") {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = indent, top = 14.dp, bottom = 4.dp)
                .clickable(onClick = onClick)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = primary,
                        style = if (englishFirst) MaterialTheme.typography.titleMedium else UrduText.subheading,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (secondary.isNotBlank()) {
                        Text(
                            text = secondary,
                            style = if (englishFirst) UrduText.body else MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Marks(isBookmarked = isBookmarked, isCompleted = isCompleted)
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(start = indent + 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(
                modifier = Modifier
                    .size(6.dp)
                    .background(MaterialTheme.colorScheme.tertiary, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = primary,
                    style = if (englishFirst) MaterialTheme.typography.bodyLarge else UrduText.body,
                    modifier = Modifier.fillMaxWidth()
                )
                if (secondary.isNotBlank()) {
                    Text(
                        text = secondary,
                        style = if (englishFirst) UrduText.body else MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Marks(isBookmarked = isBookmarked, isCompleted = isCompleted)
        }
    }
}
