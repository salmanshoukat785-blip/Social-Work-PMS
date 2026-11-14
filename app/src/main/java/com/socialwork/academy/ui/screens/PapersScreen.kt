package com.socialwork.academy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.socialwork.academy.data.TopicCatalog
import com.socialwork.academy.data.progressFor
import com.socialwork.academy.ui.AppStrings

@Composable
fun PapersScreen(
    strings: AppStrings,
    completedTopics: Set<String>,
    bookmarkCount: Int,
    onOpenPaper: (Int) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenBookmarks: () -> Unit,
    modifier: Modifier = Modifier
) {
    val paper1 = progressFor(TopicCatalog.paper1, completedTopics)
    val paper2 = progressFor(TopicCatalog.paper2, completedTopics)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = strings.papersTitle, style = MaterialTheme.typography.titleLarge, modifier = Modifier.asHeading())
        QuickActions(
            strings = strings,
            bookmarkCount = bookmarkCount,
            onOpenSearch = onOpenSearch,
            onOpenBookmarks = onOpenBookmarks
        )
        PaperCard(
            title = strings.paper1,
            subtitle = strings.paper1Sub + "\n" + strings.paperSummary(paper1.total, paper1.completed),
            icon = Icons.Filled.AccountBalance,
            onClick = { onOpenPaper(1) }
        )
        PaperCard(
            title = strings.paper2,
            subtitle = strings.paper2Sub + "\n" + strings.paperSummary(paper2.total, paper2.completed),
            icon = Icons.AutoMirrored.Filled.MenuBook,
            onClick = { onOpenPaper(2) }
        )
    }
}
