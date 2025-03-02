package com.nguyenmoclam.tutorialyoutubemadesimple.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens.HomeScreen
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens.ResultScreen
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.SummaryViewModel

/**
 * Main navigation component for the app.
 * Handles navigation between different screens and their respective composables.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: SummaryViewModel
) {
    NavHost(
        navController = navController,
        startDestination = AppScreens.Home.route
    ) {
        composable(AppScreens.Home.route) {
            HomeScreen(viewModel = viewModel, navController = navController)
        }
        composable(AppScreens.Result.route) {
            ResultScreen(viewModel = viewModel, navController = navController)
        }
    }
}

/**
 * Sealed class representing all available screens in the app.
 * This provides type-safe screen navigation and route management.
 */
sealed class AppScreens(val route: String) {
    object Home : AppScreens("home")
    object Result : AppScreens("result")
}