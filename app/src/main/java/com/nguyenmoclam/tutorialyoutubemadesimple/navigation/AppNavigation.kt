package com.nguyenmoclam.tutorialyoutubemadesimple.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens.CreateQuizScreen
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens.HomeScreen
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens.QuizDetailScreen
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens.ResultScreen
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens.SettingScreen
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizCreationViewModel
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizViewModel

/**
 * Main navigation component for the app.
 * Handles navigation between different screens and their respective composables.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: QuizViewModel,
    quizViewModel: QuizCreationViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppScreens.Home.route
    ) {
        composable(AppScreens.Home.route) {
            HomeScreen()
        }
        composable(AppScreens.CreateQuiz.route) {
            CreateQuizScreen(viewModel = viewModel, navController = navController, quizViewModel = quizViewModel)
        }
        composable(AppScreens.Result.route) {
            ResultScreen(viewModel = viewModel, navController = navController)
        }
        composable(AppScreens.Settings.route) {
            SettingScreen()
        }
        composable(AppScreens.QuizDetail.route) {
            QuizDetailScreen(navController = navController, quizViewModel = quizViewModel)
        }
    }
}

/**
 * Sealed class representing all available screens in the app.
 * This provides type-safe screen navigation and route management.
 */
sealed class AppScreens(val route: String) {
    object Home : AppScreens("home")
    object CreateQuiz : AppScreens("create_quiz")
    object Result : AppScreens("result")
    object Settings : AppScreens("settings")
    object QuizDetail : AppScreens("quiz_detail")
}