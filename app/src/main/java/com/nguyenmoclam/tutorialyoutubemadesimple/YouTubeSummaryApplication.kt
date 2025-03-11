package com.nguyenmoclam.tutorialyoutubemadesimple

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class that serves as the entry point for Hilt dependency injection.
 * The @HiltAndroidApp annotation triggers Hilt's code generation.
 */
@HiltAndroidApp
class YouTubeSummaryApplication : Application() {
    // Application-level initialization can be added here if needed
}