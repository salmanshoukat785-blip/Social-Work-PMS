package com.socialwork.academy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.socialwork.academy.data.TopicCatalog
import com.socialwork.academy.data.progressFor
import com.socialwork.academy.ui.AppStrings

/**
 * Dashboard. Every number comes from the topic list and from the topics the
 * user has marked as completed on this phone. Nothing here is made up.
 */
@Composable
fun HomeScreen(
    strings: AppStrings,
    isUrdu: Boolean,
    profileName: String,
    completedTopics: Set<String>,
    lastTopicId: String?,
    onOpenPaper: (Int) -> Unit,
    onOpenTopic: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val overall = progressFor(TopicCatalog.all, completedTopics)
    val paper1 = progressFor(TopicCatalog.paper1, completedTopics)
    val paper2 = progressFor(TopicCatalog.paper2, completedTopics)
    val lastTopic = TopicCatalog.byId(lastTopicId)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeroCard(
            title = if (profileName.isNotBlank()) strings.welcomeName(profileName) else strings.welcome,
            subtitle = strings.tagline
        )

        // Continue learning shortcut (only when the user has opened a topic before)
        if (lastTopic != null) {
            PaperCard(
                title = strings.continueLearning,
                subtitle = lastTopic.title(isUrdu),
                icon = Icons.Filled.PlayArrow,
                onClick = { onOpenTopic(lastTopic.id) }
            )
        } else {
            InfoCard(text = strings.noProgressYet)
        }

        // Numbers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatTile(value = "2", label = strings.statPapers, modifier = Modifier.weight(1f))
            StatTile(value = overall.total.toString(), label = strings.statTopics, modifier = Modifier.weight(1f))
            StatTile(value = overall.completed.toString(), label = strings.statCompleted, modifier = Modifier.weight(1f))
            StatTile(value = overall.remaining.toString(), label = strings.statRemaining, modifier = Modifier.weight(1f))
        }

        // Overall progress
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = strings.progressTitle, style = MaterialTheme.typography.titleMedium)
                LinearProgressIndicator(
                    progress = { overall.fraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Text(
                    text = strings.progressSummary(overall.completed, overall.total),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

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
        InfoCard(text = strings.offlineNote)
    }
}
