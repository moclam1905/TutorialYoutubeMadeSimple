package com.nguyenmoclam.tutorialyoutubemadesimple.data.repository

import com.nguyenmoclam.tutorialyoutubemadesimple.data.manager.ModelDataManager
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.ApiKeyValidationState
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.ModelFilter
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.Result
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.ModelInfo
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.OpenRouterCreditsResponse
import com.nguyenmoclam.tutorialyoutubemadesimple.data.service.OpenRouterService
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.ApiKeyValidator
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.SecurePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that manages interactions with the OpenRouter API.
 * Provides abstracted methods for accessing models, credits,
 * and handles caching and error handling.
 */
@Singleton
class OpenRouterRepository @Inject constructor(
    private val openRouterService: OpenRouterService,
    private val securePreferences: SecurePreferences,
    private val apiKeyValidator: ApiKeyValidator,
    private val modelDataManager: ModelDataManager
) {
    // Cached credits information
    private val _credits = MutableStateFlow<OpenRouterCreditsResponse?>(null)
    val credits: StateFlow<OpenRouterCreditsResponse?> = _credits.asStateFlow()
    
    // API key validation state
    private val _apiKeyValidationState = MutableStateFlow(ApiKeyValidationState.NOT_VALIDATED)
    val apiKeyValidationState: StateFlow<ApiKeyValidationState> = _apiKeyValidationState.asStateFlow()
    
    // Last refresh timestamp for credits
    private var lastCreditsRefreshTime: Long = 0
    
    // API key validation cache
    private data class ValidationCacheEntry(
        val apiKey: String,
        val state: ApiKeyValidationState,
        val timestamp: Long
    )
    private val validationCache = mutableMapOf<String, ValidationCacheEntry>()
    
    // Detailed validation error message
    private val _validationErrorMessage = MutableStateFlow<String?>(null)
    val validationErrorMessage: StateFlow<String?> = _validationErrorMessage.asStateFlow()
    
    /**
     * Gets the list of available models from OpenRouter.
     * Uses the ModelDataManager for efficient caching and data management.
     * 
     * @param forceRefresh If true, forces a network request even if cached data is available.
     * @return A Result containing the list of models or an error.
     */
    suspend fun getAvailableModels(forceRefresh: Boolean = false): Result<List<ModelInfo>> {
        val needsRefresh = forceRefresh || modelDataManager.needsRefresh()
        
        if (!needsRefresh) {
            // Use cached models from ModelDataManager using firstOrNull()
            val models = modelDataManager.cachedModels.firstOrNull() ?: emptyList()
            return Result.Success(models)
        }
        
        return try {
            val result = openRouterService.getAvailableModels()
            
            if (result is Result.Success) {
                // Update the ModelDataManager cache
                modelDataManager.updateCache(result.value)
            }
            
            result
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
    
    /**
     * Gets filtered and sorted models using efficient data structures.
     * 
     * @param filters Filter criteria to apply.
     * @param sortOption Sorting option to use.
     * @return A list of filtered and sorted models.
     */
    fun getFilteredModels(
        filters: Map<ModelFilter.Category, Set<String>>,
        sortOption: ModelFilter.SortOption
    ): List<ModelInfo> {
        return modelDataManager.getFilteredModels(filters, sortOption)
    }
    
    /**
     * Gets a paginated list of models with efficient filtering and sorting.
     * 
     * @param page The page number (0-based).
     * @param pageSize The number of models per page.
     * @param filters Filter criteria to apply.
     * @param sortOption Sorting option to use.
     * @return A page of filtered and sorted models.
     */
    fun getModelsPage(
        page: Int,
        pageSize: Int,
        filters: Map<ModelFilter.Category, Set<String>> = emptyMap(),
        sortOption: ModelFilter.SortOption = ModelFilter.SortOption.TOP_WEEKLY
    ): List<ModelInfo> {
        return modelDataManager.getModelsPage(page, pageSize, filters, sortOption)
    }
    
    /**
     * Gets a model by its ID from the cache.
     * 
     * @param modelId The ID of the model to get.
     * @return The model, or null if not found.
     */
    fun getModelById(modelId: String): ModelInfo? {
        return modelDataManager.getModelById(modelId)
    }
    
    /**
     * Gets the total number of models matching the filters.
     * Delegates to ModelDataManager.
     * 
     * @param filters The filter criteria to apply.
     * @return The number of matching models.
     */
    fun getFilteredModelCount(filters: Map<ModelFilter.Category, Set<String>> = emptyMap()): Int {
        return modelDataManager.getFilteredModelCount(filters)
    }
    
    /**
     * Gets the user's credits information from OpenRouter.
     * Uses cached data if available and not expired.
     * 
     * @param forceRefresh If true, forces a network request even if cached data is available.
     * @return A Result containing the credits information or an error.
     */
    suspend fun getCredits(forceRefresh: Boolean = false): Result<OpenRouterCreditsResponse> {
        // Check if we need to fetch new data
        val currentTime = System.currentTimeMillis()
        val cacheExpired = (currentTime - lastCreditsRefreshTime) > CREDITS_CACHE_EXPIRATION_TIME
        val needsRefresh = forceRefresh || _credits.value == null || cacheExpired
        
        if (!needsRefresh && _credits.value != null) {
            return Result.Success(_credits.value!!)
        }
        
        return try {
            val result = openRouterService.getCredits()
            
            if (result is Result.Success) {
                _credits.value = result.value
                lastCreditsRefreshTime = currentTime
            }
            
            result
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
    
    /**
     * Validates an OpenRouter API key.
     * Checks both format and network validation.
     * Updates the validation state and error message.
     * Uses cached validation results if available and not expired.
     * 
     * @param apiKey The API key to validate.
     * @param forceValidation If true, forces a validation even if cached result is available.
     * @return A Result containing the validation state or an error.
     */
    suspend fun validateApiKey(apiKey: String, forceValidation: Boolean = false): Result<ApiKeyValidationState> {
        if (apiKey.isBlank()) {
            _apiKeyValidationState.value = ApiKeyValidationState.INVALID_FORMAT
            _validationErrorMessage.value = "API key cannot be empty"
            return Result.Success(ApiKeyValidationState.INVALID_FORMAT)
        }
        
        // Check cache first if we're not forcing validation
        if (!forceValidation) {
            val currentTime = System.currentTimeMillis()
            val cachedEntry = validationCache[apiKey]
            
            if (cachedEntry != null && 
                (currentTime - cachedEntry.timestamp) < VALIDATION_CACHE_EXPIRATION_TIME &&
                cachedEntry.state != ApiKeyValidationState.ERROR && 
                cachedEntry.state != ApiKeyValidationState.NETWORK_ERROR) {
                
                // Use cached result
                _apiKeyValidationState.value = cachedEntry.state
                if (cachedEntry.state == ApiKeyValidationState.INVALID_FORMAT) {
                    _validationErrorMessage.value = "Invalid API key format. Keys should match the format: ${apiKeyValidator.getKeyFormatExample()}"
                } else if (cachedEntry.state == ApiKeyValidationState.INVALID_NETWORK) {
                    _validationErrorMessage.value = "API key rejected by OpenRouter. Please check that your key is correct."
                } else {
                    _validationErrorMessage.value = null
                }
                
                return Result.Success(cachedEntry.state)
            }
        }
        
        // If not cached or cache expired, validate
        _apiKeyValidationState.value = ApiKeyValidationState.VALIDATING
        _validationErrorMessage.value = null
        
        try {
            // Use the enhanced validator that returns both state and message
            val (state, errorMessage) = apiKeyValidator.validateOpenRouterKeyWithMessage(apiKey)
            
            // Update state
            _apiKeyValidationState.value = state
            _validationErrorMessage.value = errorMessage
            
            // Cache the validation result
            if (state != ApiKeyValidationState.VALIDATING) {
                validationCache[apiKey] = ValidationCacheEntry(
                    apiKey = apiKey,
                    state = state,
                    timestamp = System.currentTimeMillis()
                )
            }
            
            return Result.Success(state)
        } catch (e: Exception) {
            _apiKeyValidationState.value = ApiKeyValidationState.ERROR
            _validationErrorMessage.value = "Error validating API key: ${e.message}"
            return Result.Failure(e)
        }
    }
    
    /**
     * Saves an API key to secure storage.
     * Only saves valid API keys unless force is true.
     * 
     * @param apiKey The API key to save.
     * @param force If true, saves the key regardless of validation state.
     */
    fun saveApiKey(apiKey: String, force: Boolean = false) {
        if (force || _apiKeyValidationState.value == ApiKeyValidationState.VALID) {
            securePreferences.saveOpenRouterApiKey(apiKey)
            
            // Invalidate caches to force refresh with new key
            lastCreditsRefreshTime = 0
        }
    }
    
    /**
     * Gets the currently stored API key.
     * 
     * @return The stored API key or empty string if not found.
     */
    fun getStoredApiKey(): String {
        return securePreferences.getOpenRouterApiKey()
    }
    
    /**
     * Clears the current validation error message.
     */
    fun clearValidationError() {
        _validationErrorMessage.value = null
    }
    
    /**
     * Clears all caches including models, credits, and validation results.
     */
    suspend fun clearCache() {
        _credits.value = null
        validationCache.clear()
        lastCreditsRefreshTime = 0
        
        // Clear model data manager cache with empty list
        modelDataManager.updateCache(emptyList())
    }
    
    /**
     * Clears validation cache for a specific API key or all keys.
     * 
     * @param apiKey The API key to clear from cache, or null to clear all validation cache.
     */
    fun clearValidationCache(apiKey: String? = null) {
        if (apiKey != null) {
            validationCache.remove(apiKey)
        } else {
            validationCache.clear()
        }
    }
    
    companion object {
        // Cache expiration times
        private const val CREDITS_CACHE_EXPIRATION_TIME = 15 * 60 * 1000L // 15 minutes
        private const val VALIDATION_CACHE_EXPIRATION_TIME = 30 * 60 * 1000L // 30 minutes
    }
} 