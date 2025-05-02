package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.nguyenmoclam.tutorialyoutubemadesimple.MainActivity
import com.nguyenmoclam.tutorialyoutubemadesimple.auth.AuthManager
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.ApiKeyValidationState
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.ModelFilter
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.ModelInfo
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.OpenRouterRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.UserDataRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.repository.GetQuizRepositoryUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.settings.GetSettingsUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.settings.SetAllowContentOnMeteredUseCase
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
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.SecurePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
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
    private val setAllowContentOnMeteredUseCase: SetAllowContentOnMeteredUseCase,
    private val getQuizRepositoryUseCase: GetQuizRepositoryUseCase,
    private val authManager: AuthManager,
    private var networkUtils: NetworkUtils,
    private val openRouterRepository: OpenRouterRepository,
    private val securePreferences: SecurePreferences,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    // Settings state
    var settingsState by mutableStateOf(SettingsState())
        private set

    // Current Firebase user from AuthManager
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    // Add this state variable
    private val _isInitialSettingsLoaded = MutableStateFlow(false)
    val isInitialSettingsLoaded: StateFlow<Boolean> =
        _isInitialSettingsLoaded.asStateFlow() // Expose as StateFlow

    // Expose free calls state from UserDataRepository
    val freeCallsState: StateFlow<Int?> = userDataRepository.freeCallsStateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null // Or fetch initial value if needed
        )

    // --- Model List Management ---

    // Internal state for ALL fetched models
    private val _allModels = MutableStateFlow<List<ModelInfo>>(emptyList())

    // Loading state specifically for fetching/refreshing models
    private val _isLoadingModels = mutableStateOf(false)
    val isLoadingModels: Boolean get() = _isLoadingModels.value

    // **Public StateFlow for the DISPLAYED (filtered and sorted) models**
    private val _displayedModels = MutableStateFlow<List<ModelInfo>>(emptyList())
    val displayedModels: StateFlow<List<ModelInfo>> = _displayedModels.asStateFlow()

    init {
        // Combine flows to update displayed models
        combine(
            _allModels,
            snapshotFlow { settingsState } // Observe changes in filters and sort
        ) { allModels, currentState ->
            // Apply filtering
            val filteredModels = applyAllFilters(allModels, currentState.modelFilters)
            // Apply sorting
            sortModels(filteredModels, currentState.modelSortOption)
        }.onEach { updatedList ->
            _displayedModels.value = updatedList
        }.launchIn(viewModelScope)

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
                isNetworkAvailable = settings.isNetworkAvailable,
                allowMeteredNetworks = settings.allowMeteredNetworks
            )

            // Update network settings in NetworkUtils
            networkUtils.setDataSaverEnabled(settings.dataSaverMode)
            networkUtils.setConnectionTypeRestriction(settings.connectionType)
            networkUtils.setAllowContentOnMetered(settings.allowMeteredNetworks)
        }.launchIn(viewModelScope)

        // Observe network connectivity
        viewModelScope.launch {
            networkUtils.observeNetworkConnectivity().collect { isAvailable ->
                settingsState = settingsState.copy(isNetworkAvailable = isAvailable)
            }
        }

        // Observe Firebase user from UserDataRepository
        viewModelScope.launch {
            userDataRepository.userStateFlow.collect { user ->
                _currentUser.value = user
                // Update Google sign-in status based on Firebase auth
                settingsState = settingsState.copy(isGoogleSignedIn = user != null)
            }
        }

        updateStorageInfo()

        // Load stored API key and validate if exists
        viewModelScope.launch {
            try { // Add try-finally block
                val storedApiKey = openRouterRepository.getStoredApiKey()
                val storedModelId = securePreferences.getSelectedModelId()

                var updatedState = settingsState // Create a temporary copy to batch updates

                // Set selected model if stored
                if (storedModelId.isNotEmpty()) {
                    updatedState = updatedState.copy(selectedModel = storedModelId)
                }

                if (storedApiKey.isNotEmpty()) {
                    updatedState = updatedState.copy(openRouterApiKey = storedApiKey)

                    // Validate format first
                    val formatValidation = validateApiKeyFormat(storedApiKey)
                    if (formatValidation == ApiKeyValidationState.VALIDATING) { // Assuming VALIDATING means format is ok for now
                        updatedState =
                            updatedState.copy(apiKeyValidationState = ApiKeyValidationState.VALIDATING)
                        // Update state *before* potential suspension point (network call)
                        settingsState = updatedState

                        // Now perform the actual validation network call
                        val validationState =
                            openRouterRepository.validateApiKey(storedApiKey, false)
                        // Read the latest state again before copying, in case other flows updated it
                        updatedState = settingsState.copy(apiKeyValidationState = validationState)

                        if (validationState == ApiKeyValidationState.VALID) {
                            // Fetch models and credits *after* API key is confirmed valid
                            fetchModels() // Consider error handling/return values
                            fetchCredits() // Consider error handling/return values
                        }
                    } else {
                        // Handle case where format is invalid immediately
                        updatedState =
                            updatedState.copy(apiKeyValidationState = formatValidation) // Use the actual invalid state
                    }
                } else {
                    // No API key stored
                    updatedState =
                        updatedState.copy(apiKeyValidationState = ApiKeyValidationState.NOT_VALIDATED)
                }

                // Update the final state once after all checks
                settingsState = updatedState

            } catch (e: Exception) {
                // Handle potential errors during loading/validation
                settingsState =
                    settingsState.copy(apiKeyValidationState = ApiKeyValidationState.ERROR) // Example error handling
                // Log the exception, e.g., Log.e("SettingsViewModel", "Error loading initial settings", e)
            } finally {
                // Mark loading as complete regardless of success or failure
                _isInitialSettingsLoaded.value = true
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

    // Data management
    fun clearQuizHistory() {
        viewModelScope.launch {
            try {
                val quizRepository = getQuizRepositoryUseCase()
                val quizzes = quizRepository.getAllQuizzes().first()

                // Delete all quizzes
                quizzes.forEach { quiz ->
                    quizRepository.deleteQuiz(quiz.id)
                }

                // Update UI state
                settingsState = settingsState.copy(quizCount = 0, usedStorageBytes = 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetLearningProgress() {
        viewModelScope.launch {
            try {
                val quizRepository = getQuizRepositoryUseCase()
                val quizzes = quizRepository.getAllQuizzes().first()

                // Delete progress for all quizzes
                quizzes.forEach { quiz ->
                    quizRepository.deleteProgressForQuiz(quiz.id)
                }

                // Update UI state
                updateStorageInfo()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            try {
                val quizRepository = getQuizRepositoryUseCase()
                val quizzes = quizRepository.getAllQuizzes().first()

                // Delete transcripts, topics, and key points for all quizzes
                quizzes.forEach { quiz ->
                    quizRepository.deleteTranscriptForQuiz(quiz.id)
                    quizRepository.deleteTopicsForQuiz(quiz.id)
                    quizRepository.deleteKeyPointsForQuiz(quiz.id)
                }

                // Update UI state
                updateStorageInfo()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateStorageInfo() {
        viewModelScope.launch {
            try {
                val quizRepository = getQuizRepositoryUseCase()
                val quizCount = quizRepository.getQuizCount()
                val usedStorageBytes = quizRepository.getUsedStorageBytes()

                settingsState = settingsState.copy(
                    quizCount = quizCount,
                    usedStorageBytes = usedStorageBytes
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Network settings
    fun setDataSaverMode(enabled: Boolean) {
        viewModelScope.launch {
            setDataSaverModeUseCase(enabled)
            networkUtils.setDataSaverEnabled(enabled)
            settingsState = settingsState.copy(dataSaverMode = enabled)
        }
    }

    fun setConnectionType(type: String) {
        viewModelScope.launch {
            setConnectionTypeUseCase(type)
            networkUtils.setConnectionTypeRestriction(type)
            settingsState = settingsState.copy(connectionType = type)
        }
    }

    fun setConnectionTimeout(seconds: Int) {
        viewModelScope.launch {
            setConnectionTimeoutUseCase(seconds)
            networkUtils.setConnectionTimeout(seconds)
            settingsState = settingsState.copy(connectionTimeout = seconds)
        }
    }

    fun setRetryPolicy(policy: String) {
        viewModelScope.launch {
            setRetryPolicyUseCase(policy)
            networkUtils.setRetryPolicy(policy)
            settingsState = settingsState.copy(retryPolicy = policy)
        }
    }

    fun setAllowMeteredNetworks(allowed: Boolean) {
        viewModelScope.launch {
            setAllowContentOnMeteredUseCase(allowed)
            networkUtils.setAllowContentOnMetered(allowed)
            settingsState = settingsState.copy(allowMeteredNetworks = allowed)
        }
    }

    // Language settings
    fun setAppLanguage(language: String) {
        viewModelScope.launch {
            setAppLanguageUseCase(language)
            settingsState = settingsState.copy(appLanguage = language)

            // Apply language change immediately
            val localeList = when (language) {
                "en" -> LocaleListCompat.create(Locale("en"))
                "vi" -> LocaleListCompat.create(Locale("vi"))
                else -> LocaleListCompat.getEmptyLocaleList() // System default
            }
            AppCompatDelegate.setApplicationLocales(localeList)
            // Proper activity restart with application context
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
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

    // AI Model Settings Functions
    fun setOpenRouterApiKey(key: String) {
        viewModelScope.launch {
            // Store the API key
            settingsState = settingsState.copy(
                openRouterApiKey = key,
                apiKeyValidationState = validateApiKeyFormat(key)
            )

            // If format is valid, validate with server
            if (settingsState.apiKeyValidationState == ApiKeyValidationState.VALIDATING) {
                validateApiKeyWithServer(key)
            }
        }
    }

    /**
     * Public method to validate API key - can be called from UI components
     */
    fun validateApiKey(key: String) {
        viewModelScope.launch {
            val formatState = validateApiKeyFormat(key)
            settingsState = settingsState.copy(
                apiKeyValidationState = formatState
            )

            if (formatState == ApiKeyValidationState.VALIDATING) {
                validateApiKeyWithServer(key)
            }
        }
    }

    /**
     * Validates the API key format
     * A valid OpenRouter API key should start with "sk-or-v1-"
     */
    private fun validateApiKeyFormat(key: String): ApiKeyValidationState {
        return when {
            key.isEmpty() -> ApiKeyValidationState.NOT_VALIDATED
            !key.startsWith("sk-or-v1-") -> ApiKeyValidationState.INVALID_FORMAT
            else -> ApiKeyValidationState.VALIDATING
        }
    }

    /**
     * Validates the API key with the server and fetches models if valid
     */
    private fun validateApiKeyWithServer(key: String) {
        viewModelScope.launch {
            try {
                // Set state to validating
                settingsState =
                    settingsState.copy(apiKeyValidationState = ApiKeyValidationState.VALIDATING)

                // Validate the API key with the repository
                val validationState = openRouterRepository.validateApiKey(key)

                // Update validation state
                settingsState = settingsState.copy(apiKeyValidationState = validationState)

                // If the key is valid, fetch models and credits
                if (validationState == ApiKeyValidationState.VALID) {
                    // Save the API key securely
                    openRouterRepository.saveApiKey(key, force = true)

                    // Fetch models and credits
                    fetchModels()
                    fetchCredits()
                }
            } catch (e: Exception) {
                // Handle error (timeout, network error, etc.)
                settingsState =
                    settingsState.copy(apiKeyValidationState = ApiKeyValidationState.INVALID)
            }
        }
    }

    /**
     * Fetches models from the OpenRouter API and populates the internal `_allModels` state.
     * Resets pagination state if implemented.
     */
    fun fetchModels() {
        // Reset filters and sort? Optional, depends on desired UX. Let's keep them for now.
        viewModelScope.launch {
            _isLoadingModels.value = true
            // _isLoadingMoreModels.value = false // Reset pagination loading state
            // _hasMoreModels.value = true // Assume there are more initially
            // currentPage = 0 // Reset page count
            try {
                // Fetch ALL models for now (assuming no API pagination support yet)
                val modelsList = openRouterRepository.getAvailableModels(forceRefresh = true)
                _allModels.value = modelsList

                // Update the default sort option to MODERATED_FIRST if not already set
                if (settingsState.modelSortOption == ModelFilter.SortOption.TOP_WEEKLY) {
                    settingsState =
                        settingsState.copy(modelSortOption = ModelFilter.SortOption.MODERATED_FIRST)
                }

                // _hasMoreModels.value = modelsList.size >= PAGE_SIZE // Update based on first page size
            } catch (e: Exception) {
                _allModels.value = emptyList() // Clear on error
                // Handle error appropriately (e.g., show a message)
            } finally {
                _isLoadingModels.value = false
            }
        }
    }

    /**
     * Fetches credits information from OpenRouter.
     */
    private fun fetchCredits() {
        viewModelScope.launch {
            try {
                val creditsResponse = openRouterRepository.getCredits(true)
                settingsState =
                    settingsState.copy(apiKeyCredits = creditsResponse.data.total_credits ?: 0.0)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun setSelectedModel(modelId: String) { // Renamed parameter for clarity
        viewModelScope.launch {
            settingsState = settingsState.copy(selectedModel = modelId)
            try {
                securePreferences.saveSelectedModelId(modelId)
            } catch (e: Exception) {
                // Handle error saving preference
            }
        }
    }

    // --- Filtering and Sorting Logic ---

    /**
     * Applies a filter for a specific category.
     * If the option is already selected for the category, it effectively clears the filter for that category.
     */
    fun applyModelFilter(category: ModelFilter.Category, option: String) {
        val currentFilters = settingsState.modelFilters.toMutableMap()
        // For single-select filters, just set the new option
        currentFilters[category] = setOf(option)
        settingsState = settingsState.copy(modelFilters = currentFilters)
    }

    /**
     * Clears the filter for a specific category.
     */
    fun clearModelFilter(category: ModelFilter.Category) {
        val currentFilters = settingsState.modelFilters.toMutableMap()
        currentFilters.remove(category)
        settingsState = settingsState.copy(modelFilters = currentFilters)
    }

    /**
     * Sets the sorting option for the model list.
     */
    fun setModelSortOption(sortOption: ModelFilter.SortOption) {
        settingsState = settingsState.copy(modelSortOption = sortOption)
    }

    /**
     * Applies all active filters to the list of models.
     */
    private fun applyAllFilters(
        models: List<ModelInfo>,
        filters: Map<ModelFilter.Category, Set<String>>
    ): List<ModelInfo> {
        if (filters.isEmpty()) return models

        return models.filter { model ->
            filters.all { (category, selectedOptions) ->
                if (selectedOptions.isEmpty()) return@all true // No filter applied for this category

                val option = selectedOptions.first() // Assuming single select for now

                when (category) {
                    ModelFilter.Category.INPUT_MODALITY -> model.inputModalities.contains(option.lowercase())
                    ModelFilter.Category.CONTEXT_LENGTH -> checkContextLength(
                        model.contextLength,
                        option
                    )

                    ModelFilter.Category.PRICING -> checkPricingTier(model.promptPrice, option)
                    // Add else branch to make when exhaustive
                    else -> true // Don't filter for unhandled categories (PROVIDER, OUTPUT_MODALITY)
                }
            }
        }
    }

    /** Helper to check if a model fits a context length filter option */
    private fun checkContextLength(contextLength: Int, filterOption: String): Boolean {
        // Use constants directly from ModelFilter for reliable comparison
        return when (filterOption) {
            ModelFilter.ContextLengths.RANGE_4K -> contextLength <= 4000
            ModelFilter.ContextLengths.RANGE_8K -> contextLength in 4001..8000
            ModelFilter.ContextLengths.RANGE_16K -> contextLength in 8001..16000
            ModelFilter.ContextLengths.RANGE_32K -> contextLength in 16001..32000
            ModelFilter.ContextLengths.RANGE_64K -> contextLength in 32001..64000
            ModelFilter.ContextLengths.RANGE_128K_PLUS -> contextLength > 64000
            else -> true // Default case if filter option is unexpected
        }
    }

    /**
     * Helper to check if a model fits a pricing tier filter option.
     * Assumes promptPrice is the price per **million** tokens.
     * NOTE: The thresholds for BUDGET, STANDARD, PREMIUM might need adjustment.
     */
    private fun checkPricingTier(promptPrice: Double, filterOption: String): Boolean {
        // Use constants directly from ModelFilter for reliable comparison
        return when (filterOption) {
            ModelFilter.PricingTiers.FREE -> promptPrice == 0.0
            // Adjusted budget to include very small prices > 0
            ModelFilter.PricingTiers.BUDGET -> promptPrice > 0.0 && promptPrice <= 0.5 // Example threshold
            ModelFilter.PricingTiers.STANDARD -> promptPrice > 0.5 && promptPrice <= 1.5 // Example threshold
            ModelFilter.PricingTiers.PREMIUM -> promptPrice > 1.5 // Example threshold
            else -> true // Default case if filter option is unexpected
        }
    }

    /**
     * Sorts the list of models based on the selected sort option.
     */
    private fun sortModels(
        models: List<ModelInfo>,
        sortOption: ModelFilter.SortOption
    ): List<ModelInfo> {
        return when (sortOption) {
            ModelFilter.SortOption.TOP_WEEKLY -> models // Assuming API returns this by default, or implement ranking if available
            ModelFilter.SortOption.NEWEST -> models.sortedByDescending { it.id } // Assuming ID correlates with creation time - might need a real date field
            ModelFilter.SortOption.PRICE_LOW_TO_HIGH -> models.sortedBy { it.promptPrice }
            ModelFilter.SortOption.PRICE_HIGH_TO_LOW -> models.sortedByDescending { it.promptPrice }
            ModelFilter.SortOption.CONTEXT_HIGH_TO_LOW -> models.sortedByDescending { it.contextLength }
            ModelFilter.SortOption.MODERATED_FIRST -> models.sortedWith(
                compareByDescending<ModelInfo> { it.isModerated }
                    .thenBy { it.promptPrice }
            )
        }
    }

    // --- Pagination Logic (Placeholder - Implement later if needed) ---
    // private var currentPage = 0
    // private val PAGE_SIZE = 20 // Example page size

    // fun loadMoreModels() {
    //     if (isLoadingModels || isLoadingMoreModels || !hasMoreModels) return
    //
    //     viewModelScope.launch {
    //         _isLoadingMoreModels.value = true
    //         currentPage++
    //         try {
    //             // Fetch next page - Requires repository changes
    //             // val nextPageModels = openRouterRepository.getAvailableModels(
    //             //     offset = currentPage * PAGE_SIZE,
    //             //     limit = PAGE_SIZE
    //             // )
    //             // Simulating loading more from the existing list for now
    //             delay(1000) // Simulate network delay
    //             val currentAll = _allModels.value
    //             val start = currentPage * PAGE_SIZE
    //             val end = minOf(start + PAGE_SIZE, currentAll.size)
    //             val nextPageModels = if (start < currentAll.size) currentAll.subList(start, end) else emptyList()
    //
    //             if (nextPageModels.isNotEmpty()) {
    //                 // Append to _allModels if repository fetched new data
    //                 // _allModels.value = _allModels.value + nextPageModels
    //             }
    //             // Update hasMoreModels based on the actual result size from repo
    //             // _hasMoreModels.value = nextPageModels.size >= PAGE_SIZE
    //              _hasMoreModels.value = false // TEMPORARY: Disable load more after one attempt
    //
    //         } catch (e: Exception) {
    //             // Handle error
    //             currentPage-- // Revert page count on error
    //             _hasMoreModels.value = false // Stop trying on error
    //         } finally {
    //             _isLoadingMoreModels.value = false
    //         }
    //     }
    // }

    fun resetAllSettings() {
        setThemeMode("system")
        setQuestionOrder("sequential")
        setMaxRetryCount(1)
        setShowAnswerAfterWrong(false)
        setAutoNextQuestion(false)
        setDataSaverMode(false)
        setConnectionType("any")
        setConnectionTimeout(120)
        setRetryPolicy("exponential")
        setAppLanguage("system")
        setAllowMeteredNetworks(false)
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
    val connectionTimeout: Int = 120, // in seconds
    val retryPolicy: String = "exponential", // "none", "linear", or "exponential"

    // Language
    val appLanguage: String = "system", // "en", "vi", or "system"

    // AI Model Settings
    val openRouterApiKey: String = "",
    val selectedModel: String = "",
    val apiKeyValidationState: ApiKeyValidationState = ApiKeyValidationState.NOT_VALIDATED,
    val apiKeyCredits: Double = 0.0,

    // New settings
    val allowMeteredNetworks: Boolean = false,

    // Model List Filters and Sort
    val modelFilters: Map<ModelFilter.Category, Set<String>> = emptyMap(),
    val modelSortOption: ModelFilter.SortOption = ModelFilter.SortOption.TOP_WEEKLY,
    // Add pagination state later if needed
    // val isLoadingMoreModels: Boolean = false,
    // val hasMoreModels: Boolean = true 
)