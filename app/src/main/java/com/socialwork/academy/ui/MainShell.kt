package com.socialwork.academy.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.socialwork.academy.navigation.Routes
import com.socialwork.academy.ui.screens.HomeScreen
import com.socialwork.academy.ui.screens.PaperDetailScreen
import com.socialwork.academy.ui.screens.PapersScreen
import com.socialwork.academy.ui.screens.PracticeScreen
import com.socialwork.academy.ui.screens.SettingsScreen

private data class Tab(
    val route: String,
    val icon: ImageVector,
    val label: (AppStrings) -> String
)

private val tabs = listOf(
    Tab(Routes.HOME, Icons.Filled.Home) { it.tabHome },
    Tab(Routes.PAPERS, Icons.AutoMirrored.Filled.MenuBook) { it.tabPapers },
    Tab(Routes.PRACTICE, Icons.Filled.Quiz) { it.tabPractice },
    Tab(Routes.SETTINGS, Icons.Filled.Settings) { it.tabSettings }
)

/** The main app (bottom tabs). Shown after the first-run setup is finished. */
@Composable
fun MainShell(
    strings: AppStrings,
    profileName: String,
    isUrdu: Boolean,
    onUrduChange: (Boolean) -> Unit,
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label(strings)) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    strings = strings,
                    profileName = profileName,
                    onOpenPaper = { navController.navigate(Routes.paperDetail(it)) }
                )
            }
            composable(Routes.PAPERS) {
                PapersScreen(
                    strings = strings,
                    onOpenPaper = { navController.navigate(Routes.paperDetail(it)) }
                )
            }
            composable(Routes.PRACTICE) {
                PracticeScreen(strings = strings)
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    strings = strings,
                    isUrdu = isUrdu,
                    onUrduChange = onUrduChange,
                    darkMode = darkMode,
                    onDarkModeChange = onDarkModeChange
                )
            }
            composable(
                route = Routes.PAPER_DETAIL,
                arguments = listOf(navArgument(Routes.PAPER_ARG) { type = NavType.IntType })
            ) { entry ->
                val paperId = entry.arguments?.getInt(Routes.PAPER_ARG) ?: 1
                PaperDetailScreen(
                    strings = strings,
                    paperId = paperId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
