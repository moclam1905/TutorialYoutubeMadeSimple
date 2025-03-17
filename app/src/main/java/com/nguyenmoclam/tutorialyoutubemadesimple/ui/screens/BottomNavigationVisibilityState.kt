package com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens

import androidx.compose.runtime.mutableStateOf

/**
 * Singleton object to manage bottom navigation visibility state across the app.
 * This allows the HomeScreen to communicate scroll state to the MainActivity
 * for controlling bottom navigation visibility.
 */
object BottomNavigationVisibilityState {
    // State to track if bottom navigation should be visible
    val isVisible = mutableStateOf(true)
}