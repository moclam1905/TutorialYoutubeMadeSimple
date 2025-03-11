package com.nguyenmoclam.tutorialyoutubemadesimple

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nguyenmoclam.tutorialyoutubemadesimple.navigation.AppNavigation
import com.nguyenmoclam.tutorialyoutubemadesimple.navigation.AppScreens
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.theme.YouTubeSummaryTheme
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.SummaryViewModel
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
                // Initialize ViewModel and NavController for Compose
                val viewModel: SummaryViewModel = viewModel()
                val navController = rememberNavController()
                Surface(color = MaterialTheme.colorScheme.background) {
                    Scaffold(
                        bottomBar = {
                            BottomNavigationBar(navController)
                        }
                    ) { innerPadding ->
                        AppNavigation(
                            navController = navController,
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
    
    @Composable
    private fun BottomNavigationBar(navController: androidx.navigation.NavHostController) {
        val items = listOf(
            Triple(AppScreens.Home, "Home", Icons.Filled.Home),
            Triple(AppScreens.CreateQuiz, "Create Quiz", Icons.Filled.Create),
            Triple(AppScreens.Settings, "Settings", Icons.Filled.Settings)
        )
        
        NavigationBar {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            items.forEach { (screen, label, icon) ->
                NavigationBarItem(
                    icon = { Icon(icon, contentDescription = label) },
                    label = { Text(label) },
                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                    onClick = {
                        navController.navigate(screen.route) {
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
    }
}
