package com.englishlearning.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.englishlearning.ui.screens.vocabulary.FlashcardScreen
import com.englishlearning.ui.screens.home.HomeScreen
import com.englishlearning.ui.screens.player.PlayerScreen
import com.englishlearning.ui.screens.quiz.QuizScreen
import com.englishlearning.ui.screens.transcription.TranscriptionScreen
import com.englishlearning.ui.screens.progress.ProgressScreen
import com.englishlearning.ui.screens.settings.SettingsScreen
import com.englishlearning.ui.screens.signature.SignatureScreen

// 页面过渡动画配置
private const val ANIMATION_DURATION = 300

// 进入动画：从右侧滑入 + 淡入
private val enterTransition: EnterTransition = slideInHorizontally(
    initialOffsetX = { it },
    animationSpec = tween(ANIMATION_DURATION)
) + fadeIn(
    animationSpec = tween(ANIMATION_DURATION)
)

// 退出动画：向左侧滑出 + 淡出
private val exitTransition: ExitTransition = slideOutHorizontally(
    targetOffsetX = { -it },
    animationSpec = tween(ANIMATION_DURATION)
) + fadeOut(
    animationSpec = tween(ANIMATION_DURATION)
)

// 返回进入动画：从左侧滑入 + 淡入
private val popEnterTransition: EnterTransition = slideInHorizontally(
    initialOffsetX = { -it },
    animationSpec = tween(ANIMATION_DURATION)
) + fadeIn(
    animationSpec = tween(ANIMATION_DURATION)
)

// 返回退出动画：向右侧滑出 + 淡出
private val popExitTransition: ExitTransition = slideOutHorizontally(
    targetOffsetX = { it },
    animationSpec = tween(ANIMATION_DURATION)
) + fadeOut(
    animationSpec = tween(ANIMATION_DURATION)
)

/**
 * App navigation setup with animations
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = NavRoute.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { enterTransition },
        exitTransition = { exitTransition },
        popEnterTransition = { popEnterTransition },
        popExitTransition = { popExitTransition }
    ) {
        // Home screen
        // Home screen
        composable(NavRoute.Home.route) {
            HomeScreen(
                onNavigateToPlayer = { bookId, lessonId ->
                    navController.navigate(NavRoute.Player.createRoute(bookId, lessonId))
                },
                onNavigateToTranscription = { bookId, lessonId ->
                    navController.navigate(NavRoute.Transcription.createRoute(bookId, lessonId))
                },
                onNavigateToQuiz = { bookId, lessonId ->
                    navController.navigate(NavRoute.Quiz.createRoute(bookId, lessonId))
                },
                onNavigateToFlashcards = {
                    navController.navigate(NavRoute.Flashcards.createRoute())
                },
                onNavigateToSpeaking = { bookId, lessonId ->
                    // For now, if dashboard uses this
                    navController.navigate(NavRoute.Speaking.createRoute(bookId, lessonId))
                }
            )
        }

        // Player screen
        composable(
            route = NavRoute.Player.route,
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType },
                navArgument("lessonId") { type = NavType.StringType }
            )
        ) {
            PlayerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSpeaking = { bookId, lessonId ->
                    navController.navigate(NavRoute.Speaking.createRoute(bookId, lessonId))
                }
            )
        }

        // Transcription exercise screen
        composable(
            route = NavRoute.Transcription.route,
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType },
                navArgument("lessonId") { type = NavType.StringType }
            )
        ) {
            TranscriptionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Quiz screen
        composable(
            route = NavRoute.Quiz.route,
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType },
                navArgument("lessonId") { type = NavType.StringType }
            )
        ) {
            QuizScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Progress screen
        composable(NavRoute.Progress.route) {
            ProgressScreen()
        }

        // Flashcards screen
        composable(NavRoute.Flashcards.route) {
            FlashcardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Settings screen
        composable(NavRoute.Settings.route) {
            SettingsScreen(
                onNavigateToSignature = {
                    navController.navigate(NavRoute.Signature.route)
                }
            )
        }

        // Signature screen
        composable(NavRoute.Signature.route) {
            SignatureScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Speaking screen
        composable(
            route = NavRoute.Speaking.route,
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType },
                navArgument("lessonId") { type = NavType.StringType }
            )
        ) {
            com.englishlearning.ui.screens.speaking.SpeakingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
