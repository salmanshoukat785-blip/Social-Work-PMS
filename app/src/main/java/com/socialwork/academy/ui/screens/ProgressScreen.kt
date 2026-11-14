package com.socialwork.academy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.socialwork.academy.data.DayActivity
import com.socialwork.academy.data.ProgressCalc
import com.socialwork.academy.data.QuizAttempt
import com.socialwork.academy.data.TopicCatalog
import com.socialwork.academy.data.TopicScore
import com.socialwork.academy.data.WeekChart
import com.socialwork.academy.data.progressFor
import com.socialwork.academy.ui.AppStrings
import com.socialwork.academy.ui.DataStrings
import com.socialwork.academy.ui.QuizStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Study progress worked out only from what the student did on this phone. */
@Composable
fun ProgressScreen(
    strings: AppStrings,
    ds: DataStrings,
    qs: QuizStrings,
    isUrdu: Boolean,
    completedTopics: Set<String>,
    lastTopicId: String?,
    attempts: List<QuizAttempt>,
    topicStats: Map<String, TopicScore>,
    activity: Map<String, DayActivity>,
    onContinue: (String) -> Unit,
    onOpenAttempt: (QuizAttempt) -> Unit,
    onBack: () -> Unit
) {
    val summary = remember(attempts, topicStats) { ProgressCalc.summarize(attempts, topicStats) }
    val overall = progressFor(TopicCatalog.all, completedTopics)
    val week = remember(activity) { WeekChart.build(activity, System.currentTimeMillis()) }
    val notYet = ds.notYet

    Column(modifier = Modifier.fillMaxSize()) {
        ToolHeader(ds.progressTitle, strings.back, onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoCard(ds.calcNote)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile("2", strings.statPapers, Modifier.weight(1f))
                StatTile(overall.total.toString(), strings.statTopics, Modifier.weight(1f))
                StatTile(overall.completed.toString(), strings.statCompleted, Modifier.weight(1f))
                StatTile(overall.remaining.toString(), strings.statRemaining, Modifier.weight(1f))
            }
            LinearProgressIndicator(progress = { overall.fraction }, modifier = Modifier.fillMaxWidth())
            listOf(1 to strings.paper1, 2 to strings.paper2).forEach { (paper, label) ->
                val p = progressFor(TopicCatalog.forPaper(paper), completedTopics)
                Text(ds.paperProgress(label, p.completed, p.total), style = MaterialTheme.typography.bodyMedium)
            }
            lastTopicId?.let { id ->
                if (TopicCatalog.byId(id) != null) {
                    Button(onClick = { onContinue(id) }, modifier = Modifier.fillMaxWidth()) {
                        Text(strings.continueLearning)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile(summary.mcqsAttempted.toString(), ds.mcqsAttempted, Modifier.weight(1f))
                StatTile(summary.accuracyPercent?.let { "$it%" } ?: notYet, ds.accuracyLabel, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile(summary.bestMockPercent?.let { "$it%" } ?: notYet, ds.bestMock, Modifier.weight(1f))
                StatTile(summary.bestQuizPercent?.let { "$it%" } ?: notYet, ds.bestQuiz, Modifier.weight(1f))
            }

            Text(ds.weekTitle, style = MaterialTheme.typography.titleMedium, modifier = Modifier.asHeading())
            WeekBars(ds, isUrdu, week.map { Triple(it.millis, it.questions, it.quizSeconds) })
            Text(
                ds.weekQuizTime((week.sumOf { it.quizSeconds } / 60).toInt()) + "  -  " +
                    ds.weekTopics(week.sumOf { it.topics }),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(ds.weakTitle, style = MaterialTheme.typography.titleMedium, modifier = Modifier.asHeading())
            if (summary.weak.isEmpty()) {
                InfoCard(ds.noWeak)
            } else {
                Text(
                    ds.weakHelp(ProgressCalc.MIN_ANSWERS_FOR_WEAK, ProgressCalc.WEAK_BELOW_PERCENT),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                summary.weak.forEach { w ->
                    val title = TopicCatalog.byId(w.topicId)?.let { if (isUrdu) it.titleUr else it.titleEn } ?: w.topicId
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(title, style = MaterialTheme.typography.bodyLarge)
                        LinearProgressIndicator(progress = { w.score.percent / 100f }, modifier = Modifier.fillMaxWidth())
                        Text(
                            qs.accuracy(w.score.correct, w.score.attempted, w.score.percent),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(ds.recentTitle, style = MaterialTheme.typography.titleMedium, modifier = Modifier.asHeading())
            if (summary.recent.isEmpty()) {
                InfoCard(ds.noRecent)
            } else {
                summary.recent.forEach { a -> HistoryRow(strings, qs, isUrdu, a) { onOpenAttempt(a) } }
            }
        }
    }
}

/** Seven bars, one per day. Each bar also has its number written above it. */
@Composable
private fun WeekBars(ds: DataStrings, isUrdu: Boolean, days: List<Triple<Long, Int, Long>>) {
    val max = (days.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)
    val labelFormat = remember(isUrdu) { SimpleDateFormat("EEE", if (isUrdu) Locale("ur") else Locale.ENGLISH) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(ds.weekHelp, style = MaterialTheme.typography.labelMedium)
            if (days.all { it.second == 0 }) {
                Text(ds.noWeekActivity, style = MaterialTheme.typography.bodyMedium)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 130.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEach { (millis, count, _) ->
                    val dayLabel = labelFormat.format(Date(millis))
                    val description = dayLabel + ", " + ds.questionsShort(count)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clearAndSetSemantics { contentDescription = description }
                    ) {
                        Text(count.toString(), style = MaterialTheme.typography.labelSmall)
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height((4 + 80f * count / max).dp)
                                .background(
                                    if (count > 0) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(6.dp)
                                )
                        )
                        Text(dayLabel, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
