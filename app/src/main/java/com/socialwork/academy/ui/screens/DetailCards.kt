package com.socialwork.academy.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.socialwork.academy.data.DetailEntry
import com.socialwork.academy.ui.DetailStrings

/** One short note, shown once above the points, saying what the explanations are. */
@Composable
fun DetailsIntro(ds: DetailStrings, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = ds.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.asHeading()
            )
            Text(
                text = ds.note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = ds.tapToOpen,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

/**
 * One point of a heading as its own card. A point that has an explanation opens
 * on tap and shows the short text; "Read more" adds the long text.
 */
@Composable
fun PointCard(
    text: String,
    detail: DetailEntry?,
    isOpen: Boolean,
    isLong: Boolean,
    fontScale: Float,
    ds: DetailStrings,
    onToggle: () -> Unit,
    onToggleLong: () -> Unit,
    modifier: Modifier = Modifier
) {
    val base = MaterialTheme.typography.bodyLarge
    val body = base.copy(fontSize = base.fontSize * fontScale, lineHeight = base.lineHeight * fontScale)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (detail != null) Modifier.clickable(role = Role.Button, onClick = onToggle) else Modifier),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = text, style = body, modifier = Modifier.weight(1f))
                if (detail != null) {
                    Icon(
                        imageVector = if (isOpen) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            if (detail != null && isOpen) {
                SelectionContainer {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = ds.shortLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(text = detail.short, style = body)
                        if (isLong) {
                            Text(
                                text = ds.longLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(text = detail.long, style = body)
                        }
                    }
                }
                TextButton(onClick = onToggleLong) {
                    Text(if (isLong) ds.readLess else ds.readMore)
                }
            }
        }
    }
}
