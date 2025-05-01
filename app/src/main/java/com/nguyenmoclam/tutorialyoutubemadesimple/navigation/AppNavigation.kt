package com.nguyenmoclam.tutorialyoutubemadesimple.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseUser
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens.AIModelSettingsScreen
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens.CreateQuizScreen
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens.DetailedModelUsageScreen
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens.HomeScreen
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens.LoginScreen
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens.QuizDetailScreen
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens.QuizSettingScreen
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens.SettingScreen
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens.VideoPlayerWithTranscriptScreen
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.AuthViewModel
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizCreationViewModel
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizDetailViewModel
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizViewModel
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.SettingsViewModel
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.UsageViewModel

/**
 * Main navigation component for the app.
 * Handles navigation between different screens and their respective composables.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppNavigation(
    navController: NavHostController,
    homeScreenLazyListState: LazyListState,
    viewModel: QuizViewModel,
    quizViewModel: QuizCreationViewModel,
    settingsViewModel: SettingsViewModel,
    user: FirebaseUser?,
    modifier: Modifier = Modifier
) {
    // Get AuthViewModel to check authentication state
    val authViewModel: AuthViewModel = hiltViewModel()
    
    // Determine start destination based on auth state
    val startDestination = if (user == null) {
        AppScreens.Login.route
    } else {
        AppScreens.Home.route
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login Screen (New)
        composable(AppScreens.Login.route) {
            LoginScreen(
                navController = navController
            )
        }
        
        // Home Screen - Pass the LazyListState
        composable(AppScreens.Home.route) {
            HomeScreen(
                navController = navController,
                lazyListState = homeScreenLazyListState // Pass the state down
            )
        }
        // Create Quiz Screen
        composable(AppScreens.CreateQuiz.route) {
            CreateQuizScreen(
                viewModel = viewModel,
                navController = navController,
                quizViewModel = quizViewModel,
                settingsViewModel = settingsViewModel,
                authViewModel = authViewModel
            )
        }
        // Settings Screen
        composable(AppScreens.Settings.route) {
            SettingScreen(
                viewModel = settingsViewModel,
                authViewModel = authViewModel,
                navController = navController
            )
        }
        // Detailed Model Usage Screen
        composable(AppScreens.DetailedModelUsage.route) {
            val usageViewModel: UsageViewModel = hiltViewModel()
            DetailedModelUsageScreen(
                navController = navController,
                usageViewModel = usageViewModel
            )
        }
        // AI Model Settings Screen (New)
        composable(AppScreens.AIModelSettings.route) {
            // Pass the existing settingsViewModel instance down
            AIModelSettingsScreen(
                navController = navController,
                settingsViewModel = settingsViewModel,
                authViewModel =  authViewModel
            )
        }
        // Quiz Detail Screen
        composable(
            route = AppScreens.QuizDetail.route + "/{quizId}",
            arguments = listOf(navArgument("quizId") {
                type = NavType.LongType; defaultValue = -1L
            })
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getLong("quizId") ?: -1L
            val quizDetailViewModel: QuizDetailViewModel = hiltViewModel()
            QuizDetailScreen(
                quizId = quizId.toString(),
                navController = navController,
                quizDetailViewModel = quizDetailViewModel,
                quizViewModel = quizViewModel,
                settingsViewModel = settingsViewModel
            )
        }
        // Quiz Setting Screen
        composable(
            route = AppScreens.QuizSetting.route + "/{quizId}",
            arguments = listOf(navArgument("quizId") { type = NavType.LongType })
        ) { backStackEntry ->
            // ViewModel will handle extracting quizId from SavedStateHandle
            QuizSettingScreen(navController = navController)
        }
        // Video Player with Transcript Screen
        composable(
            route = AppScreens.VideoPlayer.route + "/{quizId}/{videoUrl}",
            arguments = listOf(
                navArgument("quizId") { type = NavType.LongType },
                navArgument("videoUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getLong("quizId") ?: -1L
            val videoUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
            VideoPlayerWithTranscriptScreen(
                quizId = quizId,
                videoUrl = videoUrl
            )
        }
    }
}

/**
 * Sealed class representing all available screens in the app.
 * This provides type-safe screen navigation and route management.
 */
sealed class AppScreens(val route: String) {
    object Login : AppScreens("login")
    object Home : AppScreens("home")
    object CreateQuiz : AppScreens("create_quiz")
    object Settings : AppScreens("settings")
    object QuizDetail : AppScreens("quiz_detail")
    object QuizSetting : AppScreens("quiz_setting")
    object VideoPlayer : AppScreens("video_player")
    object DetailedModelUsage : AppScreens("detailed_model_usage")
    object AIModelSettings : AppScreens("ai_model_settings")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}
