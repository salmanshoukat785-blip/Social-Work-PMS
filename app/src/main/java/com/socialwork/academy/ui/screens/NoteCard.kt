package com.socialwork.academy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.socialwork.academy.ui.StudyStrings
import kotlinx.coroutines.delay

/**
 * The student's own note for a topic. It is saved on the phone a moment after typing stops,
 * and once more when the screen is left, so a note is not lost.
 */
@Composable
fun NoteCard(
    topicId: String,
    st: StudyStrings,
    savedNote: String,
    onSave: (String) -> Unit
) {
    var text by remember(topicId) { mutableStateOf(savedNote) }
    // Until the student types, the box follows the stored note (it may load a moment late).
    var edited by remember(topicId) { mutableStateOf(false) }
    var saved by remember(topicId) { mutableStateOf(false) }

    LaunchedEffect(savedNote) { if (!edited) text = savedNote }

    LaunchedEffect(text, edited) {
        if (edited) {
            delay(700)
            onSave(text)
            saved = true
        }
    }

    val latestText by rememberUpdatedState(text)
    val latestEdited by rememberUpdatedState(edited)
    val latestSave by rememberUpdatedState(onSave)
    DisposableEffect(topicId) {
        onDispose { if (latestEdited) latestSave(latestText) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(st.noteTitle, style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    edited = true
                    saved = false
                },
                placeholder = { Text(st.noteHint) },
                textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Content),
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
            if (saved) {
                Text(
                    st.noteSaved,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (text.isNotBlank()) {
                TextButton(onClick = {
                    text = ""
                    edited = true
                    saved = false
                }) { Text(st.noteDelete) }
            }
        }
    }
}
