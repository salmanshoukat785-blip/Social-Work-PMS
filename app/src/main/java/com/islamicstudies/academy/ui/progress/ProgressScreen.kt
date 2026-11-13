package com.islamicstudies.academy.ui.progress

import android.app.Application
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.islamicstudies.academy.AcademyApp
import com.islamicstudies.academy.data.db.ChapterProgress
import com.islamicstudies.academy.data.settings.currentStreak
import com.islamicstudies.academy.data.settings.localEpochDay
import com.islamicstudies.academy.ui.LocalAppLanguage
import com.islamicstudies.academy.ui.LocalStrings
import com.islamicstudies.academy.ui.isEnglishPrimary
import com.islamicstudies.academy.ui.pickTitle
import com.islamicstudies.academy.ui.screenTitleStyle
import com.islamicstudies.academy.ui.theme.UrduText
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ProgressUi(
    val completed: Int = 0,
    val total: Int = 0,
    val streak: Int = 0,
    val studyDays: Int = 0,
    val attempts: Int = 0,
    val averagePercent: Int = 0,
    val bestPercent: Int = 0,
    val chapters: List<ChapterProgress> = emptyList()
)

class ProgressViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as AcademyApp

    val ui: StateFlow<ProgressUi> = combine(
        app.database.userDao().observeChapterProgress(),
        app.database.quizDao().observeAttempts(),
        app.settingsRepository.studyDays
    ) { chapters, attempts, days ->
        var done = 0
        var total = 0
        for (c in chapters) {
            done += c.completedTopics
            total += c.totalTopics
        }
        var sumCorrect = 0
        var sumTotal = 0
        var best = 0
        for (a in attempts) {
            sumCorrect += a.correctCount
            sumTotal += a.totalQuestions
            if (a.totalQuestions > 0) {
                val percent = a.correctCount * 100 / a.totalQuestions
                if (percent > best) best = percent
            }
        }
        ProgressUi(
            completed = done,
            total = total,
            streak = currentStreak(days, localEpochDay()),
            studyDays = days.size,
            attempts = attempts.size,
            averagePercent = if (sumTotal > 0) sumCorrect * 100 / sumTotal else 0,
            bestPercent = best,
            chapters = chapters
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProgressUi())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    onBack: () -> Unit,
    viewModel: ProgressViewModel = viewModel()
) {
    val s = LocalStrings.current
    val uiLang = LocalAppLanguage.current
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val overall = if (ui.total > 0) ui.completed.toFloat() / ui.total.toFloat() else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = s.progressTitle, style = screenTitleStyle()) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        text = (overall * 100).toInt().toString() + "%",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { overall },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = s.progressText(ui.completed, ui.total),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            StatCard(title = s.streakTitle) {
                Text(
                    text = s.streakDays(ui.streak),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(text = s.totalStudyDays(ui.studyDays), style = MaterialTheme.typography.bodyMedium)
            }

            StatCard(title = s.quizStatsTitle) {
                Text(text = s.quizAttemptsLine(ui.attempts), style = MaterialTheme.typography.bodyLarge)
                Text(text = s.quizAverageLine(ui.averagePercent), style = MaterialTheme.typography.bodyLarge)
                Text(text = s.quizBestLine(ui.bestPercent), style = MaterialTheme.typography.bodyLarge)
            }

            Text(s.tileChapters, style = MaterialTheme.typography.titleMedium)
            for (c in ui.chapters) {
                val fraction = if (c.totalTopics > 0) c.completedTopics.toFloat() / c.totalTopics.toFloat() else 0f
                val englishFirst = isEnglishPrimary(c.titleEn, uiLang)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = c.number.toString() + ".  ",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = pickTitle(c.titleUr, c.titleEn, uiLang),
                                style = if (englishFirst) MaterialTheme.typography.titleSmall else UrduText.subheading,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = s.chapterProgressLine(c.completedTopics, c.totalTopics),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}
