package com.englishlearning.ui.navigation

/**
 * Navigation routes for the app
 */
sealed class NavRoute(val route: String) {
    object Home : NavRoute("home")
    object Progress : NavRoute("progress")
    object Flashcards : NavRoute("flashcards") {
        fun createRoute() = route
    }
    object Settings : NavRoute("settings")

    // Detail screens (not in bottom nav)
    object Player : NavRoute("player/{bookId}/{lessonId}") {
        fun createRoute(bookId: String, lessonId: String) = "player/$bookId/$lessonId"
    }
    object Transcription : NavRoute("transcription/{bookId}/{lessonId}") {
        fun createRoute(bookId: String, lessonId: String) = "transcription/$bookId/$lessonId"
    }
    object Quiz : NavRoute("quiz/{bookId}/{lessonId}") {
        fun createRoute(bookId: String, lessonId: String) = "quiz/$bookId/$lessonId"
    }
    object Speaking : NavRoute("speaking/{bookId}/{lessonId}") {
        fun createRoute(bookId: String, lessonId: String) = "speaking/$bookId/$lessonId"
    }
    object Signature : NavRoute("signature")
}
