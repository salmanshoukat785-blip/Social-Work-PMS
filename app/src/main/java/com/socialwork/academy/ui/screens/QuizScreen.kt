package com.socialwork.academy.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.FilterChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.socialwork.academy.data.QuizSession
import com.socialwork.academy.ui.AppStrings
import com.socialwork.academy.ui.QuizStrings
import kotlinx.coroutines.delay

/**
 * The running quiz. [onFinish] ends and saves it; when the session is finished
 * (by the button or the timer) [onFinished] opens the result screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuizScreen(
    strings: AppStrings,
    qs: QuizStrings,
    session: QuizSession?,
    onAnswer: (Int, Int) -> Unit,
    onGoTo: (Int) -> Unit,
    onToggleMark: (Int) -> Unit,
    onFinish: () -> Unit,
    onFinished: () -> Unit,
    onLeave: () -> Unit
) {
    if (session == null) {
        // Nothing to show (e.g. app was restarted): go back instead of a blank screen.
        LaunchedEffect(Unit) { onLeave() }
        return
    }
    if (session.isFinished) {
        LaunchedEffect(Unit) { onFinished() }
        return
    }

    var confirmFinish by remember { mutableStateOf(false) }
    var showNavigator by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val deadline = session.deadlineMillis
    LaunchedEffect(deadline) {
        if (deadline == null) return@LaunchedEffect
        while (true) {
            now = System.currentTimeMillis()
            if (now >= deadline) {
                onFinish()
                break
            }
            delay(1000)
        }
    }

    BackHandler { confirmFinish = true }

    val index = session.currentIndex
    val question = session.questions[index]
    val chosen = session.answers[index]
    val locked = session.isLocked(index)
    val showFeedback = session.config.instantFeedback && chosen != null

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { confirmFinish = true }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
            }
            Text(
                text = qs.questionOf(index + 1, session.questions.size),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            if (deadline != null) {
                val left = ((deadline - now) / 1000).coerceAtLeast(0)
                Text(
                    text = formatDuration(left),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (left <= 60) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            IconButton(onClick = { showNavigator = true }) {
                Icon(Icons.Filled.GridView, contentDescription = qs.navigatorTitle)
            }
            val isMarked = index in session.marked
            IconButton(onClick = { onToggleMark(index) }) {
                Icon(
                    imageVector = if (isMarked) Icons.Filled.Flag else Icons.Outlined.Flag,
                    contentDescription = if (isMarked) qs.unmarkLabel else qs.markLabel,
                    tint = if (isMarked) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        LinearProgressIndicator(
            progress = { session.answers.size / session.questions.size.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        McqDirection(isUrdu = question.isUrdu) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuestionText(question)
                question.options.forEachIndexed { i, text ->
                    val mark = when {
                        showFeedback && i == question.answerIndex -> OptionMark.Correct
                        showFeedback && i == chosen -> OptionMark.Wrong
                        !showFeedback && i == chosen -> OptionMark.Selected
                        else -> OptionMark.None
                    }
                    OptionRow(
                        text = text,
                        mark = mark,
                        qs = qs,
                        enabled = !locked,
                        onClick = { onAnswer(index, i) }
                    )
                }
                if (showFeedback) ExplanationCard(question, qs)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onGoTo(index - 1) },
                enabled = index > 0,
                modifier = Modifier.weight(1f)
            ) { Text(qs.previous) }
            if (index < session.questions.size - 1) {
                Button(onClick = { onGoTo(index + 1) }, modifier = Modifier.weight(1f)) {
                    Text(qs.next)
                }
            } else {
                Button(onClick = { confirmFinish = true }, modifier = Modifier.weight(1f)) {
                    Text(qs.finish)
                }
            }
        }
    }

    if (confirmFinish) {
        val left = session.unansweredCount()
        AlertDialog(
            onDismissRequest = { confirmFinish = false },
            title = { Text(qs.finishTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (left > 0) qs.unanswered(left) else qs.allAnswered)
                    if (session.marked.isNotEmpty()) Text(qs.marked(session.marked.size))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmFinish = false
                    onFinish()
                }) { Text(qs.finishNow) }
            },
            dismissButton = {
                TextButton(onClick = { confirmFinish = false }) { Text(qs.keepGoing) }
            }
        )
    }

    if (showNavigator) {
        AlertDialog(
            onDismissRequest = { showNavigator = false },
            title = { Text(qs.navigatorTitle) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(qs.navigatorHelp, style = MaterialTheme.typography.bodySmall)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        session.questions.indices.forEach { i ->
                            FilterChip(
                                selected = session.answers.containsKey(i),
                                onClick = {
                                    onGoTo(i)
                                    showNavigator = false
                                },
                                label = { Text(if (i == index) "[${i + 1}]" else "${i + 1}") },
                                leadingIcon = if (i in session.marked) {
                                    { Icon(Icons.Filled.Flag, contentDescription = qs.markLabel) }
                                } else null
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNavigator = false }) { Text(qs.close) }
            }
        )
    }
}
