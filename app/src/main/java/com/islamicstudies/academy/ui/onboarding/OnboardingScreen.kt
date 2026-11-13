package com.islamicstudies.academy.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.islamicstudies.academy.ui.LocalAppLanguage
import com.islamicstudies.academy.ui.LocalStrings
import com.islamicstudies.academy.ui.components.ChoiceCard
import com.islamicstudies.academy.ui.components.PinField
import com.islamicstudies.academy.ui.theme.UrduText

private const val STEP_LANGUAGE = 0
private const val STEP_PROFILE = 4
private const val STEP_PIN = 5

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    val s = LocalStrings.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var step by rememberSaveable { mutableIntStateOf(STEP_LANGUAGE) }
    var name by rememberSaveable { mutableStateOf("") }
    var level by rememberSaveable { mutableStateOf("basic") }
    var pin by rememberSaveable { mutableStateOf("") }
    var pinConfirm by rememberSaveable { mutableStateOf("") }

    val pinValid = pin.length == 4 && pin == pinConfirm

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StepDots(current = step, total = STEP_PIN + 1)
            Spacer(modifier = Modifier.height(32.dp))
            when (step) {
                STEP_LANGUAGE -> LanguageStep(
                    selected = settings.language,
                    onSelect = { lang -> viewModel.setLanguage(lang) }
                )
                1 -> IntroStep("📚", s.intro1Title, s.intro1Body)
                2 -> IntroStep("🎯", s.intro2Title, s.intro2Body)
                3 -> IntroStep("📴", s.intro3Title, s.intro3Body)
                STEP_PROFILE -> ProfileStep(
                    name = name,
                    onNameChange = { v -> name = v },
                    level = level,
                    onLevelChange = { v -> level = v }
                )
                else -> PinStep(
                    pin = pin,
                    onPinChange = { v -> pin = v },
                    pinConfirm = pinConfirm,
                    onPinConfirmChange = { v -> pinConfirm = v }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > STEP_LANGUAGE) {
                TextButton(onClick = { step -= 1 }) { Text(s.back) }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            if (step == STEP_PIN) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = { viewModel.finish(name, level, null, onFinished) }
                    ) { Text(s.skipPin) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.finish(name, level, pin, onFinished) },
                        enabled = pinValid
                    ) { Text(s.setPin) }
                }
            } else {
                Button(onClick = { step += 1 }) {
                    Text(if (step == STEP_LANGUAGE) s.continueLabel else s.next)
                }
            }
        }
    }
}

@Composable
private fun StepDots(current: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in 0 until total) {
            Box(
                modifier = Modifier
                    .size(if (i == current) 10.dp else 8.dp)
                    .background(
                        if (i == current) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                        CircleShape
                    )
            )
        }
    }
}

@Composable
private fun LanguageStep(selected: String, onSelect: (String) -> Unit) {
    val s = LocalStrings.current
    Text("🌙", fontSize = 56.sp)
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = s.chooseLanguage,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(24.dp))
    ChoiceCard(
        label = "اردو",
        selected = selected == "ur",
        onClick = { onSelect("ur") },
        textStyle = UrduText.title
    )
    Spacer(modifier = Modifier.height(12.dp))
    ChoiceCard(
        label = "English",
        selected = selected == "en",
        onClick = { onSelect("en") }
    )
}

@Composable
private fun IntroStep(emoji: String, title: String, body: String) {
    val isUrdu = LocalAppLanguage.current == "ur"
    Text(emoji, fontSize = 72.sp)
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = title,
        style = if (isUrdu) UrduText.title else MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = body,
        style = if (isUrdu) UrduText.body else MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun ProfileStep(
    name: String,
    onNameChange: (String) -> Unit,
    level: String,
    onLevelChange: (String) -> Unit
) {
    val s = LocalStrings.current
    Text(
        text = s.profileTitle,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(20.dp))
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text(s.nameHint) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = s.levelTitle,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    ChoiceCard(label = s.levelBasic, selected = level == "basic", onClick = { onLevelChange("basic") })
    Spacer(modifier = Modifier.height(8.dp))
    ChoiceCard(
        label = s.levelIntermediate,
        selected = level == "intermediate",
        onClick = { onLevelChange("intermediate") }
    )
    Spacer(modifier = Modifier.height(8.dp))
    ChoiceCard(
        label = s.levelAdvanced,
        selected = level == "advanced",
        onClick = { onLevelChange("advanced") }
    )
}

@Composable
private fun PinStep(
    pin: String,
    onPinChange: (String) -> Unit,
    pinConfirm: String,
    onPinConfirmChange: (String) -> Unit
) {
    val s = LocalStrings.current
    val mismatch = pin.length == 4 && pinConfirm.length == 4 && pin != pinConfirm
    Text("🔒", fontSize = 56.sp)
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = s.pinTitle,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = s.pinBody,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(20.dp))
    PinField(value = pin, onValueChange = onPinChange, label = s.pinHint)
    Spacer(modifier = Modifier.height(12.dp))
    PinField(
        value = pinConfirm,
        onValueChange = onPinConfirmChange,
        label = s.pinConfirmHint,
        isError = mismatch
    )
    if (mismatch) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = s.pinMismatch,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
