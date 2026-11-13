package com.islamicstudies.academy.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.islamicstudies.academy.AcademyApp
import com.islamicstudies.academy.data.settings.AppSettings
import com.islamicstudies.academy.ui.chapters.ChapterDetailScreen
import com.islamicstudies.academy.ui.chapters.ChaptersScreen
import com.islamicstudies.academy.ui.home.HomeScreen
import com.islamicstudies.academy.ui.lesson.LessonScreen
import com.islamicstudies.academy.ui.saved.BookmarksScreen
import com.islamicstudies.academy.ui.saved.NotesScreen
import com.islamicstudies.academy.ui.lock.LockScreen
import com.islamicstudies.academy.ui.onboarding.OnboardingScreen
import com.islamicstudies.academy.ui.quiz.QuizHomeScreen
import com.islamicstudies.academy.ui.quiz.QuizPlayScreen
import com.islamicstudies.academy.ui.quiz.QuizResultScreen
import com.islamicstudies.academy.ui.progress.ProgressScreen
import com.islamicstudies.academy.ui.search.SearchScreen
import com.islamicstudies.academy.ui.settings.SettingsScreen
import com.islamicstudies.academy.ui.splash.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOCK = "lock"
    const val HOME = "home"
    const val CHAPTERS = "chapters"
    const val CHAPTER = "chapter/{chapterId}"
    const val LESSON = "lesson/{headingId}"
    const val BOOKMARKS = "bookmarks"
    const val NOTES = "notes"
    const val SEARCH = "search"
    const val PROGRESS = "progress"
    const val SETTINGS = "settings"
    const val QUIZ = "quiz"
    const val QUIZ_PLAY = "quiz/play/{mode}/{chapter}/{difficulty}/{count}"
    const val QUIZ_RESULT = "quiz/result/{attemptId}"
    fun quizPlay(mode: String, chapter: String, difficulty: String, count: Int) =
        "quiz/play/$mode/$chapter/$difficulty/$count"
    fun quizResult(attemptId: Long) = "quiz/result/$attemptId"
    fun chapter(id: String) = "chapter/$id"
    fun lesson(id: String) = "lesson/$id"
}

@Composable
fun AppNavHost(settings: AppSettings) {
    val navController = rememberNavController()
    val app = LocalContext.current.applicationContext as AcademyApp

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onFinished = {
                    val destination = when {
                        !settings.onboardingDone -> Routes.ONBOARDING
                        settings.pinEnabled && !app.sessionUnlocked -> Routes.LOCK
                        else -> Routes.HOME
                    }
                    navController.navigate(destination) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.LOCK) {
            LockScreen(
                onUnlocked = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOCK) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onOpenChapters = { navController.navigate(Routes.CHAPTERS) },
                onOpenBookmarks = { navController.navigate(Routes.BOOKMARKS) },
                onOpenNotes = { navController.navigate(Routes.NOTES) },
                onOpenLesson = { id -> navController.navigate(Routes.lesson(id)) },
                onOpenQuiz = { navController.navigate(Routes.QUIZ) },
                onOpenSearch = { navController.navigate(Routes.SEARCH) },
                onOpenProgress = { navController.navigate(Routes.PROGRESS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.CHAPTERS) {
            ChaptersScreen(
                onBack = { navController.popBackStack() },
                onOpenChapter = { id -> navController.navigate(Routes.chapter(id)) }
            )
        }
        composable(
            route = Routes.CHAPTER,
            arguments = listOf(navArgument("chapterId") { type = NavType.StringType })
        ) {
            ChapterDetailScreen(
                onBack = { navController.popBackStack() },
                onOpenLesson = { id -> navController.navigate(Routes.lesson(id)) }
            )
        }
        composable(
            route = Routes.LESSON,
            arguments = listOf(navArgument("headingId") { type = NavType.StringType })
        ) {
            LessonScreen(
                onBack = { navController.popBackStack() },
                onOpenLesson = { id ->
                    navController.navigate(Routes.lesson(id)) {
                        popUpTo(Routes.LESSON) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.BOOKMARKS) {
            BookmarksScreen(
                onBack = { navController.popBackStack() },
                onOpenLesson = { id -> navController.navigate(Routes.lesson(id)) }
            )
        }
        composable(Routes.NOTES) {
            NotesScreen(
                onBack = { navController.popBackStack() },
                onOpenLesson = { id -> navController.navigate(Routes.lesson(id)) }
            )
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onOpenLesson = { id -> navController.navigate(Routes.lesson(id)) }
            )
        }
        composable(Routes.QUIZ) {
            QuizHomeScreen(
                onBack = { navController.popBackStack() },
                onStart = { mode, chapter, difficulty, count ->
                    navController.navigate(Routes.quizPlay(mode, chapter, difficulty, count))
                },
                onOpenAttempt = { id -> navController.navigate(Routes.quizResult(id)) }
            )
        }
        composable(
            route = Routes.QUIZ_PLAY,
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType },
                navArgument("chapter") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType },
                navArgument("count") { type = NavType.IntType }
            )
        ) {
            QuizPlayScreen(
                onBack = { navController.popBackStack() },
                onFinished = { attemptId ->
                    navController.navigate(Routes.quizResult(attemptId)) {
                        popUpTo(Routes.QUIZ_PLAY) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Routes.QUIZ_RESULT,
            arguments = listOf(navArgument("attemptId") { type = NavType.LongType })
        ) {
            QuizResultScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PROGRESS) {
            ProgressScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
