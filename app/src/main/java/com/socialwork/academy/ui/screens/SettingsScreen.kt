package com.socialwork.academy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.socialwork.academy.data.FontScale
import com.socialwork.academy.ui.AppStrings

/**
 * Phase 1: language and theme switches work for the current session only.
 * Saving them permanently (DataStore) is added in Phase 9.
 */
@Composable
fun SettingsScreen(
    strings: AppStrings,
    isUrdu: Boolean,
    onUrduChange: (Boolean) -> Unit,
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    fontScale: Float,
    onFontScaleChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = strings.settingsTitle, style = MaterialTheme.typography.titleLarge)
        SwitchRow(label = strings.languageLabel, checked = isUrdu, onCheckedChange = onUrduChange)
        SwitchRow(label = strings.darkModeLabel, checked = darkMode, onCheckedChange = onDarkModeChange)
        TextSizeCard(strings = strings, fontScale = fontScale, onFontScaleChange = onFontScaleChange)
    }
}

/** Reading text size with a live sample. The same setting is used in the notes reader. */
@Composable
private fun TextSizeCard(
    strings: AppStrings,
    fontScale: Float,
    onFontScaleChange: (Float) -> Unit
) {
    val base = MaterialTheme.typography.bodyLarge
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = strings.textSizeLabel, style = MaterialTheme.typography.bodyLarge)
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
            Text(
                text = strings.textSizeSample,
                style = base.copy(fontSize = base.fontSize * fontScale, lineHeight = base.lineHeight * fontScale)
            )
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
