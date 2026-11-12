package com.socialwork.academy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.socialwork.academy.ui.AppStrings
import com.socialwork.academy.ui.QuizStrings

/** Set up a timed, exam-style test: Paper 1, Paper 2 or both papers. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MockSetupScreen(
    strings: AppStrings,
    qs: QuizStrings,
    isUrdu: Boolean,
    mcq: McqState,
    attempts: List<QuizAttempt>,
    onStart: (QuizConfig) -> Unit,
    onOpenAttempt: (QuizAttempt) -> Unit,
    onBack: () -> Unit
) {
    var paper by rememberSaveable { mutableStateOf(0) } // 0 = both, 1, 2
    var count by rememberSaveable { mutableStateOf(QuizOptions.mockCounts[1]) }
    var minutes by rememberSaveable { mutableStateOf(QuizOptions.mockDurations[1]) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
            }
            Text(qs.mockTitle, style = MaterialTheme.typography.titleLarge)
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { InfoCard(text = qs.mockNote) }

            when (mcq) {
                McqState.Loading -> item { Text(qs.loading) }
                McqState.Failed -> item { InfoCard(text = qs.loadError) }
                is McqState.Ready -> {
                    item {
                        val scope: QuizScope = when (paper) {
                            1 -> QuizScope.ByPaper(1)
                            2 -> QuizScope.ByPaper(2)
                            else -> QuizScope.Everything
                        }
                        val available = QuizBuilder.available(mcq.bundle.questions, scope, isUrdu)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(qs.scopeTitle, style = MaterialTheme.typography.labelLarge)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(paper == 0, { paper = 0 }, { Text(qs.scopeAll) })
                                    FilterChip(paper == 1, { paper = 1 }, { Text(strings.paper1) })
                                    FilterChip(paper == 2, { paper = 2 }, { Text(strings.paper2) })
                                }
                                Text(qs.countTitle, style = MaterialTheme.typography.labelLarge)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    QuizOptions.mockCounts.forEach { n ->
                                        FilterChip(count == n, { count = n }, { Text(n.toString()) })
                                    }
                                }
                                Text(qs.durationTitle, style = MaterialTheme.typography.labelLarge)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    QuizOptions.mockDurations.forEach { m ->
                                        FilterChip(minutes == m, { minutes = m }, { Text(qs.minutes(m)) })
                                    }
                                }
                                Text(
                                    text = when {
                                        available == 0 -> qs.noQuestions
                                        available < count -> qs.available(available) + ". " + qs.fewerNote
                                        else -> qs.available(available)
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = {
                                        onStart(
                                            QuizConfig(
                                                scope = scope,
                                                count = count,
                                                timerMinutes = minutes,
                                                instantFeedback = false,
                                                isUrdu = isUrdu,
                                                mock = true
                                            )
                                        )
                                    },
                                    enabled = available > 0,
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(qs.mockStart) }
                            }
                        }
                    }

                    item { Text(qs.mockHistoryTitle, style = MaterialTheme.typography.titleMedium) }
                    val mockAttempts = attempts.filter { it.mock }
                    if (mockAttempts.isEmpty()) {
                        item { InfoCard(text = qs.noHistory) }
                    } else {
                        items(mockAttempts.take(10)) { attempt ->
                            HistoryRow(strings, qs, isUrdu, attempt) { onOpenAttempt(attempt) }
                        }
                    }
                }
            }
        }
    }
}
