package com.socialwork.academy.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.socialwork.academy.data.TopicCatalog
import com.socialwork.academy.ui.AppStrings
import com.socialwork.academy.ui.StudyStrings

/** All the notes the student has written. Tap one to open its topic and edit it there. */
@Composable
fun MyNotesScreen(
    strings: AppStrings,
    st: StudyStrings,
    isUrdu: Boolean,
    notes: Map<String, String>,
    onOpenTopic: (String) -> Unit,
    onDelete: (String) -> Unit,
    onBack: () -> Unit
) {
    // Same order as the topic list; notes of unknown topics are ignored.
    val rows = remember(notes) {
        TopicCatalog.all.filter { notes[it.id]?.isNotBlank() == true }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        ToolHeader(st.myNotesTitle, strings.back, onBack)
        if (rows.isEmpty()) {
            InfoCard(st.noNotes, Modifier.padding(16.dp))
            return@Column
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    st.notesCount(rows.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(rows, key = { it.id }) { topic ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenTopic(topic.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(start = 14.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(topic.title(isUrdu), style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = notes[topic.id].orEmpty(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDirection = TextDirection.Content
                                ),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { onDelete(topic.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = st.noteDelete)
                        }
                    }
                }
            }
        }
    }
}
