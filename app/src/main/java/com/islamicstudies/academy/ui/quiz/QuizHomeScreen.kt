package com.islamicstudies.academy.ui.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.islamicstudies.academy.data.db.QuizAttemptEntity
import com.islamicstudies.academy.ui.LocalAppLanguage
import com.islamicstudies.academy.ui.LocalStrings
import com.islamicstudies.academy.ui.Strings
import com.islamicstudies.academy.ui.components.LevelChip
import com.islamicstudies.academy.ui.pickTitle
import com.islamicstudies.academy.ui.screenTitleStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun modeLabel(mode: String, s: Strings): String = when (mode) {
    "timed" -> s.modeTimed
    "mock" -> s.modeMock
    "mistakes" -> s.modeMistakes
    else -> s.modePractice
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizHomeScreen(
    onBack: () -> Unit,
    onStart: (mode: String, chapterId: String, difficulty: String, count: Int) -> Unit,
    onOpenAttempt: (Long) -> Unit,
    viewModel: QuizHomeViewModel = viewModel()
) {
    val s = LocalStrings.current
    val uiLang = LocalAppLanguage.current
    val chapters by viewModel.chapters.collectAsStateWithLifecycle()
    val attempts by viewModel.attempts.collectAsStateWithLifecycle()
    val questionCount by viewModel.questionCount.collectAsStateWithLifecycle()

    var mode by rememberSaveable { mutableStateOf("practice") }
    var chapterId by rememberSaveable { mutableStateOf("all") }
    var difficulty by rememberSaveable { mutableStateOf("all") }
    var count by rememberSaveable { mutableIntStateOf(10) }
    var showChapters by rememberSaveable { mutableStateOf(false) }

    val showFilters = mode == "practice" || mode == "timed"
    val chapterName = if (chapterId == "all") {
        s.allChapters
    } else {
        val found = chapters.firstOrNull { c -> c.id == chapterId }
        if (found == null) s.allChapters else pickTitle(found.titleUr, found.titleEn, uiLang)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = s.tileQuiz, style = screenTitleStyle()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = s.back
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = s.questionsAvailable(questionCount),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            if (questionCount > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = s.sampleQuestionsNotice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            ModeCard(s.modePractice, s.modePracticeDesc, mode == "practice") { mode = "practice" }
            ModeCard(s.modeTimed, s.modeTimedDesc, mode == "timed") { mode = "timed" }
            ModeCard(s.modeMock, s.modeMockDesc, mode == "mock") { mode = "mock" }
            ModeCard(s.modeMistakes, s.modeMistakesDesc, mode == "mistakes") { mode = "mistakes" }

            if (showFilters) {
                Text(s.chapterLabel, style = MaterialTheme.typography.titleSmall)
                OutlinedButton(
                    onClick = { showChapters = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(chapterName, maxLines = 1) }

                Text(s.difficultyLabel, style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LevelChip(s.allLevels, difficulty == "all", { difficulty = "all" }, Modifier.weight(1f))
                    LevelChip(s.levelBasic, difficulty == "basic", { difficulty = "basic" }, Modifier.weight(1f))
                    LevelChip(
                        s.levelIntermediate,
                        difficulty == "intermediate",
                        { difficulty = "intermediate" },
                        Modifier.weight(1f)
                    )
                    LevelChip(s.levelAdvanced, difficulty == "advanced", { difficulty = "advanced" }, Modifier.weight(1f))
                }
            }

            Text(s.countLabel, style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LevelChip("5", count == 5, { count = 5 }, Modifier.weight(1f))
                LevelChip("10", count == 10, { count = 10 }, Modifier.weight(1f))
                LevelChip("20", count == 20, { count = 20 }, Modifier.weight(1f))
                LevelChip(s.allLevels, count == 0, { count = 0 }, Modifier.weight(1f))
            }

            Button(
                onClick = {
                    val ch = if (showFilters) chapterId else "all"
                    val df = if (showFilters) difficulty else "all"
                    onStart(mode, ch, df, count)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(s.startQuiz) }

            Spacer(modifier = Modifier.height(8.dp))
            Text(s.historyTitle, style = MaterialTheme.typography.titleMedium)
            if (attempts.isEmpty()) {
                Text(s.noHistory, style = MaterialTheme.typography.bodyMedium)
            } else {
                for (attempt in attempts.take(10)) {
                    AttemptRow(attempt = attempt, onClick = { onOpenAttempt(attempt.id) })
                }
            }
        }
    }

    if (showChapters) {
        AlertDialog(
            onDismissRequest = { showChapters = false },
            confirmButton = { TextButton(onClick = { showChapters = false }) { Text(s.close) } },
            title = { Text(s.chapterLabel) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    TextButton(
                        onClick = {
                            chapterId = "all"
                            showChapters = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(s.allChapters) }
                    for (c in chapters) {
                        TextButton(
                            onClick = {
                                chapterId = c.id
                                showChapters = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(pickTitle(c.titleUr, c.titleEn, uiLang)) }
                    }
                }
            }
        )
    }
}

@Composable
private fun ModeCard(title: String, description: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AttemptRow(attempt: QuizAttemptEntity, onClick: () -> Unit) {
    val s = LocalStrings.current
    val date = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(Date(attempt.startedAt))
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = modeLabel(attempt.mode, s) + "  ·  " + s.scoreLine(attempt.correctCount, attempt.totalQuestions),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
