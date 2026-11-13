package com.socialwork.academy.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.socialwork.academy.data.FontScale
import com.socialwork.academy.navigation.Routes
import com.socialwork.academy.ui.screens.LanguageScreen
import com.socialwork.academy.ui.screens.OnboardingScreen
import com.socialwork.academy.ui.screens.ProfileScreen
import com.socialwork.academy.ui.screens.SplashScreen
import com.socialwork.academy.ui.theme.SocialWorkTheme

/**
 * Root of the app.
 * First run: Splash -> Onboarding -> Language -> Profile -> Main.
 * Later runs: Splash -> Main.
 */
@Composable
fun AcademyApp(viewModel: AppViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsState()
    val systemDark = isSystemInDarkTheme()

    val isUrdu = settings?.isUrdu ?: false
    val darkMode = settings?.darkMode ?: systemDark
    val strings = if (isUrdu) AppStrings.Urdu else AppStrings.English
    val direction = if (isUrdu) LayoutDirection.Rtl else LayoutDirection.Ltr

    SocialWorkTheme(darkTheme = darkMode) {
        CompositionLocalProvider(LocalLayoutDirection provides direction) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val rootNav = rememberNavController()

                NavHost(navController = rootNav, startDestination = Routes.SPLASH) {

                    composable(Routes.SPLASH) {
                        SplashScreen(
                            ready = settings != null,
                            onFinished = {
                                val destination =
                                    if (settings?.setupDone == true) Routes.MAIN else Routes.ONBOARDING
                                rootNav.navigate(destination) {
                                    popUpTo(Routes.SPLASH) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Routes.ONBOARDING) {
                        OnboardingScreen(
                            strings = strings,
                            isUrdu = isUrdu,
                            onToggleLanguage = { viewModel.setUrdu(!isUrdu) },
                            onFinish = { rootNav.navigate(Routes.LANGUAGE) }
                        )
                    }

                    composable(Routes.LANGUAGE) {
                        LanguageScreen(
                            strings = strings,
                            isUrdu = isUrdu,
                            onSelectUrdu = { viewModel.setUrdu(it) },
                            onContinue = { rootNav.navigate(Routes.PROFILE) }
                        )
                    }

                    composable(Routes.PROFILE) {
                        ProfileScreen(
                            strings = strings,
                            onSave = { name, institution ->
                                viewModel.saveProfile(name, institution)
                                rootNav.navigate(Routes.MAIN) {
                                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                                }
                            },
                            onSkip = {
                                viewModel.finishSetupWithoutProfile()
                                rootNav.navigate(Routes.MAIN) {
                                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Routes.MAIN) {
                        MainShell(
                            strings = strings,
                            profileName = settings?.profileName ?: "",
                            completedTopics = settings?.completedTopics ?: emptySet(),
                            lastTopicId = settings?.lastTopicId,
                            isUrdu = isUrdu,
                            onUrduChange = { viewModel.setUrdu(it) },
                            darkMode = darkMode,
                            onDarkModeChange = { viewModel.setDarkMode(it) },
                            onTopicOpened = { viewModel.setLastTopic(it) },
                            onTopicCompletedChange = { id, done -> viewModel.setTopicCompleted(id, done) },
                            bookmarks = settings?.bookmarks ?: emptySet(),
                            fontScale = settings?.fontScale ?: FontScale.DEFAULT,
                            onToggleBookmark = { viewModel.toggleBookmark(it) },
                            onFontScaleChange = { viewModel.setFontScale(it) }
                        )
                    }
                }
            }
        }
    }
}
