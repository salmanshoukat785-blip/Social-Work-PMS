package com.socialwork.academy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.socialwork.academy.data.ContentState
import com.socialwork.academy.data.Topic
import com.socialwork.academy.data.TopicSection
import com.socialwork.academy.ui.AppStrings

/**
 * Phase 4: shows the topic's heading, the completed switch and the study
 * material taken from the Word file (English and Urdu lists, as in the file).
 * Phase 5 turns this into the full notes reader (font size, search, bookmarks).
 */
@Composable
fun TopicDetailScreen(
    strings: AppStrings,
    isUrdu: Boolean,
    topic: Topic,
    content: ContentState,
    completed: Boolean,
    onToggleCompleted: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val paperLabel = if (topic.paper == 2) strings.paper2 else strings.paper1

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
            }
            Text(
                text = paperLabel,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Column(
            modifier = Modifier.padding(start = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = topic.title(isUrdu), style = MaterialTheme.typography.headlineMedium)
            Text(
                text = topic.otherTitle(isUrdu),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            modifier = Modifier.padding(start = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (completed) {
                OutlinedButton(
                    onClick = { onToggleCompleted(false) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.markNotCompleted)
                }
            } else {
                Button(
                    onClick = { onToggleCompleted(true) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.markCompleted)
                }
            }

            when (content) {
                ContentState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                ContentState.Failed -> InfoCard(text = strings.contentError)

                is ContentState.Ready -> {
                    val sections = content.bundle.forTopic(topic.id)?.sections.orEmpty()
                    if (sections.isEmpty()) {
                        InfoCard(text = strings.noSourcePoints)
                    } else {
                        Text(
                            text = strings.sourceLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        sections.forEach { section ->
                            SectionCard(section = section, isUrdu = isUrdu)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(section: TopicSection, isUrdu: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val heading = section.heading(isUrdu)
            val otherHeading = section.otherHeading(isUrdu)
            if (heading.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = heading, style = MaterialTheme.typography.titleMedium)
                    if (otherHeading.isNotBlank()) {
                        Text(
                            text = otherHeading,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // The selected language comes first; both lists are shown as they are in the file.
            if (isUrdu) {
                LanguageBlock(label = "اردو", items = section.itemsUr, rtl = true)
                LanguageBlock(label = "English", items = section.itemsEn, rtl = false)
            } else {
                LanguageBlock(label = "English", items = section.itemsEn, rtl = false)
                LanguageBlock(label = "اردو", items = section.itemsUr, rtl = true)
            }
        }
    }
}

/** One language's list. Its own text direction, whatever the app language is. */
@Composable
private fun LanguageBlock(label: String, items: List<String>, rtl: Boolean) {
    if (items.isEmpty()) return
    val direction = if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            items.forEach { item ->
                Text(text = "• $item", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
