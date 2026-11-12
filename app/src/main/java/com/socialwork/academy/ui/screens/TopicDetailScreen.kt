package com.socialwork.academy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.socialwork.academy.data.Topic
import com.socialwork.academy.ui.AppStrings

/**
 * Phase 3: heading, both-language titles and the completed switch.
 * The notes themselves arrive in Phase 4 (content) and Phase 5 (reader).
 */
@Composable
fun TopicDetailScreen(
    strings: AppStrings,
    isUrdu: Boolean,
    topic: Topic,
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
            InfoCard(text = strings.topicNotesLater)
        }
    }
}
