package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmoclam.tutorialyoutubemadesimple.data.manager.ModelDataManager
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.ApiKeyValidationState
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.ModelFilter
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.LLMConfig
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.ModelInfo
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.OpenRouterCreditsResponse
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.OpenRouterRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.LLMProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel that manages model selection and API key configuration.
 * Provides methods for fetching available models, validating API keys,
 * and monitoring credits usage.
 */
@HiltViewModel
class ModelSettingsViewModel @Inject constructor(
    private val openRouterRepository: OpenRouterRepository,
    private val modelDataManager: ModelDataManager,
    private val llmProcessor: LLMProcessor
) : ViewModel() {

    // Filtered and sorted models for display
    private val _displayModels = MutableStateFlow<List<ModelInfo>>(emptyList())
    val displayModels: StateFlow<List<ModelInfo>> = _displayModels.asStateFlow()
    
    // Pagination state
    private val _currentPage = MutableStateFlow(0)
    private val _hasMorePages = MutableStateFlow(true)
    val hasMorePages: StateFlow<Boolean> = _hasMorePages.asStateFlow()
    private val pageSize = 20 // Number of models per page
    
    // Current API key
    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()
    
    // Currently selected model
    private val _selectedModel = MutableStateFlow<ModelInfo?>(null)
    val selectedModel: StateFlow<ModelInfo?> = _selectedModel.asStateFlow()
    
    // API key validation state
    val apiKeyValidationState: StateFlow<ApiKeyValidationState> = openRouterRepository.apiKeyValidationState
        .stateIn(viewModelScope, SharingStarted.Eagerly, ApiKeyValidationState.NOT_VALIDATED)
    
    // Validation error message
    val validationErrorMessage: StateFlow<String?> = openRouterRepository.validationErrorMessage
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    
    // Credits information
    val credits: StateFlow<OpenRouterCreditsResponse?> = openRouterRepository.credits
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Model filters
    private val _currentFilters = MutableStateFlow<Map<ModelFilter.Category, Set<String>>>(emptyMap())
    val currentFilters: StateFlow<Map<ModelFilter.Category, Set<String>>> = _currentFilters.asStateFlow()
    
    // Sort options
    private val _currentSortOption = MutableStateFlow(ModelFilter.SortOption.TOP_WEEKLY)
    val currentSortOption: StateFlow<ModelFilter.SortOption> = _currentSortOption.asStateFlow()
    
    // Available filter options
    private val _availableProviders = MutableStateFlow<Set<String>>(emptySet())
    val availableProviders: StateFlow<Set<String>> = _availableProviders.asStateFlow()
    
    // Total filtered count
    private val _totalFilteredCount = MutableStateFlow(0)
    val totalFilteredCount: StateFlow<Int> = _totalFilteredCount.asStateFlow()
    
    init {
        loadStoredApiKey()
        loadSelectedModel()
        refreshModels()
        refreshCredits()
        
        // Listen for model data cache updates
        viewModelScope.launch {
            modelDataManager.cachedModels.collect {
                // Update available filters
                updateAvailableFilters()
                // Re-apply current filters
                applyFiltersAndSort()
            }
        }
    }
    
    /**
     * Loads the stored API key from secure storage.
     */
    private fun loadStoredApiKey() {
        val storedKey = openRouterRepository.getStoredApiKey()
        if (storedKey.isNotEmpty()) {
            _apiKey.value = storedKey
            validateApiKey(storedKey)
        }
    }
    
    /**
     * Loads the currently selected model from LLMProcessor config.
     */
    private fun loadSelectedModel() {
        val config = llmProcessor.getConfig()
        viewModelScope.launch {
            // Find the model by ID
            openRouterRepository.getModelById(config.modelId)?.let {
                _selectedModel.value = it
            }
        }
    }
    
    /**
     * Fetches available models from OpenRouter.
     * 
     * @param forceRefresh If true, forces a network request even if cached data is available.
     */
    fun refreshModels(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                openRouterRepository.getAvailableModels(forceRefresh)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load models: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Updates available filter options based on the current model data.
     */
    private fun updateAvailableFilters() {
        viewModelScope.launch {
            _availableProviders.value = modelDataManager.getAvailableProviders()
        }
    }
    
    /**
     * Loads the next page of models.
     * Useful for implementing infinite scrolling in the UI.
     */
    fun loadNextPage() {
        if (!_hasMorePages.value || _isLoading.value) return
        
        viewModelScope.launch {
            _isLoading.value = true
            
            val nextPage = _currentPage.value + 1
            val newModels = openRouterRepository.getModelsPage(
                nextPage,
                pageSize,
                _currentFilters.value,
                _currentSortOption.value
            )
            
            if (newModels.isNotEmpty()) {
                val currentModels = _displayModels.value.toMutableList()
                currentModels.addAll(newModels)
                _displayModels.value = currentModels
                _currentPage.value = nextPage
            } else {
                _hasMorePages.value = false
            }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Refreshes the user's credits information.
     * 
     * @param forceRefresh If true, forces a network request even if cached data is available.
     */
    fun refreshCredits(forceRefresh: Boolean = false) {
        if (_apiKey.value.isBlank()) return
        
        viewModelScope.launch {
            try {
                openRouterRepository.getCredits(forceRefresh)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load credits: ${e.message}"
            }
        }
    }
    
    /**
     * Validates an API key.
     * 
     * @param key The API key to validate.
     */
    fun validateApiKey(key: String) {
        if (key == _apiKey.value && 
            apiKeyValidationState.value == ApiKeyValidationState.VALID) {
            // Already validated this key
            return
        }
        
        _apiKey.value = key
        _errorMessage.value = null
        
        viewModelScope.launch {
            try {
                openRouterRepository.validateApiKey(key)
            } catch (e: Exception) {
                _errorMessage.value = "Error validating API key: ${e.message}"
            }
        }
    }
    
    /**
     * Saves an API key to secure storage.
     * 
     * @param key The API key to save.
     * @param force If true, saves the key regardless of validation state.
     */
    fun saveApiKey(key: String, force: Boolean = false) {
        if (key.isBlank()) return
        
        _apiKey.value = key
        openRouterRepository.saveApiKey(key, force)
        
        // Refresh models and credits with the new key
        refreshModels(true)
        refreshCredits(true)
    }
    
    /**
     * Selects a model and updates the LLMProcessor configuration.
     * 
     * @param model The model to select.
     */
    fun selectModel(model: ModelInfo) {
        _selectedModel.value = model
        
        // Update LLMProcessor configuration
        val config = LLMConfig(
            modelId = model.id,
            apiKey = _apiKey.value,
            maxTokens = (model.contextLength / 2).coerceAtMost(8192), // Default to half of context length, max 8192
            temperature = 0.7,
            topP = 0.95
        )
        
        llmProcessor.setConfig(config)
    }
    
    /**
     * Updates the filters for model listing.
     * 
     * @param category The filter category.
     * @param values The set of values to filter by.
     */
    fun updateFilter(category: ModelFilter.Category, values: Set<String>) {
        val updatedFilters = _currentFilters.value.toMutableMap()
        
        if (values.isEmpty()) {
            updatedFilters.remove(category)
        } else {
            updatedFilters[category] = values
        }
        
        _currentFilters.value = updatedFilters
        resetPagination()
        applyFiltersAndSort()
    }
    
    /**
     * Sets the sorting option for model listing.
     * 
     * @param sortOption The sort option to use.
     */
    fun setSortOption(sortOption: ModelFilter.SortOption) {
        _currentSortOption.value = sortOption
        resetPagination()
        applyFiltersAndSort()
    }
    
    /**
     * Resets pagination state for when filters or sort options change.
     */
    private fun resetPagination() {
        _currentPage.value = 0
        _hasMorePages.value = true
    }
    
    /**
     * Applies the current filters and sort option to the models list.
     */
    private fun applyFiltersAndSort() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Get first page with current filters
            val filteredModels = openRouterRepository.getModelsPage(
                0,
                pageSize,
                _currentFilters.value,
                _currentSortOption.value
            )
            
            // Update display models
            _displayModels.value = filteredModels
            
            // Update total count for pagination info
            _totalFilteredCount.value = openRouterRepository.getFilteredModelCount(_currentFilters.value)
            
            // Calculate if there are more pages
            val totalPages = modelDataManager.getTotalPages(pageSize, _currentFilters.value)
            _hasMorePages.value = totalPages > 1
            
            _isLoading.value = false
        }
    }
    
    /**
     * Gets models matching the search query.
     * 
     * @param query The search query string.
     * @return A list of models matching the query.
     */
    fun searchModels(query: String): List<ModelInfo> {
        if (query.isBlank()) {
            return emptyList()
        }
        
        val lowercaseQuery = query.lowercase()
        
        // Use the efficient data manager to get all models first
        val allModels = openRouterRepository.getFilteredModels(
            emptyMap(),
            ModelFilter.SortOption.TOP_WEEKLY
        )
        
        // Then filter by search query
        return allModels.filter { model ->
            model.name.lowercase().contains(lowercaseQuery) || 
            model.id.lowercase().contains(lowercaseQuery) ||
            model.providerName.lowercase().contains(lowercaseQuery)
        }.take(10) // Limit to 10 results for performance
    }
    
    /**
     * Clears the current API key and validation state.
     */
    fun clearApiKey() {
        _apiKey.value = ""
        openRouterRepository.saveApiKey("", true)
        viewModelScope.launch {
            openRouterRepository.clearCache()
        }
        openRouterRepository.clearValidationError()
        refreshModels(true)
    }
    
    /**
     * Clears the current error message.
     */
    fun clearError() {
        _errorMessage.value = null
        openRouterRepository.clearValidationError()
    }
} 