package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.nguyenmoclam.tutorialyoutubemadesimple.auth.AuthManager
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.settings.GetSettingsUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.settings.SetAppLanguageUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.settings.SetAutoNextQuestionUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.settings.SetConnectionTimeoutUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.settings.SetConnectionTypeUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.settings.SetDataSaverModeUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.settings.SetGoogleSignInUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.settings.SetMaxRetryCountUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.settings.SetQuestionOrderUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.settings.SetRetryPolicyUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.settings.SetShowAnswerAfterWrongUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.settings.SetThemeModeUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.settings.SetTranscriptModeUseCase
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
    private val setRetryPolicyUseCase: SetRetryPolicyUseCase,
    private val authManager: AuthManager
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
            if (signedIn) {
                // We don't actually sign in here - this will be triggered by the activity result
                // The UI will show the Google Sign-In button which will launch the sign-in intent
            } else {
                // Sign out
                authManager.signOut()
                setGoogleSignInUseCase(false)
                settingsState = settingsState.copy(isGoogleSignedIn = false)
            }
        }
    }
    
    /**
     * Process the Google Sign-In result
     */
    fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        val account = authManager.handleSignInResult(task)
        viewModelScope.launch {
            if (account != null) {
                // Sign-in successful
                setGoogleSignInUseCase(true)
                settingsState = settingsState.copy(isGoogleSignedIn = true)
                
                // If transcript mode is anonymous, switch to google
                if (settingsState.transcriptMode == "anonymous") {
                    setTranscriptMode("google")
                }
            }
        }
    }
    
    /**
     * Get sign-in intent for launching Google Sign-In
     */
    fun getSignInIntent(): Intent {
        return authManager.getSignInIntent()
    }
    
    fun setTranscriptMode(mode: String) {
        viewModelScope.launch {
            setTranscriptModeUseCase(mode)
            settingsState = settingsState.copy(transcriptMode = mode)
        }
    }
    
    fun clearAccountData() {
        viewModelScope.launch {
            // Sign out from Google
            authManager.signOut()
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