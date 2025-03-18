package com.nguyenmoclam.tutorialyoutubemadesimple

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.theme.YouTubeSummaryTheme
import com.nguyenmoclam.tutorialyoutubemadesimple.navigation.AppNavigation
import com.nguyenmoclam.tutorialyoutubemadesimple.navigation.AppScreens
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.CurvedBottomNavigation
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.NavItem
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens.BottomNavigationVisibilityState
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizCreationViewModel
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        const val YOUTUBE_API_KEY: String = BuildConfig.YOUTUBE_API_KEY
        const val OPENROUTER_API_KEY: String = BuildConfig.OPENROUTER_API_KEY
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YouTubeSummaryTheme {
                val quizViewModel: QuizViewModel = viewModel()
                val quizCreationViewModel: QuizCreationViewModel = viewModel()
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestinationRoute = navBackStackEntry?.destination?.route

                // Observe bottom navigation visibility state
                val isBottomNavVisible by BottomNavigationVisibilityState.isVisible

                Surface(color = MaterialTheme.colorScheme.background) {
                    Scaffold(
                        bottomBar = {
                            if (currentDestinationRoute?.startsWith(AppScreens.QuizDetail.route) != true
                                && currentDestinationRoute != AppScreens.Splash.route
                            ) {
                                // Animate bottom navigation visibility
                                AnimatedVisibility(
                                    visible = isBottomNavVisible,
                                    enter = slideInVertically(initialOffsetY = { it }),
                                    exit = slideOutVertically(targetOffsetY = { it })
                                ) {
                                    BottomNavigationBar(navController)
                                }
                            }
                        }
                    ) { innerPadding ->
                        AppNavigation(
                            navController = navController,
                            viewModel = quizViewModel,
                            quizViewModel = quizCreationViewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun BottomNavigationBar(navController: androidx.navigation.NavHostController) {
        val navItems = listOf(
            NavItem(
                title = "Home",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                route = AppScreens.Home.route
            ),
            NavItem(
                title = "Create Quiz",
                selectedIcon = Icons.Filled.Add,
                unselectedIcon = Icons.Filled.Add,
                route = AppScreens.CreateQuiz.route
            ),
            NavItem(
                title = "Settings",
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Filled.Settings,
                route = AppScreens.Settings.route
            )
        )

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        // Find the selected index based on current route
        val selectedIndex = navItems.indexOfFirst { item ->
            currentDestination?.hierarchy?.any { it.route == item.route } == true
        }.takeIf { it >= 0 } ?: 0

        CurvedBottomNavigation(
            items = navItems,
            selectedItemIndex = selectedIndex,
            onItemSelected = { index ->
                val selectedItem = navItems[index]
                navController.navigate(selectedItem.route) {
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
            }
        )
    }
}
