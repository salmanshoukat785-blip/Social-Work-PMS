package com.islamicstudies.academy.ui.home

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.islamicstudies.academy.ui.LocalAppLanguage
import com.islamicstudies.academy.ui.LocalStrings
import com.islamicstudies.academy.ui.isEnglishPrimary
import com.islamicstudies.academy.ui.pickTitle
import com.islamicstudies.academy.ui.screenTitleStyle
import com.islamicstudies.academy.ui.theme.UrduText
import kotlinx.coroutines.launch

private data class Tile(val emoji: String, val label: String, val onClick: () -> Unit)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenChapters: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenLesson: (String) -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val s = LocalStrings.current
    val isUrdu = LocalAppLanguage.current == "ur"
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val comingSoon: () -> Unit = {
        scope.launch { snackbarHostState.showSnackbar(s.comingSoon) }
    }

    val levelLabel = when (ui.settings.level) {
        "intermediate" -> s.levelIntermediate
        "advanced" -> s.levelAdvanced
        else -> s.levelBasic
    }
    val fraction = if (ui.total > 0) ui.completed.toFloat() / ui.total.toFloat() else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = s.appName, style = screenTitleStyle()) },
                actions = {
                    TextButton(onClick = { viewModel.toggleLanguage() }) {
                        Text(text = s.langToggleLabel, color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = s.greeting(ui.settings.name),
                        style = if (isUrdu) UrduText.title else MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = s.tagline,
                        style = if (isUrdu) UrduText.body else MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = levelLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            val last = ui.lastOpened
            if (last != null) {
                val lastEnglish = isEnglishPrimary(last.titleEn, LocalAppLanguage.current)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenLesson(last.headingId) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = s.continueLearning,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = pickTitle(last.titleUr, last.titleEn, LocalAppLanguage.current),
                            style = if (lastEnglish) MaterialTheme.typography.titleMedium else UrduText.subheading,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = pickTitle(last.chapterTitleUr, last.chapterTitleEn, LocalAppLanguage.current),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenProgress),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = s.progressTitle,
                        style = if (isUrdu) UrduText.subheading else MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = s.progressText(ui.completed, ui.total),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            val tiles = listOf(
                Tile("📚", s.tileChapters, onOpenChapters),
                Tile("📝", s.tileQuiz, onOpenQuiz),
                Tile("🔖", s.tileBookmarks, onOpenBookmarks),
                Tile("🗒️", s.tileNotes, onOpenNotes),
                Tile("🔍", s.tileSearch, onOpenSearch),
                Tile("⚙️", s.tileSettings, onOpenSettings)
            )
            tiles.chunked(2).forEach { rowTiles ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowTiles.forEach { tile ->
                        TileCard(tile = tile, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TileCard(tile: Tile, modifier: Modifier = Modifier) {
    val isUrdu = LocalAppLanguage.current == "ur"
    Card(
        modifier = modifier.clickable(onClick = tile.onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = tile.emoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tile.label,
                style = if (isUrdu) UrduText.subheading else MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
