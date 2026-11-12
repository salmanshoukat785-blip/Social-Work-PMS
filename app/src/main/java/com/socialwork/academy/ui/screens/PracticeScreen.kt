package com.socialwork.academy.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.socialwork.academy.data.McqState
import com.socialwork.academy.data.QuizAttempt
import com.socialwork.academy.data.QuizBuilder
import com.socialwork.academy.data.QuizConfig
import com.socialwork.academy.data.QuizOptions
import com.socialwork.academy.data.QuizScope
import com.socialwork.academy.data.TopicCatalog
import com.socialwork.academy.data.TopicScore
import com.socialwork.academy.ui.AppStrings
import com.socialwork.academy.ui.QuizStrings
import java.text.DateFormat
import java.util.Date

@Composable
fun PracticeScreen(
    strings: AppStrings,
    qs: QuizStrings,
    isUrdu: Boolean,
    mcq: McqState,
    attempts: List<QuizAttempt>,
    topicStats: Map<String, TopicScore>,
    onStart: (QuizConfig) -> Unit,
    onOpenAttempt: (QuizAttempt) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = strings.practiceTitle, style = MaterialTheme.typography.titleLarge)
        }
        item { InfoCard(text = qs.note) }

        when (mcq) {
            McqState.Loading -> item { Text(qs.loading) }
            McqState.Failed -> item { InfoCard(text = qs.loadError) }
            is McqState.Ready -> {
                item { SetupCard(strings, qs, isUrdu, mcq, onStart) }
                item {
                    Text(qs.historyTitle, style = MaterialTheme.typography.titleMedium)
                }
                if (attempts.isEmpty()) {
                    item { InfoCard(text = qs.noHistory) }
                } else {
                    items(attempts.take(10)) { attempt ->
                        HistoryRow(strings, qs, isUrdu, attempt) { onOpenAttempt(attempt) }
                    }
                }
                item {
                    Text(qs.topicAccuracyTitle, style = MaterialTheme.typography.titleMedium)
                }
                if (topicStats.isEmpty()) {
                    item { InfoCard(text = qs.noAccuracy) }
                } else {
                    val rows = topicStats.entries
                        .sortedWith(compareBy({ it.value.percent }, { it.key }))
                    items(rows) { (topicId, score) ->
                        AccuracyRow(qs, isUrdu, topicId, score)
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupCard(
    strings: AppStrings,
    qs: QuizStrings,
    isUrdu: Boolean,
    ready: McqState.Ready,
    onStart: (QuizConfig) -> Unit
) {
    var scopeKind by rememberSaveable { mutableStateOf("all") } // all, p1, p2, topic
    var topicId by rememberSaveable { mutableStateOf("") }
    var count by rememberSaveable { mutableStateOf(QuizOptions.counts.first()) }
    var timer by rememberSaveable { mutableStateOf(0) }
    var feedback by rememberSaveable { mutableStateOf(true) }
    var menuOpen by remember { mutableStateOf(false) }

    val pool = ready.bundle.questions
    // Only topics that have questions in the current language can be picked.
    val topics = remember(pool, isUrdu) {
        TopicCatalog.all.filter { QuizBuilder.available(pool, QuizScope.ByTopic(it.id), isUrdu) > 0 }
    }
    val scope: QuizScope? = when (scopeKind) {
        "p1" -> QuizScope.ByPaper(1)
        "p2" -> QuizScope.ByPaper(2)
        "topic" -> topics.firstOrNull { it.id == topicId }?.let { QuizScope.ByTopic(it.id) }
        else -> QuizScope.Everything
    }
    val available = scope?.let { QuizBuilder.available(pool, it, isUrdu) } ?: 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(qs.setupTitle, style = MaterialTheme.typography.titleMedium)

            Text(qs.scopeTitle, style = MaterialTheme.typography.labelLarge)
            ChipColumn {
                ScopeChip(qs.scopeAll, scopeKind == "all") { scopeKind = "all" }
                ScopeChip(strings.paper1, scopeKind == "p1") { scopeKind = "p1" }
                ScopeChip(strings.paper2, scopeKind == "p2") { scopeKind = "p2" }
                ScopeChip(qs.scopeTopic, scopeKind == "topic") { scopeKind = "topic" }
            }
            if (scopeKind == "topic") {
                val chosen = topics.firstOrNull { it.id == topicId }
                Column {
                    OutlinedButton(onClick = { menuOpen = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = chosen?.let { if (isUrdu) it.titleUr else it.titleEn } ?: qs.pickTopic,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        topics.forEach { topic ->
                            DropdownMenuItem(
                                text = { Text(if (isUrdu) topic.titleUr else topic.titleEn) },
                                onClick = {
                                    topicId = topic.id
                                    menuOpen = false
                                }
                            )
                        }
                    }
                }
            }

            Text(qs.countTitle, style = MaterialTheme.typography.labelLarge)
            ChipColumn {
                QuizOptions.counts.forEach { n ->
                    ScopeChip(n.toString(), count == n) { count = n }
                }
            }

            Text(qs.timerTitle, style = MaterialTheme.typography.labelLarge)
            ChipColumn {
                QuizOptions.timers.forEach { m ->
                    ScopeChip(if (m == 0) qs.noTimer else qs.minutes(m), timer == m) { timer = m }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(qs.feedbackLabel, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        qs.feedbackHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = feedback, onCheckedChange = { feedback = it })
            }

            Text(
                text = when {
                    scope == null || available == 0 -> qs.noQuestions
                    available < count -> qs.available(available) + ". " + qs.fewerNote
                    else -> qs.available(available)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = {
                    if (scope != null) {
                        onStart(QuizConfig(scope, count, timer, feedback, isUrdu))
                    }
                },
                enabled = scope != null && available > 0,
                modifier = Modifier.fillMaxWidth()
            ) { Text(qs.startQuiz) }
        }
    }
}

/** Chips that wrap onto the next line instead of running off the screen. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ChipColumn(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) { content() }
}

@Composable
private fun ScopeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

@Composable
private fun HistoryRow(
    strings: AppStrings,
    qs: QuizStrings,
    isUrdu: Boolean,
    attempt: QuizAttempt,
    onClick: () -> Unit
) {
    val date = remember(attempt.timestampMillis) {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
            .format(Date(attempt.timestampMillis))
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = scopeLabel(attempt.scopeKey, strings, qs, isUrdu),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = qs.score(attempt.correct, attempt.total) + " - " + attempt.percent + "%",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = date + " - " + formatDuration(attempt.durationSeconds),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AccuracyRow(qs: QuizStrings, isUrdu: Boolean, topicId: String, score: TopicScore) {
    val title = TopicCatalog.byId(topicId)?.let { if (isUrdu) it.titleUr else it.titleEn } ?: topicId
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
