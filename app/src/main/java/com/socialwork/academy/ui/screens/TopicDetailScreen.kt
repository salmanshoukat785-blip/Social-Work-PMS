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
import com.socialwork.academy.data.FontScale
import com.socialwork.academy.data.ReadingText
import com.socialwork.academy.data.Topic
import com.socialwork.academy.data.TopicSection
import com.socialwork.academy.ui.AppStrings
import com.socialwork.academy.ui.StudyStrings

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
            Text(
                text = topic.otherTitle(isUrdu),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                        Text(
                            text = strings.sourceLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        // Long-press selects text, with the system Copy / Share menu.
                        SelectionContainer {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                sections.forEach { section ->
                                    SectionCard(section = section, isUrdu = isUrdu, fontScale = fontScale)
                                }
                            }
                        }
                    }
                }
            }

            NoteCard(topicId = topic.id, st = st, savedNote = savedNote, onSave = onNoteSave)
        }
    }
}

@Composable
private fun SectionCard(section: TopicSection, isUrdu: Boolean, fontScale: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val heading = section.heading(isUrdu)
            val otherHeading = section.otherHeading(isUrdu)
            if (heading.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = heading, style = MaterialTheme.typography.titleMedium, modifier = Modifier.asHeading())
                    if (otherHeading.isNotBlank()) {
                        Text(
                            text = otherHeading,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // The selected language comes first; both lists are shown as they are in the file.
            if (isUrdu) {
                LanguageBlock(label = "اردو", items = section.itemsUr, rtl = true, fontScale = fontScale)
                LanguageBlock(label = "English", items = section.itemsEn, rtl = false, fontScale = fontScale)
            } else {
                LanguageBlock(label = "English", items = section.itemsEn, rtl = false, fontScale = fontScale)
                LanguageBlock(label = "اردو", items = section.itemsUr, rtl = true, fontScale = fontScale)
            }
        }
    }
}

/** One language's list. Its own text direction, whatever the app language is. */
@Composable
private fun LanguageBlock(label: String, items: List<String>, rtl: Boolean, fontScale: Float) {
    if (items.isEmpty()) return
    val direction = if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    val base = MaterialTheme.typography.bodyLarge
    val itemStyle = base.copy(fontSize = base.fontSize * fontScale, lineHeight = base.lineHeight * fontScale)
    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            items.forEach { item ->
                Text(text = "• $item", style = itemStyle)
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
