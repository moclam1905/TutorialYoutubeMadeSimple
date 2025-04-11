package com.nguyenmoclam.tutorialyoutubemadesimple.utils

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal to provide NetworkStateListener for Composables
 * This helps Composables easily access NetworkStateListener through dependency injection
 */
val LocalNetworkStateListener = staticCompositionLocalOf<NetworkStateListener> {
    error("NetworkStateListener not provided. Make sure to provide NetworkStateListener through CompositionLocalProvider")
}