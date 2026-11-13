package com.islamicstudies.academy.ui.lock

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.islamicstudies.academy.AcademyApp
import com.islamicstudies.academy.ui.LocalStrings
import com.islamicstudies.academy.ui.components.PinField
import kotlinx.coroutines.launch

class LockViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as AcademyApp

    fun unlock(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = app.settingsRepository.verifyPin(pin)
            if (ok) app.sessionUnlocked = true
            onResult(ok)
        }
    }
}

@Composable
fun LockScreen(
    onUnlocked: () -> Unit,
    viewModel: LockViewModel = viewModel()
) {
    val s = LocalStrings.current
    var pin by rememberSaveable { mutableStateOf("") }
    var wrong by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔒", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = s.unlockTitle,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        PinField(
            value = pin,
            onValueChange = { v ->
                pin = v
                wrong = false
            },
            label = s.pinHint,
            isError = wrong
        )
        if (wrong) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = s.wrongPin,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                viewModel.unlock(pin) { ok ->
                    if (ok) {
                        onUnlocked()
                    } else {
                        wrong = true
                        pin = ""
                    }
                }
            },
            enabled = pin.length == 4
        ) {
            Text(s.unlockButton)
        }
    }
}
