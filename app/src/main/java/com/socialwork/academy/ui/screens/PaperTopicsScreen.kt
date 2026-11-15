package com.socialwork.academy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.socialwork.academy.data.Chapter
import com.socialwork.academy.data.ChapterCatalog
import com.socialwork.academy.data.Topic
import com.socialwork.academy.data.TopicCatalog
import com.socialwork.academy.ui.AppStrings
import com.socialwork.academy.ui.theme.HierarchyColors
import com.socialwork.academy.ui.theme.hierarchyColors

/** The chapters of one paper; each chapter lists its headings (topics) under it. */
@Composable
fun PaperTopicsScreen(
    strings: AppStrings,
    isUrdu: Boolean,
    paperId: Int,
    completedTopics: Set<String>,
    bookmarks: Set<String>,
    onOpenTopic: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = if (paperId == 2) strings.paper2 else strings.paper1
    val chapters = ChapterCatalog.forPaper(paperId)
    val colors = hierarchyColors()

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 16.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
            }
            Text(text = title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.asHeading())
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            itemsIndexed(chapters, key = { _, chapter -> chapter.key }) { index, chapter ->
                ChapterBlock(
                    number = index + 1,
                    chapter = chapter,
                    isUrdu = isUrdu,
                    colors = colors,
                    completedTopics = completedTopics,
                    bookmarks = bookmarks,
                    onOpenTopic = onOpenTopic
                )
            }
        }
    }
}

@Composable
private fun ChapterBlock(
    number: Int,
    chapter: Chapter,
    isUrdu: Boolean,
    colors: HierarchyColors,
    completedTopics: Set<String>,
    bookmarks: Set<String>,
    onOpenTopic: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val topicId = chapter.chapterTopicId
        // Chapter bar (strong colour). It opens the chapter's own notes when it has any.
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (topicId != null) Modifier.clickable { onOpenTopic(topicId) } else Modifier),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.chapter)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.onChapter),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.chapter
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = chapter.title(isUrdu),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onChapter
                    )
                }
                if (topicId != null && topicId in bookmarks) {
                    Icon(Icons.Filled.Bookmark, contentDescription = null, tint = colors.onChapter)
                }
                if (topicId != null && topicId in completedTopics) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = colors.onChapter)
                }
            }
        }

        // Headings of the chapter (soft colour), slightly indented.
        chapter.childTopicIds.forEach { id ->
            val topic = TopicCatalog.byId(id) ?: return@forEach
            HeadingRow(
                topic = topic,
                isUrdu = isUrdu,
                colors = colors,
                completed = id in completedTopics,
                bookmarked = id in bookmarks,
                onClick = { onOpenTopic(id) }
            )
        }
    }
}

@Composable
private fun HeadingRow(
    topic: Topic,
    isUrdu: Boolean,
    colors: HierarchyColors,
    completed: Boolean,
    bookmarked: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.heading)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = topic.title(isUrdu),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onHeading
                )
            }
            if (bookmarked) {
                Icon(Icons.Filled.Bookmark, contentDescription = null, tint = colors.onHeading)
            }
            if (completed) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = colors.onHeading)
            }
        }
    }
}
