package com.socialwork.academy.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.socialwork.academy.data.FontScale
import com.socialwork.academy.ui.AppStrings
import com.socialwork.academy.ui.DataStatus
import com.socialwork.academy.ui.DataStrings

/** Language, theme and text size (all saved on the phone), then backup, reset, about and privacy. */
@Composable
fun SettingsScreen(
    strings: AppStrings,
    isUrdu: Boolean,
    onUrduChange: (Boolean) -> Unit,
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    fontScale: Float,
    onFontScaleChange: (Float) -> Unit,
    ds: DataStrings,
    status: DataStatus?,
    onBackupTo: (Uri) -> Unit,
    onRestoreFrom: (Uri) -> Unit,
    onReset: () -> Unit,
    onStatusShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = strings.settingsTitle, style = MaterialTheme.typography.titleLarge, modifier = Modifier.asHeading())
        SwitchRow(label = strings.languageLabel, checked = isUrdu, onCheckedChange = onUrduChange)
        SwitchRow(label = strings.darkModeLabel, checked = darkMode, onCheckedChange = onDarkModeChange)
        TextSizeCard(strings = strings, fontScale = fontScale, onFontScaleChange = onFontScaleChange)
        DataCard(ds, status, onBackupTo, onRestoreFrom, onReset, onStatusShown)
        InfoSection(ds.aboutTitle, ds.aboutBody + "\n" + ds.version(appVersion()))
        InfoSection(ds.privacyTitle, ds.privacyBody)
    }
}

@Composable
private fun appVersion(): String {
    val context = LocalContext.current
    return remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}

@Composable
private fun InfoSection(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.asHeading())
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Backup, restore and reset. Restore and reset ask for confirmation first. */
@Composable
private fun DataCard(
    ds: DataStrings,
    status: DataStatus?,
    onBackupTo: (Uri) -> Unit,
    onRestoreFrom: (Uri) -> Unit,
    onReset: () -> Unit,
    onStatusShown: () -> Unit
) {
    var confirmRestore by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }

    val createLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> if (uri != null) onBackupTo(uri) }
    val openLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) onRestoreFrom(uri) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(ds.dataTitle, style = MaterialTheme.typography.titleMedium, modifier = Modifier.asHeading())
            if (status != null) {
                Text(
                    ds.statusText(status),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (status == DataStatus.BackupFailed || status == DataStatus.RestoreFailed ||
                        status == DataStatus.RestoreInvalid
                    ) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            Text(ds.backupHelp, style = MaterialTheme.typography.bodySmall)
            Button(
                onClick = {
                    onStatusShown()
                    createLauncher.launch(DataStrings.BACKUP_FILE_NAME)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(ds.backupButton) }
            Text(ds.restoreHelp, style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = { confirmRestore = true }, modifier = Modifier.fillMaxWidth()) {
                Text(ds.restoreButton)
            }
            Text(ds.resetHelp, style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth()) {
                Text(ds.resetButton)
            }
        }
    }

    if (confirmRestore) {
        AlertDialog(
            onDismissRequest = { confirmRestore = false },
            title = { Text(ds.restoreConfirmTitle) },
            text = { Text(ds.restoreConfirmBody) },
            confirmButton = {
                TextButton(onClick = {
                    confirmRestore = false
                    onStatusShown()
                    openLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
                }) { Text(ds.confirm) }
            },
            dismissButton = { TextButton(onClick = { confirmRestore = false }) { Text(ds.cancel) } }
        )
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(ds.resetConfirmTitle) },
            text = { Text(ds.resetConfirmBody) },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    onReset()
                }) { Text(ds.confirm) }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text(ds.cancel) } }
        )
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
                .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            // The row handles the tap; the switch itself only shows the state.
            Switch(checked = checked, onCheckedChange = null)
        }
    }
}
