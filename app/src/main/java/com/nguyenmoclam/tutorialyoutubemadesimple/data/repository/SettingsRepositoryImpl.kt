package com.nguyenmoclam.tutorialyoutubemadesimple.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Settings
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Define the DataStore at the file level
private val Context.dataStore by preferencesDataStore("settings")

/**
 * Implementation of SettingsRepository that uses DataStore for persistence.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    // Network utilities
    private val networkUtils = NetworkUtils(context)
    
    // Preference keys
    private object PreferenceKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val QUESTION_ORDER = stringPreferencesKey("question_order")
        val MAX_RETRY_COUNT = intPreferencesKey("max_retry_count")
        val SHOW_ANSWER_AFTER_WRONG = booleanPreferencesKey("show_answer_after_wrong")
        val AUTO_NEXT_QUESTION = booleanPreferencesKey("auto_next_question")
        val GOOGLE_SIGNED_IN = booleanPreferencesKey("google_signed_in")
        val TRANSCRIPT_MODE = stringPreferencesKey("transcript_mode")
        val DATA_SAVER_MODE = booleanPreferencesKey("data_saver_mode")
        val CONNECTION_TYPE = stringPreferencesKey("connection_type")
        val CONNECTION_TIMEOUT = intPreferencesKey("connection_timeout")
        val RETRY_POLICY = stringPreferencesKey("retry_policy")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
    }
    
    override fun getSettings(): Flow<Settings> {
        return context.dataStore.data.map { preferences ->
            Settings(
                themeMode = preferences[PreferenceKeys.THEME_MODE] ?: "system",
                questionOrder = preferences[PreferenceKeys.QUESTION_ORDER] ?: "sequential",
                maxRetryCount = preferences[PreferenceKeys.MAX_RETRY_COUNT] ?: 1,
                showAnswerAfterWrong = preferences[PreferenceKeys.SHOW_ANSWER_AFTER_WRONG] ?: false,
                autoNextQuestion = preferences[PreferenceKeys.AUTO_NEXT_QUESTION] ?: false,
                isGoogleSignedIn = preferences[PreferenceKeys.GOOGLE_SIGNED_IN] ?: false,
                transcriptMode = preferences[PreferenceKeys.TRANSCRIPT_MODE] ?: "anonymous",
                dataSaverMode = preferences[PreferenceKeys.DATA_SAVER_MODE] ?: false,
                connectionType = preferences[PreferenceKeys.CONNECTION_TYPE] ?: "any",
                connectionTimeout = preferences[PreferenceKeys.CONNECTION_TIMEOUT] ?: 30,
                retryPolicy = preferences[PreferenceKeys.RETRY_POLICY] ?: "exponential",
                appLanguage = preferences[PreferenceKeys.APP_LANGUAGE] ?: "system",
                isNetworkAvailable = networkUtils.isNetworkAvailable()
            )
        }
    }
    
    override suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.THEME_MODE] = mode
        }
    }
    
    override suspend fun setQuestionOrder(order: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.QUESTION_ORDER] = order
        }
    }
    
    override suspend fun setMaxRetryCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.MAX_RETRY_COUNT] = count
        }
    }
    
    override suspend fun setShowAnswerAfterWrong(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.SHOW_ANSWER_AFTER_WRONG] = show
        }
    }
    
    override suspend fun setAutoNextQuestion(auto: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.AUTO_NEXT_QUESTION] = auto
        }
    }
    
    override suspend fun setGoogleSignIn(signedIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.GOOGLE_SIGNED_IN] = signedIn
        }
    }
    
    override suspend fun setTranscriptMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.TRANSCRIPT_MODE] = mode
        }
    }
    
    override suspend fun setDataSaverMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.DATA_SAVER_MODE] = enabled
        }
    }
    
    override suspend fun setConnectionType(type: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.CONNECTION_TYPE] = type
        }
    }
    
    override suspend fun setConnectionTimeout(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.CONNECTION_TIMEOUT] = seconds
        }
    }
    
    override suspend fun setRetryPolicy(policy: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.RETRY_POLICY] = policy
        }
    }
    
    override suspend fun setAppLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.APP_LANGUAGE] = language
        }
    }
}