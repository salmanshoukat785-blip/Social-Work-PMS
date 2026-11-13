package com.islamicstudies.academy.ui.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.islamicstudies.academy.ui.LocalAppLanguage
import com.islamicstudies.academy.ui.LocalStrings
import com.islamicstudies.academy.ui.components.LangLines
import com.islamicstudies.academy.ui.components.contentLanguages
import com.islamicstudies.academy.ui.components.linesFor
import com.islamicstudies.academy.ui.screenTitleStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultScreen(
    onBack: () -> Unit,
    viewModel: QuizResultViewModel = viewModel()
) {
    val s = LocalStrings.current
    val uiLang = LocalAppLanguage.current
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val langs = contentLanguages(settings, uiLang)
    val scale = settings.textScalePercent / 100f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = s.resultTitle, style = screenTitleStyle()) },
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
        val attempt = ui.attempt
        if (ui.loading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else if (attempt == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                contentAlignment = Alignment.Center
            ) { Text(text = s.noHistory, textAlign = TextAlign.Center) }
        } else {
            val percent = if (attempt.totalQuestions > 0) {
                attempt.correctCount * 100 / attempt.totalQuestions
            } else {
                0
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$percent%",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = s.scoreLine(attempt.correctCount, attempt.totalQuestions),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = modeLabel(attempt.mode, s) + "  ·  " + formatSeconds(attempt.durationSec),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                for ((position, item) in ui.items.withIndex()) {
                    val q = item.question
                    val urOptions = q.optionsFor("ur")
                    val enOptions = q.optionsFor("en")
                    val yourUr = urOptions.getOrNull(item.selected).orEmpty()
                    val yourEn = enOptions.getOrNull(item.selected).orEmpty()
                    val rightUr = urOptions.getOrNull(q.correctIndex).orEmpty()
                    val rightEn = enOptions.getOrNull(q.correctIndex).orEmpty()
                    val wasCorrect = item.selected == q.correctIndex

                    Surface(
                        color = if (wasCorrect) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = (position + 1).toString() + "  " + (if (wasCorrect) "✓" else "✗"),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            LangLines(
                                lines = linesFor(q.textUr, q.textEn, langs),
                                bold = true,
                                scale = scale
                            )
                            Text(s.yourAnswerLabel, style = MaterialTheme.typography.labelMedium)
                            if (item.selected < 0) {
                                Text(s.notAnswered, style = MaterialTheme.typography.bodyMedium)
                            } else {
                                LangLines(lines = linesFor(yourUr, yourEn, langs), scale = scale)
                            }
                            if (!wasCorrect) {
                                Text(s.correctAnswerLabel, style = MaterialTheme.typography.labelMedium)
                                LangLines(lines = linesFor(rightUr, rightEn, langs), scale = scale)
                            }
                            val explanation = linesFor(q.explanationUr, q.explanationEn, langs)
                            if (explanation.isNotEmpty()) {
                                Text(s.explanationLabel, style = MaterialTheme.typography.labelMedium)
                                LangLines(lines = explanation, scale = scale)
                            }
                        }
                    }
                }

                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text(s.backToQuiz)
                }
            }
        }
    }
}
