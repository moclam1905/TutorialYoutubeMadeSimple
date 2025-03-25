package com.nguyenmoclam.tutorialyoutubemadesimple.domain.model

/**
 * Domain model representing application settings.
 */
data class Settings(
    // Theme settings
    val themeMode: String = "system", // "light", "dark", or "system"
    
    // Quiz configuration
    val questionOrder: String = "sequential", // "sequential" or "shuffle"
    val maxRetryCount: Int = 1,
    val showAnswerAfterWrong: Boolean = false,
    val autoNextQuestion: Boolean = false,
    
    // Google account
    val isGoogleSignedIn: Boolean = false,
    val transcriptMode: String = "anonymous", // "google" or "anonymous"
    
    // Data management
    val usedStorageBytes: Long = 0,
    val quizCount: Int = 0,
    
    // Network settings
    val isNetworkAvailable: Boolean = true,
    val dataSaverMode: Boolean = false,
    val connectionType: String = "any", // "wifi_only" or "any"
    val connectionTimeout: Int = 120, // in seconds
    val retryPolicy: String = "exponential", // "none", "linear", or "exponential"
    
    // Language
    val appLanguage: String = "system" // "en", "vi", or "system"
)