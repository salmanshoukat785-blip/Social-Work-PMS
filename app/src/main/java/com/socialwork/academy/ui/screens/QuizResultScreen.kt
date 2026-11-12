package com.socialwork.academy.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.socialwork.academy.data.QuizSession
import com.socialwork.academy.data.TopicCatalog
import com.socialwork.academy.ui.AppStrings
import com.socialwork.academy.ui.QuizStrings

@Composable
fun QuizResultScreen(
    strings: AppStrings,
    qs: QuizStrings,
    isUrdu: Boolean,
    session: QuizSession?,
    onReview: () -> Unit,
    onRetryIncorrect: () -> Unit,
    onDone: () -> Unit
) {
    if (session == null) {
        // Nothing to show (e.g. the app was restarted): go back to Practice.
        LaunchedEffect(Unit) { onDone() }
        return
    }
    // A new quiz (retry) has started while this screen is still animating away.
    if (!session.isFinished) return
    BackHandler { onDone() }

    val result = remember(session) { session.result() }
    val topics = remember(session) {
        result.byTopic.entries.sortedWith(compareBy({ it.value.percent }, { it.key }))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = if (session.config.mock) qs.mockTag + " - " + qs.resultTitle else qs.resultTitle,
                style = MaterialTheme.typography.titleLarge
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "${result.percent}%",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Text(
                        text = qs.score(result.correct, result.total),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("${qs.correctLabel}: ${result.correct}")
                    Text("${qs.wrongLabel}: ${result.wrong}")
                    Text("${qs.unansweredLabel}: ${result.unanswered}")
                    Text("${qs.timeTaken}: ${formatDuration(result.durationSeconds)}")
                }
            }
        }
        if (result.wrong == 0 && result.unanswered == 0) {
            item { InfoCard(text = qs.perfectScore) }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onReview, modifier = Modifier.fillMaxWidth()) {
                    Text(qs.reviewAnswers)
                }
                if (result.wrong > 0) {
                    OutlinedButton(onClick = onRetryIncorrect, modifier = Modifier.fillMaxWidth()) {
                        Text(qs.retryIncorrect)
                    }
                }
                OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                    Text(qs.backToPractice)
                }
            }
        }
        if (topics.isNotEmpty()) {
            item { Text(qs.topicAccuracyTitle, style = MaterialTheme.typography.titleMedium) }
            items(topics) { (topicId, score) ->
                val title = TopicCatalog.byId(topicId)
                    ?.let { if (isUrdu) it.titleUr else it.titleEn } ?: topicId
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, style = MaterialTheme.typography.bodyLarge)
                    LinearProgressIndicator(
                        progress = { score.percent / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = qs.accuracy(score.correct, score.attempted, score.percent),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
