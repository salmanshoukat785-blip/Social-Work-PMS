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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.socialwork.academy.data.DetailEntry
import com.socialwork.academy.ui.DetailStrings

/** "Additional Explanation": one expandable card per sub-heading, short + long. */
@Composable
fun DetailCards(
    entries: List<DetailEntry>,
    isUrdu: Boolean,
    fontScale: Float,
    ds: DetailStrings,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) return
    val open = remember(entries) { mutableStateOf(emptySet<Int>()) }
    val direction = if (isUrdu) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = ds.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.asHeading()
            )
            Text(
                text = ds.note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ds.tapToOpen,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                val allOpen = open.value.size == entries.size
                TextButton(onClick = {
                    open.value = if (allOpen) emptySet() else entries.indices.toSet()
                }) { Text(if (allOpen) ds.collapseAll else ds.expandAll) }
            }
            entries.forEachIndexed { index, entry ->
                val isOpen = index in open.value
                DetailCard(
                    entry = entry,
                    isOpen = isOpen,
                    fontScale = fontScale,
                    ds = ds,
                    onToggle = {
                        open.value = if (isOpen) open.value - index else open.value + index
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailCard(
    entry: DetailEntry,
    isOpen: Boolean,
    fontScale: Float,
    ds: DetailStrings,
    onToggle: () -> Unit
) {
    val base = MaterialTheme.typography.bodyLarge
    val body = base.copy(fontSize = base.fontSize * fontScale, lineHeight = base.lineHeight * fontScale)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = onToggle),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.heading,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isOpen) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null
                )
            }
            if (isOpen) {
                SelectionContainer {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = ds.shortLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(text = entry.short, style = body)
                        Text(
                            text = ds.longLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(text = entry.long, style = body)
                    }
                }
            }
        }
    }
}
