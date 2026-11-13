package com.islamicstudies.academy.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.islamicstudies.academy.BuildConfig
import com.islamicstudies.academy.ui.LocalStrings
import com.islamicstudies.academy.ui.components.LevelChip
import com.islamicstudies.academy.ui.components.PinField
import com.islamicstudies.academy.ui.screenTitleStyle
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val s = LocalStrings.current
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var pinDialog by rememberSaveable { mutableStateOf("") }          // "", "set", "change", "remove"
    var confirmRestore by rememberSaveable { mutableStateOf(false) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.setReminderEnabled(true) else viewModel.reportNotificationsDenied()
    }
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) viewModel.backupTo(uri)
    }
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.restoreFrom(uri)
    }

    LaunchedEffect(message) {
        val code = message
        if (code != null) {
            val text = when (code) {
                "notifications_denied" -> s.notificationsDenied
                "pin_changed" -> s.pinChanged
                "pin_removed" -> s.pinRemoved
                "pin_wrong" -> s.wrongPin
                "backup_ok" -> s.backupOk
                "backup_failed" -> s.backupFailed
                "restore_ok" -> s.restoreOk
                "restore_failed" -> s.restoreFailed
                "reset_done" -> s.resetDone
                else -> ""
            }
            if (text.isNotEmpty()) snackbarHostState.showSnackbar(text)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = s.tileSettings, style = screenTitleStyle()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = s.back
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ---- Profile ----
            SectionCard(title = s.profileSection) {
                var nameText by rememberSaveable(settings.name) { mutableStateOf(settings.name) }
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { v -> nameText = v },
                    label = { Text(s.nameHint) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = { viewModel.saveName(nameText) },
                    enabled = nameText.trim() != settings.name
                ) { Text(s.nameSave) }
            }

            // ---- Language ----
            SectionCard(title = s.appLanguageTitle) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LevelChip(s.langUrdu, settings.language == "ur", { viewModel.setUiLanguage("ur") }, Modifier.weight(1f))
                    LevelChip(s.langEnglish, settings.language == "en", { viewModel.setUiLanguage("en") }, Modifier.weight(1f))
                }
            }

            // ---- Reading ----
            SectionCard(title = s.readingSection) {
                Text(s.readerLanguageTitle, style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LevelChip(s.langFollow, settings.contentLanguage == "", { viewModel.setContentLanguage("") }, Modifier.weight(1.4f))
                    LevelChip(s.langUrdu, settings.contentLanguage == "ur", { viewModel.setContentLanguage("ur") }, Modifier.weight(1f))
                    LevelChip(s.langEnglish, settings.contentLanguage == "en", { viewModel.setContentLanguage("en") }, Modifier.weight(1f))
                    LevelChip(s.langBoth, settings.contentLanguage == "both", { viewModel.setContentLanguage("both") }, Modifier.weight(1f))
                }

                Text(s.defaultLevelTitle, style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LevelChip(s.levelBasic, settings.level == "basic", { viewModel.setLevel("basic") }, Modifier.weight(1f))
                    LevelChip(s.levelIntermediate, settings.level == "intermediate", { viewModel.setLevel("intermediate") }, Modifier.weight(1f))
                    LevelChip(s.levelAdvanced, settings.level == "advanced", { viewModel.setLevel("advanced") }, Modifier.weight(1f))
                }

                Text(s.textSizeTitle, style = MaterialTheme.typography.titleSmall)
                Slider(
                    value = settings.textScalePercent.toFloat(),
                    onValueChange = { v -> viewModel.setTextScale(v.roundToInt()) },
                    valueRange = 80f..160f,
                    steps = 7
                )
                Text("${settings.textScalePercent}%", style = MaterialTheme.typography.bodySmall)

                Text(s.themeTitle, style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LevelChip(s.themeSystem, settings.theme == "system", { viewModel.setTheme("system") }, Modifier.weight(1f))
                    LevelChip(s.themeLight, settings.theme == "light", { viewModel.setTheme("light") }, Modifier.weight(1f))
                    LevelChip(s.themeDark, settings.theme == "dark", { viewModel.setTheme("dark") }, Modifier.weight(1f))
                }
            }

            // ---- Reminder ----
            SectionCard(title = s.reminderSection) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = s.reminderSwitch,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = settings.reminderEnabled,
                        onCheckedChange = { on ->
                            if (!on) {
                                viewModel.setReminderEnabled(false)
                            } else if (Build.VERSION.SDK_INT >= 33 &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.setReminderEnabled(true)
                            }
                        }
                    )
                }
                OutlinedButton(
                    onClick = {
                        android.app.TimePickerDialog(
                            context,
                            { _, hour, minute -> viewModel.setReminderTime(hour, minute) },
                            settings.reminderHour,
                            settings.reminderMinute,
                            android.text.format.DateFormat.is24HourFormat(context)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(s.reminderTime + ":  " + String.format("%02d:%02d", settings.reminderHour, settings.reminderMinute))
                }
            }

            // ---- Security ----
            SectionCard(title = s.securitySection) {
                if (settings.pinEnabled) {
                    OutlinedButton(onClick = { pinDialog = "change" }, modifier = Modifier.fillMaxWidth()) {
                        Text(s.changePinAction)
                    }
                    OutlinedButton(onClick = { pinDialog = "remove" }, modifier = Modifier.fillMaxWidth()) {
                        Text(s.removePinAction)
                    }
                } else {
                    OutlinedButton(onClick = { pinDialog = "set" }, modifier = Modifier.fillMaxWidth()) {
                        Text(s.setPin)
                    }
                }
            }

            // ---- Backup ----
            SectionCard(title = s.backupSection) {
                Text(s.backupDesc, style = MaterialTheme.typography.bodyMedium)
                Button(
                    onClick = { backupLauncher.launch("islamic-studies-backup.json") },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(s.backupAction) }
                OutlinedButton(
                    onClick = { confirmRestore = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(s.restoreAction) }
            }

            // ---- Reset ----
            SectionCard(title = s.resetSection) {
                OutlinedButton(
                    onClick = { confirmReset = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(s.resetAction, color = MaterialTheme.colorScheme.error) }
            }

            // ---- About ----
            SectionCard(title = s.aboutSection) {
                Text(s.aboutText, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = s.versionLabel + ": " + BuildConfig.VERSION_NAME,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (pinDialog.isNotEmpty()) {
        PinDialog(
            mode = pinDialog,
            onDismiss = { pinDialog = "" },
            onSubmit = { oldPin, newPin ->
                when (pinDialog) {
                    "remove" -> viewModel.removePin(oldPin)
                    "change" -> viewModel.changePin(oldPin, newPin)
                    else -> viewModel.changePin(null, newPin)
                }
            }
        )
    }
    if (confirmRestore) {
        ConfirmDialog(
            text = s.restoreConfirm,
            onConfirm = {
                confirmRestore = false
                restoreLauncher.launch(arrayOf("*/*"))
            },
            onDismiss = { confirmRestore = false }
        )
    }
    if (confirmReset) {
        ConfirmDialog(
            text = s.resetConfirm,
            onConfirm = {
                confirmReset = false
                viewModel.resetProgress()
            },
            onDismiss = { confirmReset = false }
        )
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
            content()
        }
    }
}

@Composable
private fun ConfirmDialog(text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val s = LocalStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(s.confirm) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } }
    )
}

