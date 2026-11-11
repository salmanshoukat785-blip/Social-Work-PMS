package com.socialwork.academy.navigation

/** All navigation route names in one place. */
object Routes {
    // First-run flow (top level)
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LANGUAGE = "language"
    const val PROFILE = "profile"
    const val MAIN = "main"

    // Main app tabs (inside MAIN)
    const val HOME = "home"
    const val PAPERS = "papers"
    const val PRACTICE = "practice"
    const val SETTINGS = "settings"

    const val PAPER_ARG = "paperId"
    const val PAPER_DETAIL = "paper/{$PAPER_ARG}"

    fun paperDetail(paperId: Int) = "paper/$paperId"
}
