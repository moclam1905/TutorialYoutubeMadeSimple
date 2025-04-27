package com.nguyenmoclam.tutorialyoutubemadesimple.utils

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for securely storing and retrieving sensitive data such as API keys
 * using Android's EncryptedSharedPreferences with AES256_GCM encryption.
 */
@Singleton
class SecurePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val ENCRYPTED_PREFS_FILE_NAME = "secure_api_keys"
        private const val KEY_OPENROUTER_API_KEY = "openrouter_api_key"
        private const val KEY_YOUTUBE_API_KEY = "youtube_api_key"
        private const val KEY_SELECTED_MODEL_ID = "selected_model_id"
        private const val KEY_HAS_MIGRATED = "has_migrated_keys"
    }

    // Create or retrieve the Master Key for encryption
    private val masterKey: MasterKey by lazy {
        // Build the KeyGenParameterSpec first
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            MasterKey.DEFAULT_MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setKeySize(256)
        .build()
        
        // Set the spec before the scheme
        MasterKey.Builder(context)
            .setKeyGenParameterSpec(keyGenParameterSpec)
            .build()
    }

    // Create or retrieve the EncryptedSharedPreferences
    private val securePrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Saves the OpenRouter API key securely.
     * @param apiKey The API key to be stored
     */
    fun saveOpenRouterApiKey(apiKey: String) {
        securePrefs.edit().putString(KEY_OPENROUTER_API_KEY, apiKey).apply()
    }

    /**
     * Retrieves the securely stored OpenRouter API key.
     * @return The stored API key or empty string if not found
     */
    fun getOpenRouterApiKey(): String {
        return securePrefs.getString(KEY_OPENROUTER_API_KEY, "") ?: ""
    }

    /**
     * Saves the YouTube API key securely.
     * @param apiKey The API key to be stored
     */
    fun saveYouTubeApiKey(apiKey: String) {
        securePrefs.edit().putString(KEY_YOUTUBE_API_KEY, apiKey).apply()
    }

    /**
     * Retrieves the securely stored YouTube API key.
     * @return The stored API key or empty string if not found
     */
    fun getYouTubeApiKey(): String {
        return securePrefs.getString(KEY_YOUTUBE_API_KEY, "") ?: ""
    }

    /**
     * Saves the selected model ID securely.
     * @param modelId The model ID to be stored
     */
    fun saveSelectedModelId(modelId: String) {
        securePrefs.edit().putString(KEY_SELECTED_MODEL_ID, modelId).apply()
    }

    /**
     * Retrieves the securely stored selected model ID.
     * @return The stored model ID or empty string if not found
     */
    fun getSelectedModelId(): String {
        return securePrefs.getString(KEY_SELECTED_MODEL_ID, "") ?: ""
    }

    /**
     * Checks if keys have been migrated from the old storage to the secure storage.
     * @return true if migration has been completed, false otherwise
     */
    fun hasMigratedKeys(): Boolean {
        return securePrefs.getBoolean(KEY_HAS_MIGRATED, false)
    }

    /**
     * Marks keys as migrated to prevent multiple migration attempts.
     */
    fun markKeysMigrated() {
        securePrefs.edit().putBoolean(KEY_HAS_MIGRATED, true).apply()
    }

    /**
     * Clears all stored API keys and resets migration status.
     * Useful for logout or troubleshooting.
     */
    fun clearStoredKeys() {
        securePrefs.edit()
            .remove(KEY_OPENROUTER_API_KEY)
            .remove(KEY_YOUTUBE_API_KEY)
            .remove(KEY_SELECTED_MODEL_ID)
            .remove(KEY_HAS_MIGRATED)
            .apply()
    }
} 