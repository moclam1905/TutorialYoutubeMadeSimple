package com.nguyenmoclam.tutorialyoutubemadesimple

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nguyenmoclam.tutorialyoutubemadesimple.navigation.AppNavigation
import com.nguyenmoclam.tutorialyoutubemadesimple.navigation.AppScreens
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.NavItem
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.theme.YouTubeSummaryTheme
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.LanguageChangeHelper
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.LocalNetworkStateListener
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.LocalNetworkUtils
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkSnackbarManager
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkStateListener
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizCreationViewModel
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizViewModel
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.SettingsViewModel
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        const val YOUTUBE_API_KEY: String = BuildConfig.YOUTUBE_API_KEY
        const val OPENROUTER_API_KEY: String = BuildConfig.OPENROUTER_API_KEY
    }

    val languageChangeHelper by lazy {
        LanguageChangeHelper()
    }

    @Inject
    lateinit var networkUtils: NetworkUtils

    @Inject
    lateinit var networkStateListener: NetworkStateListener

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get SplashViewModel instance
        val splashViewModel = ViewModelProvider(this)[SplashViewModel::class.java]

        // Create a SnackbarHostState for network notifications
        val snackbarHostState = SnackbarHostState()

        // Handle the splash screen transition
        val splashScreen = installSplashScreen()

        // Keep the splash screen visible until initialization is complete
        splashScreen.setKeepOnScreenCondition {
            !splashViewModel.state.value.isInitialized &&
                    (splashViewModel.state.value.isLoading || splashViewModel.state.value.networkAvailable)
        }

        // Initialize network connectivity monitoring
        networkUtils.observeNetworkConnectivity()

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val themeMode = settingsViewModel.settingsState.themeMode
            val appLanguage = settingsViewModel.settingsState.appLanguage

            // Apply language settings
            val localeList = when (appLanguage) {
                "en" -> LocaleListCompat.create(Locale("en"))
                "vi" -> LocaleListCompat.create(Locale("vi"))
                else -> LocaleListCompat.getEmptyLocaleList() // System default
            }
            languageChangeHelper.changeLanguage(applicationContext, localeList[0]?.language ?: "en")

            // Determine dark theme based on settings
            val isDarkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme() // "system" option
            }

            YouTubeSummaryTheme(darkTheme = isDarkTheme) {
                val quizViewModel: QuizViewModel = viewModel()
                val quizCreationViewModel: QuizCreationViewModel = viewModel()
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // State for HomeScreen's LazyColumn, hoisted to MainActivity
                val homeScreenLazyListState = rememberLazyListState()
                val currentRoute = currentDestination?.route

                // Use the reusable composable to get scroll direction state
                val isScrollingDown by rememberIsScrollingDown(homeScreenLazyListState)

                // Use derivedStateOf for the final visibility calculation
                val isBottomBarVisible by remember(
                    currentRoute,
                    isScrollingDown,
                    remember { derivedStateOf { homeScreenLazyListState.firstVisibleItemIndex } },
                    remember { derivedStateOf { homeScreenLazyListState.firstVisibleItemScrollOffset } }
                ) {
                    derivedStateOf {
                        val isAtTop =
                            homeScreenLazyListState.firstVisibleItemIndex == 0 && homeScreenLazyListState.firstVisibleItemScrollOffset == 0
                        val shouldShowBasedOnScroll =
                            isAtTop || isScrollingDown // Show if at top OR scrolling down

                        val finalVisibility = when (currentRoute) {
                            AppScreens.Home.route -> shouldShowBasedOnScroll
                            AppScreens.CreateQuiz.route, AppScreens.Settings.route -> true
                            else -> false
                        }
                        finalVisibility
                    }
                }

                // Provide NetworkUtils and NetworkStateListener to the composable tree
                CompositionLocalProvider(
                    LocalNetworkUtils provides networkUtils,
                    LocalNetworkStateListener provides networkStateListener
                ) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        // Show network status snackbar
                        NetworkSnackbarManager.NetworkStatusSnackbar(
                            snackbarHostState = snackbarHostState,
                            networkStateListener = networkStateListener
                        )

                        Scaffold(
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            bottomBar = {
                                // Use the derived state value for visibility
                                AnimatedVisibility(
                                    visible = isBottomBarVisible, // Read derived state value
                                    enter = slideInVertically(initialOffsetY = { it }),
                                    exit = slideOutVertically(targetOffsetY = { it })
                                ) {
                                    BottomNavigationBar(navController)
                                }
                            },
                        ) { innerPadding ->
                            AppNavigation(
                                navController = navController,
                                homeScreenLazyListState = homeScreenLazyListState, // Pass the state down
                                viewModel = quizViewModel,
                                quizViewModel = quizCreationViewModel,
                                settingsViewModel = settingsViewModel,
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .navigationBarsPadding()
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister network callback to avoid memory leaks
        networkStateListener.unregisterNetworkCallback()
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

//        CurvedBottomNavigation(
//            items = navItems,
//            selectedItemIndex = selectedIndex,
//            onItemSelected = { index ->
//                val selectedItem = navItems[index]
//                navController.navigate(selectedItem.route) {
//                    // Pop up to the start destination of the graph to
//                    // avoid building up a large stack of destinations
//                    popUpTo(navController.graph.findStartDestination().id) {
//                        saveState = true
//                    }
//                    // Avoid multiple copies of the same destination when
//                    // reselecting the same item
//                    launchSingleTop = true
//                    // Restore state when reselecting a previously selected item
//                    restoreState = true
//                }
//            }
//        )

        NavigationBar {
            navItems.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = if (selectedIndex == index) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title
                        )
                    },
                    label = { Text(item.title) },
                    selected = selectedIndex == index,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }

}

/**
 * A composable function that remembers whether the user is currently scrolling down in a LazyList.
 *
 * @param listState The LazyListState to observe.
 * @return A State<Boolean> which is true if scrolling down, false otherwise.
 */
@Composable
fun rememberIsScrollingDown(listState: androidx.compose.foundation.lazy.LazyListState): State<Boolean> {
    val isScrollingDown = remember { mutableStateOf(false) } // Default to not scrolling down

    LaunchedEffect(listState) { // Key based on the listState object
        var previousOffset = listState.firstVisibleItemScrollOffset

        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collect { currentOffset ->
                val scrollingDown = currentOffset < previousOffset
                if (isScrollingDown.value != scrollingDown) {
                    isScrollingDown.value = scrollingDown
                }
                previousOffset = currentOffset // Update previous offset for the next emission
            }
    }
    return isScrollingDown
}
