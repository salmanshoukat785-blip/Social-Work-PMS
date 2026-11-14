package com.socialwork.academy.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Build
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.socialwork.academy.data.ContentRepository
import com.socialwork.academy.data.ContentState
import com.socialwork.academy.data.DetailsBundle
import com.socialwork.academy.data.DetailsRepository
import com.socialwork.academy.data.TopicCatalog
import com.socialwork.academy.navigation.Routes
import com.socialwork.academy.ui.screens.BookmarksScreen
import com.socialwork.academy.ui.screens.HomeScreen
import com.socialwork.academy.ui.screens.PaperTopicsScreen
import com.socialwork.academy.ui.screens.PapersScreen
import com.socialwork.academy.ui.screens.FlashcardsScreen
import com.socialwork.academy.ui.screens.GlossaryScreen
import com.socialwork.academy.ui.screens.MockSetupScreen
import com.socialwork.academy.ui.screens.MyNotesScreen
import com.socialwork.academy.ui.screens.PdfReaderScreen
import com.socialwork.academy.ui.screens.ToolsScreen
import com.socialwork.academy.ui.screens.PracticeScreen
import com.socialwork.academy.ui.screens.ProgressScreen
import com.socialwork.academy.ui.screens.QuizResultScreen
import com.socialwork.academy.ui.screens.QuizReviewScreen
import com.socialwork.academy.ui.screens.QuizScreen
import com.socialwork.academy.ui.screens.SearchScreen
import com.socialwork.academy.ui.screens.SettingsScreen
import com.socialwork.academy.ui.screens.TopicDetailScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class Tab(
    val route: String,
    val icon: ImageVector,
    val label: (AppStrings) -> String
)

/** The tab that should look selected on a screen opened from it (e.g. Glossary belongs to Tools). */
private fun tabRouteFor(route: String?): String? = when (route) {
    Routes.GLOSSARY, Routes.FLASHCARDS, Routes.MY_NOTES, Routes.PDF_READER, Routes.PROGRESS -> Routes.TOOLS
    Routes.MOCK, Routes.QUIZ, Routes.QUIZ_RESULT, Routes.QUIZ_REVIEW -> Routes.PRACTICE
    else -> route
}