@Composable
private fun PinDialog(
    mode: String,
    onDismiss: () -> Unit,
    onSubmit: (oldPin: String, newPin: String) -> Unit
) {
    val s = LocalStrings.current
    var oldPin by rememberSaveable { mutableStateOf("") }
    var newPin by rememberSaveable { mutableStateOf("") }
    var confirmPin by rememberSaveable { mutableStateOf("") }

    val needOld = mode != "set"
    val needNew = mode != "remove"
    val mismatch = needNew && newPin.length == 4 && confirmPin.length == 4 && newPin != confirmPin
    val valid = (!needOld || oldPin.length == 4) && (!needNew || (newPin.length == 4 && newPin == confirmPin))
    val title = when (mode) {
        "remove" -> s.removePinAction
        "change" -> s.changePinAction
        else -> s.setPin
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (needOld) {
                    PinField(value = oldPin, onValueChange = { v -> oldPin = v }, label = s.currentPinHint)
                }
                if (needNew) {
                    PinField(value = newPin, onValueChange = { v -> newPin = v }, label = s.newPinHint)
                    PinField(
                        value = confirmPin,
                        onValueChange = { v -> confirmPin = v },
                        label = s.pinConfirmHint,
                        isError = mismatch
                    )
                    if (mismatch) {
                        Text(
                            text = s.pinMismatch,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSubmit(oldPin, newPin)
                    onDismiss()
                },
                enabled = valid
            ) { Text(s.confirm) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } }
    )
}
