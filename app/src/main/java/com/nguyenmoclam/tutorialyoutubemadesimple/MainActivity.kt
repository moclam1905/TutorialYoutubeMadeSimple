package com.nguyenmoclam.tutorialyoutubemadesimple

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.nguyenmoclam.tutorialyoutubemadesimple.navigation.AppNavigation
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.theme.YouTubeSummaryTheme
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.SummaryViewModel

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
                    AppNavigation(navController = navController, viewModel = viewModel)
                }
            }
        }
    }
}
