package com.islamicstudies.academy.ui.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.islamicstudies.academy.data.db.QuestionEntity
import com.islamicstudies.academy.ui.LocalAppLanguage
import com.islamicstudies.academy.ui.LocalStrings
import com.islamicstudies.academy.ui.components.LangLines
import com.islamicstudies.academy.ui.components.contentLanguages
import com.islamicstudies.academy.ui.components.linesFor
import com.islamicstudies.academy.ui.screenTitleStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizPlayScreen(
    onBack: () -> Unit,
    onFinished: (Long) -> Unit,
    viewModel: QuizPlayViewModel = viewModel()
) {
    val s = LocalStrings.current
    val uiLang = LocalAppLanguage.current
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val langs = contentLanguages(settings, uiLang)
    val scale = settings.textScalePercent / 100f

    LaunchedEffect(ui.finishedAttemptId) {
        if (ui.finishedAttemptId >= 0) onFinished(ui.finishedAttemptId)
    }

    val total = ui.questions.size
    val hasTimer = ui.mode == "timed" || ui.mode == "mock"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (total > 0) s.questionOf(ui.index + 1, total) else s.tileQuiz,
                        style = screenTitleStyle()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = s.back
                        )
                    }
                },
                actions = {
                    if (hasTimer && total > 0) {
                        Text(
                            text = "⏱ " + formatSeconds(ui.secondsLeft),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 16.dp)
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
        if (ui.loading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else if (total == 0) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (ui.mode == "mistakes") s.noMistakes else s.noQuestions,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val question = ui.questions[ui.index]
            val immediate = ui.mode == "practice" || ui.mode == "mistakes"
            val canContinue = if (immediate) ui.revealed else ui.selected >= 0 || hasTimer
            val isLast = ui.index + 1 >= total

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val done = ui.index + (if (ui.revealed) 1 else 0)
                LinearProgressIndicator(
                    progress = { done.toFloat() / total.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                LangLines(
                    lines = linesFor(question.textUr, question.textEn, langs),
                    bold = true,
                    scale = scale
                )

                val urOptions = question.optionsFor("ur")
                val enOptions = question.optionsFor("en")
                val optionCount = if (urOptions.size > enOptions.size) urOptions.size else enOptions.size
                for (i in 0 until optionCount) {
                    val lines = linesFor(urOptions.getOrNull(i).orEmpty(), enOptions.getOrNull(i).orEmpty(), langs)
                    OptionCard(
                        number = i + 1,
                        lines = lines,
                        scale = scale,
                        isSelected = ui.selected == i,
                        revealed = ui.revealed,
                        isCorrect = question.correctIndex == i,
                        onClick = { viewModel.select(i) }
                    )
                }

                if (ui.revealed) {
                    ExplanationCard(question = question, langs = langs, scale = scale)
                }

                Button(
                    onClick = { viewModel.next() },
                    enabled = canContinue,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isLast) s.finish else s.next)
                }
            }
        }
    }
}

@Composable
private fun OptionCard(
    number: Int,
    lines: List<Pair<String, String>>,
    scale: Float,
    isSelected: Boolean,
    revealed: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val container = when {
        revealed && isCorrect -> MaterialTheme.colorScheme.secondaryContainer
        revealed && isSelected -> MaterialTheme.colorScheme.errorContainer
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        revealed && isCorrect -> MaterialTheme.colorScheme.secondary
        revealed && isSelected -> MaterialTheme.colorScheme.error
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }
    val mark = when {
        revealed && isCorrect -> "✓"
        revealed && isSelected -> "✗"
        else -> number.toString()
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(enabled = !revealed, onClick = onClick),
        shape = shape,
        color = container,
        border = BorderStroke(if (isSelected || (revealed && isCorrect)) 2.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = mark,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(28.dp)
            )
            LangLines(lines = lines, scale = scale, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ExplanationCard(question: QuestionEntity, langs: List<String>, scale: Float) {
    val s = LocalStrings.current
    val lines = linesFor(question.explanationUr, question.explanationEn, langs)
    if (lines.isEmpty()) return
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = s.explanationLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            LangLines(lines = lines, scale = scale)
        }
    }
}
