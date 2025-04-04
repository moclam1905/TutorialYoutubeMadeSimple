package com.nguyenmoclam.tutorialyoutubemadesimple.utils

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

/**
 * Manages offline mode settings.
 * This class stores and retrieves offline settings from SharedPreferences.
 */
@Singleton
class OfflineSettingsManager @Inject constructor(
    context: Context
) {
    companion object {
        private const val PREFS_NAME = "offline_settings"
        private const val KEY_OFFLINE_MODE_ENABLED = "offline_mode_enabled"
        private const val KEY_AUTO_SYNC_WHEN_ONLINE = "auto_sync_when_online"
        private const val KEY_AUTO_DOWNLOAD_CONTENT = "auto_download_content"
        private const val KEY_MAX_OFFLINE_STORAGE = "max_offline_storage"
        private const val KEY_CONNECTION_TYPE_RESTRICTION = "connection_type_restriction"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Check if offline mode is enabled
     * Always returns true so the app works in offline mode when there's no network connection
     */
    fun isOfflineModeEnabled(): Boolean {
        // Always return true so the app works in offline mode when there's no network connection
        return true
    }

    /**
     * Enable/disable offline mode
     * This method no longer changes state as we've removed this option
     */
    fun setOfflineModeEnabled(enabled: Boolean) {
        // Do nothing as we've removed this option
        // Keep this method to avoid errors in places that call it
    }

    /**
     * Check if auto sync should be enabled when online
     */
    fun isAutoSyncWhenOnlineEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_SYNC_WHEN_ONLINE, true)
    }

    /**
     * Enable/disable auto sync when online
     */
    fun setAutoSyncWhenOnlineEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_AUTO_SYNC_WHEN_ONLINE, enabled) }
    }

    /**
     * Check if content should be automatically downloaded for offline use
     */
    fun isAutoDownloadContentEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_DOWNLOAD_CONTENT, false)
    }

    /**
     * Enable/disable automatic content download for offline use
     */
    fun setAutoDownloadContentEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_AUTO_DOWNLOAD_CONTENT, enabled) }
    }

    /**
     * Get maximum offline storage size (in MB)
     */
    fun getMaxOfflineStorage(): Int {
        return prefs.getInt(KEY_MAX_OFFLINE_STORAGE, 500) // Default 500MB
    }

    /**
     * Set maximum offline storage size (in MB)
     */
    fun setMaxOfflineStorage(maxSizeMB: Int) {
        prefs.edit { putInt(KEY_MAX_OFFLINE_STORAGE, maxSizeMB) }
    }

    /**
     * Get connection type restriction
     * @return "any" (default), "wifi_only" or "mobile_only"
     */
    fun getConnectionTypeRestriction(): String {
        return prefs.getString(KEY_CONNECTION_TYPE_RESTRICTION, "any") ?: "any"
    }
    
    /**
     * Set connection type restriction
     * @param type "any", "wifi_only" or "mobile_only"
     */
    @Suppress("unused") // May be used in the future
    fun setConnectionTypeRestriction(type: String) {
        if (type in listOf("any", "wifi_only", "mobile_only")) {
            prefs.edit { putString(KEY_CONNECTION_TYPE_RESTRICTION, type) }
        }
    }

    /**
     * Reset all settings to defaults
     */
    fun resetToDefaults() {
        prefs.edit {
            apply {
                putBoolean(KEY_OFFLINE_MODE_ENABLED, false)
                putBoolean(KEY_AUTO_SYNC_WHEN_ONLINE, true)
                putBoolean(KEY_AUTO_DOWNLOAD_CONTENT, false)
                putInt(KEY_MAX_OFFLINE_STORAGE, 500)
                putString(KEY_CONNECTION_TYPE_RESTRICTION, "any")
            }
        }
    }
}