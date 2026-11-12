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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.socialwork.academy.data.TopicCatalog
import com.socialwork.academy.navigation.Routes
import com.socialwork.academy.ui.screens.HomeScreen
import com.socialwork.academy.ui.screens.PaperTopicsScreen
import com.socialwork.academy.ui.screens.PapersScreen
import com.socialwork.academy.ui.screens.PracticeScreen
import com.socialwork.academy.ui.screens.SettingsScreen
import com.socialwork.academy.ui.screens.TopicDetailScreen

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
    completedTopics: Set<String>,
    lastTopicId: String?,
    isUrdu: Boolean,
    onUrduChange: (Boolean) -> Unit,
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onTopicOpened: (String) -> Unit,
    onTopicCompletedChange: (String, Boolean) -> Unit
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
                    isUrdu = isUrdu,
                    profileName = profileName,
                    completedTopics = completedTopics,
                    lastTopicId = lastTopicId,
                    onOpenPaper = { navController.navigate(Routes.paperTopics(it)) },
                    onOpenTopic = { navController.navigate(Routes.topicDetail(it)) }
                )
            }
            composable(Routes.PAPERS) {
                PapersScreen(
                    strings = strings,
                    completedTopics = completedTopics,
                    onOpenPaper = { navController.navigate(Routes.paperTopics(it)) }
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
                route = Routes.PAPER_TOPICS,
                arguments = listOf(navArgument(Routes.PAPER_ARG) { type = NavType.IntType })
            ) { entry ->
                val paperId = entry.arguments?.getInt(Routes.PAPER_ARG) ?: 1
                PaperTopicsScreen(
                    strings = strings,
                    isUrdu = isUrdu,
                    paperId = paperId,
                    completedTopics = completedTopics,
                    onOpenTopic = { navController.navigate(Routes.topicDetail(it)) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Routes.TOPIC_DETAIL,
                arguments = listOf(navArgument(Routes.TOPIC_ARG) { type = NavType.StringType })
            ) { entry ->
                val topic = TopicCatalog.byId(entry.arguments?.getString(Routes.TOPIC_ARG))
                if (topic == null) {
                    // Unknown id (should not happen): go back instead of showing a blank screen.
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    LaunchedEffect(topic.id) { onTopicOpened(topic.id) }
                    TopicDetailScreen(
                        strings = strings,
                        isUrdu = isUrdu,
                        topic = topic,
                        completed = topic.id in completedTopics,
                        onToggleCompleted = { onTopicCompletedChange(topic.id, it) },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
