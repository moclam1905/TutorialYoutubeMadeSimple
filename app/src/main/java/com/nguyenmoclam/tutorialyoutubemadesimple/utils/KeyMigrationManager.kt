package com.nguyenmoclam.tutorialyoutubemadesimple.utils

import com.nguyenmoclam.tutorialyoutubemadesimple.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles the one-time migration of hardcoded API keys to secure storage.
 * This class ensures that existing API keys from BuildConfig are properly
 * migrated to EncryptedSharedPreferences.
 */
@Singleton
class KeyMigrationManager @Inject constructor(
    private val securePreferences: SecurePreferences
) {
    /**
     * Performs the migration of hardcoded API keys if needed.
     * Only runs once, as tracked by the hasMigratedKeys flag.
     *
     * @return true if migration was performed, false if it was already done
     */
    fun migrateKeysIfNeeded(): Boolean {
        // Skip if already migrated
        if (securePreferences.hasMigratedKeys()) {
            return false
        }

        // Migrate the YouTube API key if it exists
        val youtubeApiKey = BuildConfig.YOUTUBE_API_KEY
        if (youtubeApiKey.isNotBlank() && youtubeApiKey != "null") {
            securePreferences.saveYouTubeApiKey(youtubeApiKey)
        }

        // Migrate the OpenRouter API key if it exists
        val openRouterApiKey = BuildConfig.OPENROUTER_API_KEY
        if (openRouterApiKey.isNotBlank() && openRouterApiKey != "null") {
            securePreferences.saveOpenRouterApiKey(openRouterApiKey)
        }

        // Mark migration as complete
        securePreferences.markKeysMigrated()
        return true
    }
} 