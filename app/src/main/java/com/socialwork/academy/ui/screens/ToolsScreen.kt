package com.socialwork.academy.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.socialwork.academy.ui.DataStrings
import com.socialwork.academy.ui.StudyStrings

@Composable
fun ToolsScreen(
    st: StudyStrings,
    ds: DataStrings,
    onOpenProgress: () -> Unit,
    onOpenGlossary: () -> Unit,
    onOpenFlashcards: () -> Unit,
    onOpenMyNotes: () -> Unit,
    onOpenPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(st.toolsTitle, style = MaterialTheme.typography.titleLarge, modifier = Modifier.asHeading())
        ToolCard(Icons.Filled.Insights, ds.progressTitle, ds.progressDesc, onOpenProgress)
        ToolCard(Icons.Filled.Translate, st.glossaryTitle, st.glossaryDesc, onOpenGlossary)
        ToolCard(Icons.Filled.Style, st.flashcardsTitle, st.flashcardsDesc, onOpenFlashcards)
        ToolCard(Icons.Filled.Edit, st.myNotesTitle, st.myNotesDesc, onOpenMyNotes)
        ToolCard(Icons.Filled.PictureAsPdf, st.pdfTitle, st.pdfDesc, onOpenPdf)
    }
}

@Composable
private fun ToolCard(icon: ImageVector, title: String, desc: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
