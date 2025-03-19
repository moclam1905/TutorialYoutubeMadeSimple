package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.settings.*
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing app settings
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val setAppLanguageUseCase: SetAppLanguageUseCase,
    private val setQuestionOrderUseCase: SetQuestionOrderUseCase,
    private val setMaxRetryCountUseCase: SetMaxRetryCountUseCase,
    private val setShowAnswerAfterWrongUseCase: SetShowAnswerAfterWrongUseCase,
    private val setAutoNextQuestionUseCase: SetAutoNextQuestionUseCase,
    private val setGoogleSignInUseCase: SetGoogleSignInUseCase,
    private val setTranscriptModeUseCase: SetTranscriptModeUseCase,
    private val setDataSaverModeUseCase: SetDataSaverModeUseCase,
    private val setConnectionTypeUseCase: SetConnectionTypeUseCase,
    private val setConnectionTimeoutUseCase: SetConnectionTimeoutUseCase,
    private val setRetryPolicyUseCase: SetRetryPolicyUseCase
) : ViewModel() {

    // Network utilities
    private val networkUtils = NetworkUtils(context)
    
    // Settings state
    var settingsState by mutableStateOf(SettingsState())
        private set
    
    init {
        // Load settings using the use case
        getSettingsUseCase().onEach { settings ->
            settingsState = settingsState.copy(
                themeMode = settings.themeMode,
                questionOrder = settings.questionOrder,
                maxRetryCount = settings.maxRetryCount,
                showAnswerAfterWrong = settings.showAnswerAfterWrong,
                autoNextQuestion = settings.autoNextQuestion,
                isGoogleSignedIn = settings.isGoogleSignedIn,
                transcriptMode = settings.transcriptMode,
                dataSaverMode = settings.dataSaverMode,
                connectionType = settings.connectionType,
                connectionTimeout = settings.connectionTimeout,
                retryPolicy = settings.retryPolicy,
                appLanguage = settings.appLanguage,
                isNetworkAvailable = settings.isNetworkAvailable
            )
        }.launchIn(viewModelScope)
        
        // Observe network connectivity
        viewModelScope.launch {
            networkUtils.observeNetworkConnectivity().collect { isAvailable ->
                settingsState = settingsState.copy(isNetworkAvailable = isAvailable)
            }
        }
    }
    
    // Theme settings
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            setThemeModeUseCase(mode)
            settingsState = settingsState.copy(themeMode = mode)
        }
    }
    
    // Quiz configuration settings
    fun setQuestionOrder(order: String) {
        viewModelScope.launch {
            setQuestionOrderUseCase(order)
            settingsState = settingsState.copy(questionOrder = order)
        }
    }
    
    fun setMaxRetryCount(count: Int) {
        viewModelScope.launch {
            setMaxRetryCountUseCase(count)
            settingsState = settingsState.copy(maxRetryCount = count)
        }
    }
    
    fun setShowAnswerAfterWrong(show: Boolean) {
        viewModelScope.launch {
            setShowAnswerAfterWrongUseCase(show)
            settingsState = settingsState.copy(showAnswerAfterWrong = show)
        }
    }
    
    fun setAutoNextQuestion(auto: Boolean) {
        viewModelScope.launch {
            setAutoNextQuestionUseCase(auto)
            settingsState = settingsState.copy(autoNextQuestion = auto)
        }
    }
    
    // Google account settings
    fun setGoogleSignIn(signedIn: Boolean) {
        viewModelScope.launch {
            setGoogleSignInUseCase(signedIn)
            settingsState = settingsState.copy(isGoogleSignedIn = signedIn)
        }
    }
    
    fun setTranscriptMode(mode: String) {
        viewModelScope.launch {
            setTranscriptModeUseCase(mode)
            settingsState = settingsState.copy(transcriptMode = mode)
        }
    }
    
    fun clearAccountData() {
        viewModelScope.launch {
            setGoogleSignInUseCase(false)
            setTranscriptModeUseCase("anonymous")
            settingsState = settingsState.copy(
                isGoogleSignedIn = false,
                transcriptMode = "anonymous"
            )
        }
    }
    
    // Data management
    fun clearQuizHistory() {
        viewModelScope.launch {
            // Here you would call repository methods to clear quiz history from Room database
            // For now, we'll just update the UI state
            settingsState = settingsState.copy(quizCount = 0)
        }
    }
    
    fun resetLearningProgress() {
        viewModelScope.launch {
            // Here you would call repository methods to reset learning progress in Room database
        }
    }
    
    fun clearCache() {
        viewModelScope.launch {
            // Here you would implement cache clearing logic
            // For now, we'll just update the UI state
            settingsState = settingsState.copy(usedStorageBytes = 0)
        }
    }
    
    // Network settings
    fun setDataSaverMode(enabled: Boolean) {
        viewModelScope.launch {
            setDataSaverModeUseCase(enabled)
            settingsState = settingsState.copy(dataSaverMode = enabled)
        }
    }
    
    fun setConnectionType(type: String) {
        viewModelScope.launch {
            setConnectionTypeUseCase(type)
            settingsState = settingsState.copy(connectionType = type)
        }
    }
    
    fun setConnectionTimeout(seconds: Int) {
        viewModelScope.launch {
            setConnectionTimeoutUseCase(seconds)
            settingsState = settingsState.copy(connectionTimeout = seconds)
        }
    }
    
    fun setRetryPolicy(policy: String) {
        viewModelScope.launch {
            setRetryPolicyUseCase(policy)
            settingsState = settingsState.copy(retryPolicy = policy)
        }
    }
    
    // Language settings
    fun setAppLanguage(language: String) {
        viewModelScope.launch {
            setAppLanguageUseCase(language)
            settingsState = settingsState.copy(appLanguage = language)
        }
    }
    
    // App information
    fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            "Unknown"
        }.toString()
    }
}

/**
 * Data class representing the state of all settings
 */
data class SettingsState(
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
    val connectionTimeout: Int = 30, // in seconds
    val retryPolicy: String = "exponential", // "none", "linear", or "exponential"
    
    // Language
    val appLanguage: String = "system" // "en", "vi", or "system"
)