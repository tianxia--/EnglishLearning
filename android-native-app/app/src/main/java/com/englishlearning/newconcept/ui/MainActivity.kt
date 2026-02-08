package com.englishlearning.newconcept.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.englishlearning.ui.components.MiniPlayer
import com.englishlearning.ui.navigation.AppNavigation
import com.englishlearning.ui.navigation.NavRoute
import com.englishlearning.ui.screens.player.AudioManager
import com.englishlearning.ui.screens.player.PlaybackStateManager
import com.englishlearning.ui.theme.NewConceptEnglishTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main activity for New Concept English app
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var playbackStateManager: PlaybackStateManager

    @Inject
    lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewConceptEnglishTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    // Bottom navigation items
                    val bottomNavItems = listOf(
                        BottomNavItem(
                            route = NavRoute.Home.route,
                            icon = Icons.Default.Home,
                            label = "首页"
                        ),
                        BottomNavItem(
                            route = NavRoute.Progress.route,
                            icon = Icons.Default.TrendingUp,
                            label = "进度"
                        ),
                        BottomNavItem(
                            route = NavRoute.Flashcards.route,
                            icon = Icons.Default.Style,
                            label = "词汇"
                        ),
                        BottomNavItem(
                            route = NavRoute.Settings.route,
                            icon = Icons.Default.Settings,
                            label = "设置"
                        )
                    )

                    // Calculate bottom padding based on whether mini player and bottom nav are shown
                    val showBottomNav = currentDestination?.route in bottomNavItems.map { it.route }
                    val hasActiveLesson by playbackStateManager.currentLesson.collectAsState()
                    val showMiniPlayer = hasActiveLesson != null

                    Scaffold(
                        bottomBar = {
                            Column {
                                // Mini player (shown when there's an active lesson)
                                if (showMiniPlayer) {
                                    MiniPlayer(
                                        playbackStateManager = playbackStateManager,
                                        audioManager = audioManager,
                                        onExpand = {
                                            // Navigate to player screen with current lesson
                                            playbackStateManager.getCurrentBookId()?.let { bookId ->
                                                playbackStateManager.getCurrentLessonId()?.let { lessonId ->
                                                    navController.navigate(NavRoute.Player.createRoute(bookId, lessonId))
                                                }
                                            }
                                        }
                                    )
                                }

                                // Bottom navigation bar (only for main screens)
                                if (showBottomNav) {
                                    NavigationBar {
                                        bottomNavItems.forEach { item ->
                                            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                                            NavigationBarItem(
                                                selected = selected,
                                                onClick = {
                                                    navController.navigate(item.route) {
                                                        // Pop up to the start destination of the graph to
                                                        // avoid building up a large stack of destinations
                                                        popUpTo(navController.graph.findStartDestination().id) {
                                                            saveState = true
                                                        }
                                                        // Avoid multiple copies of the same destination when
                                                        // reselecting the same item
                                                        launchSingleTop = true
                                                        // Restore state when reselecting a previously selected item
                                                        restoreState = true
                                                    }
                                                },
                                                icon = {
                                                    Icon(
                                                        imageVector = item.icon,
                                                        contentDescription = item.label
                                                    )
                                                },
                                                label = { Text(item.label) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    ) { paddingValues ->
                        AppNavigation(
                            navController = navController,
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)
