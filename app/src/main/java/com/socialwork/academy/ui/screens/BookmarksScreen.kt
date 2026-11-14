package com.socialwork.academy.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.socialwork.academy.data.TopicCatalog
import com.socialwork.academy.ui.AppStrings

/** Topics the user has bookmarked, in the order of the topic list. */
@Composable
fun BookmarksScreen(
    strings: AppStrings,
    isUrdu: Boolean,
    bookmarks: Set<String>,
    onOpenTopic: (String) -> Unit,
    onRemoveBookmark: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val saved = TopicCatalog.all.filter { it.id in bookmarks }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 16.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
            }
            Text(text = strings.bookmarksTitle, style = MaterialTheme.typography.titleLarge, modifier = Modifier.asHeading())
        }

        if (saved.isEmpty()) {
            InfoCard(text = strings.noBookmarks, modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(saved, key = { it.id }) { topic ->
                    val paperLabel = if (topic.paper == 2) strings.paper2 else strings.paper1
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenTopic(topic.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = paperLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(text = topic.title(isUrdu), style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = topic.otherTitle(isUrdu),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onRemoveBookmark(topic.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.Bookmark,
                                    contentDescription = strings.bookmarkRemove,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
