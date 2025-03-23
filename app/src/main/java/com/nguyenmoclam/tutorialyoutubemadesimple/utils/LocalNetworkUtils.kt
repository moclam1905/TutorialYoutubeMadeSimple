package com.nguyenmoclam.tutorialyoutubemadesimple.utils

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal to provide NetworkUtils to Composable
 * This helps Composable easily access NetworkUtils through dependency injection
 */
val LocalNetworkUtils = staticCompositionLocalOf<NetworkUtils> {
    error("NetworkUtils not provided. Please ensure you provide NetworkUtils through CompositionLocalProvider")
}