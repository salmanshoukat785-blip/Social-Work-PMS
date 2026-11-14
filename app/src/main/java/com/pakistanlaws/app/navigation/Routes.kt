package com.pakistanlaws.app.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.ui.graphics.vector.ImageVector
import com.pakistanlaws.app.R

/** Top-level screens. Splash, onboarding and auth are added in Part 2. */
object Routes {
    const val MAIN = "main"
}

/** The five bottom-navigation tabs. */
enum class BottomTab(
    val route: String,
    @StringRes val label: Int,
    val icon: ImageVector
) {
    HOME("home", R.string.tab_home, Icons.Filled.Home),
    LIBRARY("library", R.string.tab_library, Icons.AutoMirrored.Filled.MenuBook),
    PRACTICE("practice", R.string.tab_practice, Icons.Filled.Quiz),
    STUDY("study", R.string.tab_study, Icons.Filled.Bookmarks),
    PROFILE("profile", R.string.tab_profile, Icons.Filled.Person)
}
