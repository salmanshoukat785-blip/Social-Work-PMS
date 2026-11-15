package com.socialwork.academy.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.socialwork.academy.data.ContentState
import com.socialwork.academy.data.DetailEntry
import com.socialwork.academy.data.FontScale
import com.socialwork.academy.data.ReadingText
import com.socialwork.academy.data.Topic
import com.socialwork.academy.data.TopicDetails
import com.socialwork.academy.data.TopicSection
import com.socialwork.academy.ui.AppStrings
import com.socialwork.academy.ui.DetailStrings
import com.socialwork.academy.ui.StudyStrings
import com.socialwork.academy.ui.theme.hierarchyColors

/**
 * Notes reader for one topic: both-language titles, bookmark, copy, share,
 * reading text size, the "completed" switch and the study material taken
 * from the Word file (English and Urdu lists, as in the file).
 */
@Composable
fun TopicDetailScreen(
    strings: AppStrings,
    isUrdu: Boolean,
    topic: Topic,
    content: ContentState,
    details: TopicDetails?,
    completed: Boolean,
    bookmarked: Boolean,
    fontScale: Float,
    onToggleCompleted: (Boolean) -> Unit,
    onToggleBookmark: () -> Unit,
    onFontScaleChange: (Float) -> Unit,
    st: StudyStrings,
    savedNote: String,
    onNoteSave: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val paperLabel = if (topic.paper == 2) strings.paper2 else strings.paper1
    val sections: List<TopicSection> =
        (content as? ContentState.Ready)?.bundle?.forTopic(topic.id)?.sections.orEmpty()
    val canShareText = sections.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
            }
            Text(
                text = paperLabel,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onToggleBookmark) {
                Icon(
                    imageVector = if (bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = if (bookmarked) strings.bookmarkRemove else strings.bookmarkAdd,
                    tint = if (bookmarked) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                enabled = canShareText,
                onClick = {
                    copyText(context, topic.titleEn, ReadingText.build(topic, sections, isUrdu), strings.copiedMessage)
                }
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = strings.copyLabel)
            }
            IconButton(
                enabled = canShareText,
                onClick = {
                    shareText(context, topic.title(isUrdu), ReadingText.build(topic, sections, isUrdu), strings.shareLabel)
                }
            ) {
                Icon(Icons.Filled.Share, contentDescription = strings.shareLabel)
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = topic.title(isUrdu), style = MaterialTheme.typography.headlineMedium)
        }

        Column(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (completed) {
                OutlinedButton(
                    onClick = { onToggleCompleted(false) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.markNotCompleted)
                }
            } else {
                Button(
                    onClick = { onToggleCompleted(true) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.markCompleted)
                }
            }

            // Reading text size
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = strings.textSizeLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    TextButton(
                        enabled = FontScale.canShrink(fontScale),
                        onClick = { onFontScaleChange(FontScale.smaller(fontScale)) },
                        modifier = Modifier.semantics { contentDescription = strings.smallerText }
                    ) { Text("A-") }
                    TextButton(
                        enabled = FontScale.canGrow(fontScale),
                        onClick = { onFontScaleChange(FontScale.larger(fontScale)) },
                        modifier = Modifier.semantics { contentDescription = strings.largerText }
                    ) { Text("A+") }
                }
            }

            when (content) {
                ContentState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                ContentState.Failed -> InfoCard(text = strings.contentError)

                is ContentState.Ready -> {
                    if (sections.isEmpty()) {
                        InfoCard(text = strings.noSourcePoints)
                    } else {
                        val ds = DetailStrings.of(isUrdu)
                        val entries = details?.entries(isUrdu).orEmpty()
                        val open = remember(topic.id, isUrdu) { mutableStateOf(emptySet<String>()) }
                        val longOpen = remember(topic.id, isUrdu) { mutableStateOf(emptySet<String>()) }
                        val offsets = sections.runningFold(0) { acc, s -> acc + s.items(isUrdu).size }

                        if (entries.isNotEmpty()) DetailsIntro(ds = ds)

                        sections.forEachIndexed { si, section ->
                            val items = section.items(isUrdu)
                            // The explanation is used only when its heading is exactly this point.
                            val matched = items.mapIndexed { i, item ->
                                entries.getOrNull(offsets[si] + i)?.takeIf { it.heading.trim() == item.trim() }
                            }
                            SectionCard(
                                section = section,
                                isUrdu = isUrdu,
                                items = items,
                                details = matched,
                                sectionIndex = si,
                                open = open.value,
                                longOpen = longOpen.value,
                                fontScale = fontScale,
                                ds = ds,
                                onToggle = { key ->
                                    open.value = if (key in open.value) open.value - key else open.value + key
                                },
                                onToggleLong = { key ->
                                    longOpen.value = if (key in longOpen.value) longOpen.value - key else longOpen.value + key
                                },
                                onSetAll = { keys, show ->
                                    open.value = if (show) open.value + keys else open.value - keys
                                }
                            )
                        }
                    }
                }
            }

            NoteCard(topicId = topic.id, st = st, savedNote = savedNote, onSave = onNoteSave)
        }
    }
}

@Composable
private fun SectionCard(
    section: TopicSection,
    isUrdu: Boolean,
    items: List<String>,
    details: List<DetailEntry?>,
    sectionIndex: Int,
    open: Set<String>,
    longOpen: Set<String>,
    fontScale: Float,
    ds: DetailStrings,
    onToggle: (String) -> Unit,
    onToggleLong: (String) -> Unit,
    onSetAll: (Set<String>, Boolean) -> Unit
) {
    val colors = hierarchyColors()
    val heading = section.heading(isUrdu)

    val (container, onContainer, indent) = when (section.level) {
        0 -> Triple(colors.chapter, colors.onChapter, 0)
        2 -> Triple(colors.sub, colors.onSub, 16)
        else -> Triple(colors.heading, colors.onHeading, 0)
    }
    val keys = items.indices.filter { details[it] != null }.map { "$sectionIndex:$it" }.toSet()
    val allOpen = keys.isNotEmpty() && keys.all { it in open }

    Card(
        modifier = Modifier.fillMaxWidth().padding(start = indent.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = container, contentColor = onContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (heading.isNotBlank() || keys.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = heading,
                        style = MaterialTheme.typography.titleMedium,
                        color = onContainer,
                        modifier = Modifier.weight(1f).asHeading()
                    )
                    if (keys.isNotEmpty()) {
                        TextButton(onClick = { onSetAll(keys, !allOpen) }) {
                            Text(if (allOpen) ds.collapseAll else ds.expandAll, color = onContainer)
                        }
                    }
                }
            }
            items.forEachIndexed { i, item ->
                val key = "$sectionIndex:$i"
                PointCard(
                    text = item,
                    detail = details[i],
                    isOpen = key in open,
                    isLong = key in longOpen,
                    fontScale = fontScale,
                    ds = ds,
                    onToggle = { onToggle(key) },
                    onToggleLong = { onToggleLong(key) }
                )
            }
        }
    }
}

private fun copyText(context: Context, label: String, text: String, copiedMessage: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    // Android 13 and newer show their own "copied" confirmation.
    if (Build.VERSION.SDK_INT < 33) {
        Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
    }
}

private fun shareText(context: Context, subject: String, text: String, chooserTitle: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, chooserTitle))
}
