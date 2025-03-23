package com.nguyenmoclam.tutorialyoutubemadesimple.domain.repository

import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Settings
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing application settings.
 * This follows the Clean Architecture principle by defining a contract
 * that the data layer must implement.
 */
interface SettingsRepository {
    /**
     * Get the current settings as a Flow.
     * 
     * @return Flow of Settings domain model
     */
    fun getSettings(): Flow<Settings>
    
    /**
     * Update the theme mode setting.
     * 
     * @param mode The theme mode ("light", "dark", or "system")
     */
    suspend fun setThemeMode(mode: String)
    
    /**
     * Update the question order setting.
     * 
     * @param order The question order ("sequential" or "shuffle")
     */
    suspend fun setQuestionOrder(order: String)
    
    /**
     * Update the maximum retry count setting.
     * 
     * @param count The maximum number of retries allowed
     */
    suspend fun setMaxRetryCount(count: Int)
    
    /**
     * Update the show answer after wrong setting.
     * 
     * @param show Whether to show the answer after a wrong attempt
     */
    suspend fun setShowAnswerAfterWrong(show: Boolean)
    
    /**
     * Update the auto next question setting.
     * 
     * @param auto Whether to automatically proceed to the next question
     */
    suspend fun setAutoNextQuestion(auto: Boolean)
    
    /**
     * Update the Google sign-in status.
     * 
     * @param signedIn Whether the user is signed in with Google
     */
    suspend fun setGoogleSignIn(signedIn: Boolean)
    
    /**
     * Update the transcript mode setting.
     * 
     * @param mode The transcript mode ("google" or "anonymous")
     */
    suspend fun setTranscriptMode(mode: String)
    
    /**
     * Update the data saver mode setting.
     * 
     * @param enabled Whether data saver mode is enabled
     */
    suspend fun setDataSaverMode(enabled: Boolean)
    
    /**
     * Update the connection type setting.
     * 
     * @param type The connection type ("wifi_only" or "any")
     */
    suspend fun setConnectionType(type: String)
    
    /**
     * Update the connection timeout setting.
     * 
     * @param seconds The timeout in seconds
     */
    suspend fun setConnectionTimeout(seconds: Int)
    
    /**
     * Update the retry policy setting.
     * 
     * @param policy The retry policy ("none", "linear", or "exponential")
     */
    suspend fun setRetryPolicy(policy: String)
    
    /**
     * Update the app language setting.
     * 
     * @param language The app language ("en", "vi", or "system")
     */
    suspend fun setAppLanguage(language: String)
}