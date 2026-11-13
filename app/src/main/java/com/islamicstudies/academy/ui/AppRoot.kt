package com.islamicstudies.academy.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.islamicstudies.academy.AcademyApp
import com.islamicstudies.academy.ImportState
import com.islamicstudies.academy.ui.theme.AcademyTheme

@Composable
fun AppRoot(viewModel: AppViewModel = viewModel()) {
    val app = LocalContext.current.applicationContext as AcademyApp
    val importState by app.importState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val isEnglish = settings.language == "en"
    val strings = if (isEnglish) EnStrings else UrStrings
    val direction = if (isEnglish) LayoutDirection.Ltr else LayoutDirection.Rtl
    val state = importState
    val darkTheme = when (settings.theme) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    AcademyTheme(darkTheme = darkTheme) {
        CompositionLocalProvider(
            LocalStrings provides strings,
            LocalAppLanguage provides settings.language,
            LocalLayoutDirection provides direction
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                when {
                    state is ImportState.Failed -> ErrorScreen(state.message)
                    state is ImportState.Loading || !settings.loaded -> LoadingScreen()
                    else -> AppNavHost(settings = settings)
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorScreen(message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Could not load the course content.", style = MaterialTheme.typography.titleMedium)
        Text(message, style = MaterialTheme.typography.bodyMedium)
    }
}
