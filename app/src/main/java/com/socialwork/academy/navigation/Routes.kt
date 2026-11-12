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

    // Search and bookmarks (opened from Home and Papers)
    const val SEARCH = "search"
    const val BOOKMARKS = "bookmarks"

    // Quiz flow (opened from Practice)
    const val QUIZ = "quiz"
    const val QUIZ_RESULT = "quiz_result"
    const val QUIZ_REVIEW = "quiz_review"

    // Paper -> topic list -> topic detail
    const val PAPER_ARG = "paperId"
    const val PAPER_TOPICS = "paper/{$PAPER_ARG}"

    const val TOPIC_ARG = "topicId"
    const val TOPIC_DETAIL = "topic/{$TOPIC_ARG}"

    fun paperTopics(paperId: Int) = "paper/$paperId"
    fun topicDetail(topicId: String) = "topic/$topicId"
}