private val tabs = listOf(
    Tab(Routes.HOME, Icons.Filled.Home) { it.tabHome },
    Tab(Routes.PAPERS, Icons.AutoMirrored.Filled.MenuBook) { it.tabPapers },
    Tab(Routes.PRACTICE, Icons.Filled.Quiz) { it.tabPractice },
    Tab(Routes.TOOLS, Icons.Filled.Build) { it.tabTools },
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
    onTopicCompletedChange: (String, Boolean) -> Unit,
    bookmarks: Set<String>,
    fontScale: Float,
    onToggleBookmark: (String) -> Unit,
    onFontScaleChange: (Float) -> Unit
) {
    val context = LocalContext.current
    // Study content is read once from the app's bundled files (works offline).
    val contentState by produceState<ContentState>(ContentState.Loading) {
        val bundle = withContext(Dispatchers.IO) { ContentRepository.load(context) }
        value = if (bundle != null) ContentState.Ready(bundle) else ContentState.Failed
    }
    // "Additional Explanation" texts; if unreadable they are just not shown.
    val detailsBundle by produceState<DetailsBundle?>(null) {
        value = withContext(Dispatchers.IO) { DetailsRepository.load(context) }
    }

    val quizVm: QuizViewModel = viewModel()
    val quizStrings = QuizStrings.forLanguage(isUrdu)
    val mcqState by quizVm.mcq.collectAsState()
    val attempts by quizVm.attempts.collectAsState()
    val topicStats by quizVm.topicStats.collectAsState()
    val session by quizVm.session.collectAsState()
    val reviewItems by quizVm.review.collectAsState()

    val studyVm: StudyViewModel = viewModel()
    val studyStrings = StudyStrings.forLanguage(isUrdu)
    val notes by studyVm.notes.collectAsState()
    val knownCards by studyVm.knownCards.collectAsState()
    val glossary by studyVm.glossary.collectAsState()

    val dataVm: DataViewModel = viewModel()
    val dataStrings = DataStrings.forLanguage(isUrdu)
    val activity by dataVm.activity.collectAsState()
    val dataStatus by dataVm.status.collectAsState()

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // The tab bar is hidden while a quiz is on screen so it is not left by accident.
            val inQuiz = currentRoute == Routes.QUIZ || currentRoute == Routes.QUIZ_RESULT ||
                currentRoute == Routes.QUIZ_REVIEW
            if (!inQuiz) NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = tabRouteFor(currentRoute) == tab.route,
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
                    bookmarkCount = bookmarks.size,
                    onOpenPaper = { navController.navigate(Routes.paperTopics(it)) },
                    onOpenTopic = { navController.navigate(Routes.topicDetail(it)) },
                    onOpenSearch = { navController.navigate(Routes.SEARCH) },
                    onOpenBookmarks = { navController.navigate(Routes.BOOKMARKS) }
                )
            }
            composable(Routes.PAPERS) {
                PapersScreen(
                    strings = strings,
                    completedTopics = completedTopics,
                    bookmarkCount = bookmarks.size,
                    onOpenPaper = { navController.navigate(Routes.paperTopics(it)) },
                    onOpenSearch = { navController.navigate(Routes.SEARCH) },
                    onOpenBookmarks = { navController.navigate(Routes.BOOKMARKS) }
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(
                    strings = strings,
                    isUrdu = isUrdu,
                    content = contentState,
                    onOpenTopic = { navController.navigate(Routes.topicDetail(it)) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.BOOKMARKS) {
                BookmarksScreen(
                    strings = strings,
                    isUrdu = isUrdu,
                    bookmarks = bookmarks,
                    onOpenTopic = { navController.navigate(Routes.topicDetail(it)) },
                    onRemoveBookmark = onToggleBookmark,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.PRACTICE) {
                PracticeScreen(
                    strings = strings,
                    qs = quizStrings,
                    isUrdu = isUrdu,
                    mcq = mcqState,
                    attempts = attempts,
                    topicStats = topicStats,
                    onStart = { config ->
                        if (quizVm.start(config)) navController.navigate(Routes.QUIZ)
                    },
                    onOpenAttempt = { attempt ->
                        quizVm.openReviewOf(attempt)
                        navController.navigate(Routes.QUIZ_REVIEW)
                    },
                    onOpenMock = { navController.navigate(Routes.MOCK) }
                )
            }
            composable(Routes.MOCK) {
                MockSetupScreen(
                    strings = strings,
                    qs = quizStrings,
                    isUrdu = isUrdu,
                    mcq = mcqState,
                    attempts = attempts,
                    onStart = { config ->
                        if (quizVm.start(config)) navController.navigate(Routes.QUIZ)
                    },
                    onOpenAttempt = { attempt ->
                        quizVm.openReviewOf(attempt)
                        navController.navigate(Routes.QUIZ_REVIEW)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.QUIZ) {
                QuizScreen(
                    strings = strings,
                    qs = quizStrings,
                    session = session,
                    onAnswer = quizVm::answer,
                    onGoTo = quizVm::goTo,
                    onToggleMark = quizVm::toggleMark,
                    onFinish = quizVm::finish,
                    onFinished = {
                        navController.navigate(Routes.QUIZ_RESULT) {
                            popUpTo(Routes.QUIZ) { inclusive = true }
                        }
                    },
                    onLeave = { navController.popBackStack() }
                )
            }
            composable(Routes.QUIZ_RESULT) {
                QuizResultScreen(
                    strings = strings,
                    qs = quizStrings,
                    isUrdu = isUrdu,
                    session = session,
                    onReview = {
                        quizVm.openReviewOfCurrent()
                        navController.navigate(Routes.QUIZ_REVIEW)
                    },
                    onRetryIncorrect = {
                        if (quizVm.retryIncorrect()) {
                            navController.navigate(Routes.QUIZ) {
                                popUpTo(Routes.QUIZ_RESULT) { inclusive = true }
                            }
                        }
                    },
                    onDone = {
                        navController.popBackStack(Routes.PRACTICE, inclusive = false)
                    }
                )
            }
            composable(Routes.QUIZ_REVIEW) {
                QuizReviewScreen(
                    strings = strings,
                    qs = quizStrings,
                    items = reviewItems,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.TOOLS) {
                ToolsScreen(
                    st = studyStrings,
                    ds = dataStrings,
                    onOpenProgress = { navController.navigate(Routes.PROGRESS) },
                    onOpenGlossary = { navController.navigate(Routes.GLOSSARY) },
                    onOpenFlashcards = { navController.navigate(Routes.FLASHCARDS) },
                    onOpenMyNotes = { navController.navigate(Routes.MY_NOTES) },
                    onOpenPdf = { navController.navigate(Routes.PDF_READER) }
                )
            }
            composable(Routes.PROGRESS) {
                ProgressScreen(
                    strings = strings,
                    ds = dataStrings,
                    qs = quizStrings,
                    isUrdu = isUrdu,
                    completedTopics = completedTopics,
                    lastTopicId = lastTopicId,
                    attempts = attempts,
                    topicStats = topicStats,
                    activity = activity,
                    onContinue = { navController.navigate(Routes.topicDetail(it)) },
                    onOpenAttempt = { attempt ->
                        quizVm.openReviewOf(attempt)
                        navController.navigate(Routes.QUIZ_REVIEW)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.GLOSSARY) {
                GlossaryScreen(
                    strings = strings, st = studyStrings, isUrdu = isUrdu, state = glossary,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.FLASHCARDS) {
                FlashcardsScreen(
                    strings = strings, st = studyStrings, isUrdu = isUrdu, state = glossary,
                    known = knownCards,
                    onSetKnown = studyVm::setKnown,
                    onClearKnown = studyVm::clearKnown,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.MY_NOTES) {
                MyNotesScreen(
                    strings = strings, st = studyStrings, isUrdu = isUrdu, notes = notes,
                    onOpenTopic = { navController.navigate(Routes.topicDetail(it)) },
                    onDelete = { studyVm.saveNote(it, "") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.PDF_READER) {
                PdfReaderScreen(
                    strings = strings, st = studyStrings,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    strings = strings,
                    isUrdu = isUrdu,
                    onUrduChange = onUrduChange,
                    darkMode = darkMode,
                    onDarkModeChange = onDarkModeChange,
                    fontScale = fontScale,
                    onFontScaleChange = onFontScaleChange,
                    ds = dataStrings,
                    status = dataStatus,
                    onBackupTo = dataVm::backupTo,
                    onRestoreFrom = dataVm::restoreFrom,
                    onReset = dataVm::resetProgress,
                    onStatusShown = dataVm::clearStatus
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
                    bookmarks = bookmarks,
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
                    LaunchedEffect(topic.id) {
                        onTopicOpened(topic.id)
                        dataVm.recordTopicOpened(topic.id)
                    }
                    TopicDetailScreen(
                        strings = strings,
                        isUrdu = isUrdu,
                        topic = topic,
                        content = contentState,
                        details = detailsBundle?.forTopic(topic.id),
                        completed = topic.id in completedTopics,
                        bookmarked = topic.id in bookmarks,
                        fontScale = fontScale,
                        onToggleCompleted = { onTopicCompletedChange(topic.id, it) },
                        onToggleBookmark = { onToggleBookmark(topic.id) },
                        onFontScaleChange = onFontScaleChange,
                        st = studyStrings,
                        savedNote = notes[topic.id].orEmpty(),
                        onNoteSave = { studyVm.saveNote(topic.id, it) },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
