package com.islamicstudies.academy.ui.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.islamicstudies.academy.data.db.LessonSectionEntity
import com.islamicstudies.academy.data.settings.AppSettings
import com.islamicstudies.academy.ui.EnStrings
import com.islamicstudies.academy.ui.LocalAppLanguage
import com.islamicstudies.academy.ui.LocalStrings
import com.islamicstudies.academy.ui.UrStrings
import com.islamicstudies.academy.ui.components.LevelChip
import com.islamicstudies.academy.ui.isEnglishPrimary
import com.islamicstudies.academy.ui.pickTitle
import com.islamicstudies.academy.ui.screenTitleStyle
import com.islamicstudies.academy.ui.theme.UrduText
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    onBack: () -> Unit,
    onOpenLesson: (String) -> Unit,
    viewModel: LessonViewModel = viewModel()
) {
    val s = LocalStrings.current
    val uiLang = LocalAppLanguage.current

    val heading by viewModel.heading.collectAsStateWithLifecycle()
    val chapter by viewModel.chapter.collectAsStateWithLifecycle()
    val siblings by viewModel.siblings.collectAsStateWithLifecycle()
    val sections by viewModel.sections.collectAsStateWithLifecycle()
    val bookmarked by viewModel.bookmarked.collectAsStateWithLifecycle()
    val completed by viewModel.completed.collectAsStateWithLifecycle()
    val note by viewModel.note.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showNote by rememberSaveable { mutableStateOf(false) }

    val scale = settings.textScalePercent / 100f
    val contentLang = if (settings.contentLanguage.isBlank()) uiLang else settings.contentLanguage
    val langs = if (contentLang == "both") listOf("ur", "en") else listOf(contentLang)
    val level = settings.level

    val index = siblings.indexOfFirst { h -> h.id == viewModel.headingId }
    val prev = if (index > 0) siblings[index - 1] else null
    val next = if (index >= 0 && index < siblings.size - 1) siblings[index + 1] else null

    val headingUr = heading?.titleUr.orEmpty()
    val headingEn = heading?.titleEn.orEmpty()
    val englishFirst = isEnglishPrimary(headingEn, uiLang)
    val chapterTitle = pickTitle(chapter?.titleUr.orEmpty(), chapter?.titleEn.orEmpty(), uiLang)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = chapterTitle,
                        style = screenTitleStyle(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                    IconButton(onClick = { viewModel.toggleBookmark() }) {
                        Text(text = if (bookmarked) "⭐" else "☆", fontSize = 22.sp)
                    }
                    IconButton(onClick = { showNote = true }) {
                        Text(text = "📝", fontSize = 20.sp)
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Text(
                            text = "Aa",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimary
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
            // Heading
            Text(
                text = if (englishFirst) headingEn else headingUr,
                style = if (englishFirst) {
                    MaterialTheme.typography.headlineSmall
                } else {
                    UrduText.title.copy(fontSize = (22 * scale).sp, lineHeight = (38 * scale).sp)
                },
                modifier = Modifier.fillMaxWidth()
            )
            val secondaryTitle = if (englishFirst) headingUr else headingEn
            if (secondaryTitle.isNotBlank()) {
                Text(
                    text = secondaryTitle,
                    style = if (englishFirst) UrduText.body else MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Level selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LevelChip(s.levelBasic, level == "basic", { viewModel.setLevel("basic") }, Modifier.weight(1f))
                LevelChip(
                    s.levelIntermediate,
                    level == "intermediate",
                    { viewModel.setLevel("intermediate") },
                    Modifier.weight(1f)
                )
                LevelChip(s.levelAdvanced, level == "advanced", { viewModel.setLevel("advanced") }, Modifier.weight(1f))
            }

            // Content
            val blocks = langs.map { lang ->
                Pair(lang, sections.filter { sec -> sec.lang == lang && sec.level == level })
            }
            if (sections.isEmpty()) {
                InfoCard(text = s.noContent, isWarning = false)
            } else if (blocks.all { b -> b.second.isEmpty() }) {
                InfoCard(text = s.noContentForSelection, isWarning = false)
            } else {
                if (heading?.reviewStatus == "Draft") {
                    InfoCard(text = s.draftNotice, isWarning = true)
                }
                for (block in blocks) {
                    if (block.second.isNotEmpty()) {
                        if (langs.size > 1) {
                            Text(
                                text = if (block.first == "ur") "اردو" else "English",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        for (sec in block.second) {
                            SectionBlock(section = sec, scale = scale)
                        }
                    }
                }
            }

            // Personal note preview
            if (note.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showNote = true }
                ) {
                    Text(
                        text = "📝  " + note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Completed toggle
            Button(
                onClick = { viewModel.toggleCompleted() },
                modifier = Modifier.fillMaxWidth(),
                colors = if (completed) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(if (completed) s.completed else s.markCompleted)
            }

            // Previous / next lesson in this chapter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (prev != null) {
                    OutlinedButton(onClick = { onOpenLesson(prev.id) }) { Text(s.previous) }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                if (next != null) {
                    OutlinedButton(onClick = { onOpenLesson(next.id) }) { Text(s.next) }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
            }
        }
    }

    if (showSettings) {
        ReaderSettingsDialog(
            settings = settings,
            effectiveLanguage = contentLang,
            onDismiss = { showSettings = false },
            onContentLanguage = { l -> viewModel.setContentLanguage(l) },
            onTextScale = { p -> viewModel.setTextScale(p) },
            onTheme = { t -> viewModel.setTheme(t) }
        )
    }
    if (showNote) {
        NoteDialog(
            initial = note,
            onSave = { text -> viewModel.saveNote(text) },
            onDismiss = { showNote = false }
        )
    }
}

@Composable
private fun InfoCard(text: String, isWarning: Boolean) {
    Surface(
        color = if (isWarning) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isWarning) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(14.dp)
        )
    }
}

@Composable
private fun SectionBlock(section: LessonSectionEntity, scale: Float) {
    val isUrdu = section.lang == "ur"
    val direction = if (isUrdu) LayoutDirection.Rtl else LayoutDirection.Ltr
    val bodyStyle = if (isUrdu) {
        UrduText.body.copy(fontSize = (17 * scale).sp, lineHeight = (31 * scale).sp)
    } else {
        MaterialTheme.typography.bodyLarge.copy(fontSize = (16 * scale).sp, lineHeight = (26 * scale).sp)
    }

    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        when (section.kind) {
            "point" -> Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Spacer(modifier = Modifier.width(4.dp))
                Spacer(
                    modifier = Modifier
                        .padding(top = (10 * scale).dp)
                        .size(7.dp)
                        .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = section.body,
                    style = bodyStyle,
                    modifier = Modifier.weight(1f)
                )
            }
            "reference" -> Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (isUrdu) UrStrings.referenceLabel else EnStrings.referenceLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = section.body,
                        style = bodyStyle,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            else -> Text(
                text = section.body,
                style = bodyStyle,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ReaderSettingsDialog(
    settings: AppSettings,
    effectiveLanguage: String,
    onDismiss: () -> Unit,
    onContentLanguage: (String) -> Unit,
    onTextScale: (Int) -> Unit,
    onTheme: (String) -> Unit
) {
    val s = LocalStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(s.close) } },
        title = { Text(s.readerSettings) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(s.readerLanguageTitle, style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LevelChip(s.langUrdu, effectiveLanguage == "ur", { onContentLanguage("ur") }, Modifier.weight(1f))
                    LevelChip(s.langEnglish, effectiveLanguage == "en", { onContentLanguage("en") }, Modifier.weight(1f))
                    LevelChip(s.langBoth, effectiveLanguage == "both", { onContentLanguage("both") }, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(s.textSizeTitle, style = MaterialTheme.typography.titleSmall)
                Slider(
                    value = settings.textScalePercent.toFloat(),
                    onValueChange = { v -> onTextScale(v.roundToInt()) },
                    valueRange = 80f..160f,
                    steps = 7
                )
                Text("${settings.textScalePercent}%", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(4.dp))
                Text(s.themeTitle, style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LevelChip(s.themeSystem, settings.theme == "system", { onTheme("system") }, Modifier.weight(1f))
                    LevelChip(s.themeLight, settings.theme == "light", { onTheme("light") }, Modifier.weight(1f))
                    LevelChip(s.themeDark, settings.theme == "dark", { onTheme("dark") }, Modifier.weight(1f))
                }
            }
        }
    )
}

@Composable
private fun NoteDialog(
    initial: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val s = LocalStrings.current
    var text by rememberSaveable { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.noteTitle) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { v -> text = v },
                placeholder = { Text(s.noteHint) },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(text)
                onDismiss()
            }) { Text(s.save) }
        },
        dismissButton = {
            Row {
                if (initial.isNotBlank()) {
                    TextButton(onClick = {
                        onSave("")
                        onDismiss()
                    }) { Text(s.deleteNote) }
                }
                TextButton(onClick = onDismiss) { Text(s.cancel) }
            }
        }
    )
}
