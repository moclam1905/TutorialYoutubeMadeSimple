package com.nguyenmoclam.tutorialyoutubemadesimple.data.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.ModelFilter
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.ModelInfo
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages model data with efficient caching, indexing, and filtering capabilities.
 * Provides persistent storage for model data using DataStore and in-memory indexing
 * for fast filtering and sorting operations.
 */
@Singleton
class ModelDataManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkUtils: NetworkUtils
) {
    // DataStore for persistent caching
    private val Context.modelDataStore: DataStore<Preferences> by preferencesDataStore(name = "model_data")
    private val modelDataStore = context.modelDataStore
    
    // Keys for DataStore
    private val MODELS_CACHE_KEY = stringPreferencesKey("models_cache")
    private val MODELS_LAST_REFRESH_KEY = longPreferencesKey("models_last_refresh")
    
    // In-memory cache for fast access
    private val modelsCache = ConcurrentHashMap<String, ModelInfo>()
    
    // Indexed data for quick filtering
    private val providerIndex = ConcurrentHashMap<String, MutableSet<String>>()
    private val contextLengthIndex = ConcurrentHashMap<String, MutableSet<String>>()
    private val modalityIndex = ConcurrentHashMap<String, MutableSet<String>>()
    private val pricingIndex = ConcurrentHashMap<String, MutableSet<String>>()
    
    // Gson for serialization/deserialization
    private val gson = Gson()
    
    /**
     * Gets a Flow of the cached model list.
     * This will emit updates when the cache is refreshed.
     */
    val cachedModels: Flow<List<ModelInfo>> = modelDataStore.data.map { preferences ->
        val modelsJson = preferences[MODELS_CACHE_KEY] ?: "[]"
        try {
            val type = object : TypeToken<List<ModelInfo>>() {}.type
            gson.fromJson<List<ModelInfo>>(modelsJson, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Gets the last refresh timestamp.
     */
    val lastRefreshTime: Flow<Long> = modelDataStore.data.map { preferences ->
        preferences[MODELS_LAST_REFRESH_KEY] ?: 0L
    }
    
    /**
     * Checks if the cache needs refreshing based on cache duration.
     * 
     * @param cacheDuration The maximum age of the cache in milliseconds.
     * @return True if the cache needs refreshing, false otherwise.
     */
    suspend fun needsRefresh(cacheDuration: Long = DEFAULT_CACHE_DURATION): Boolean {
        val lastRefresh = modelDataStore.data.map { it[MODELS_LAST_REFRESH_KEY] ?: 0L }.firstOrNull() ?: 0L
        val currentTime = System.currentTimeMillis()
        return currentTime - lastRefresh > cacheDuration
    }
    
    /**
     * Updates the model cache with a new list of models.
     * Also rebuilds all indexes for efficient querying.
     * 
     * @param models The new list of models.
     */
    suspend fun updateCache(models: List<ModelInfo>) {
        // Update persistent cache
        modelDataStore.edit { preferences ->
            preferences[MODELS_CACHE_KEY] = gson.toJson(models)
            preferences[MODELS_LAST_REFRESH_KEY] = System.currentTimeMillis()
        }
        
        // Update in-memory cache and indexes
        modelsCache.clear()
        providerIndex.clear()
        contextLengthIndex.clear()
        modalityIndex.clear()
        pricingIndex.clear()
        
        // Populate caches and indexes
        models.forEach { model ->
            // Add to main cache
            modelsCache[model.id] = model
            
            // Index by provider
            val provider = model.providerName
            if (!providerIndex.containsKey(provider)) {
                providerIndex[provider] = mutableSetOf()
            }
            providerIndex[provider]?.add(model.id)
            
            // Index by context length
            val contextCategory = getContextLengthCategory(model.contextLength)
            if (!contextLengthIndex.containsKey(contextCategory)) {
                contextLengthIndex[contextCategory] = mutableSetOf()
            }
            contextLengthIndex[contextCategory]?.add(model.id)
            
            // Index by modalities
            model.inputModalities.forEach { modality ->
                if (!modalityIndex.containsKey("input:$modality")) {
                    modalityIndex["input:$modality"] = mutableSetOf()
                }
                modalityIndex["input:$modality"]?.add(model.id)
            }
            
            model.outputModalities.forEach { modality ->
                if (!modalityIndex.containsKey("output:$modality")) {
                    modalityIndex["output:$modality"] = mutableSetOf()
                }
                modalityIndex["output:$modality"]?.add(model.id)
            }
            
            // Index by pricing
            val pricingCategory = getPricingCategory(model)
            if (!pricingIndex.containsKey(pricingCategory)) {
                pricingIndex[pricingCategory] = mutableSetOf()
            }
            pricingIndex[pricingCategory]?.add(model.id)
        }
    }
    
    /**
     * Gets a filtered and sorted list of models based on filter criteria.
     * Uses indexed data structures for efficient filtering.
     * 
     * @param filters The filter criteria to apply.
     * @param sortOption The sorting option to apply.
     * @return A list of models matching the filters and sorted accordingly.
     */
    fun getFilteredModels(
        filters: Map<ModelFilter.Category, Set<String>>,
        sortOption: ModelFilter.SortOption
    ): List<ModelInfo> {
        // Fast path: if no filters and default sort, return all models
        if (filters.isEmpty() && sortOption == ModelFilter.SortOption.TOP_WEEKLY) {
            return modelsCache.values.toList()
        }
        
        // Set of model IDs that match all filters
        var matchingIds: MutableSet<String>? = null
        
        // Apply each filter category
        filters.forEach { (category, values) ->
            if (values.isEmpty()) return@forEach
            
            // Set of model IDs matching this filter
            val categoryMatches = mutableSetOf<String>()
            
            when (category) {
                ModelFilter.Category.PROVIDER -> {
                    values.forEach { provider ->
                        providerIndex[provider]?.let { categoryMatches.addAll(it) }
                    }
                }
                ModelFilter.Category.CONTEXT_LENGTH -> {
                    values.forEach { contextLength ->
                        contextLengthIndex[contextLength]?.let { categoryMatches.addAll(it) }
                    }
                }
                ModelFilter.Category.INPUT_MODALITY -> {
                    values.forEach { modality ->
                        modalityIndex["input:$modality"]?.let { categoryMatches.addAll(it) }
                    }
                }
                ModelFilter.Category.OUTPUT_MODALITY -> {
                    values.forEach { modality ->
                        modalityIndex["output:$modality"]?.let { categoryMatches.addAll(it) }
                    }
                }
                ModelFilter.Category.PRICING -> {
                    values.forEach { pricing ->
                        pricingIndex[pricing]?.let { categoryMatches.addAll(it) }
                    }
                }
            }
            
            // Intersect with previous matches or initialize matches
            if (matchingIds == null) {
                matchingIds = categoryMatches
            } else {
                matchingIds!!.retainAll(categoryMatches)
            }
            
            // Early exit if no matches
            if (matchingIds?.isEmpty() == true) {
                return emptyList()
            }
        }
        
        // Get full model objects
        val results = if (matchingIds != null) {
            matchingIds!!.mapNotNull { modelsCache[it] }
        } else {
            modelsCache.values.toList()
        }
        
        // Apply sorting
        return when (sortOption) {
            ModelFilter.SortOption.TOP_WEEKLY -> {
                // Custom heuristic sorting for popularity
                results.sortedWith(
                    compareByDescending<ModelInfo> { 
                        it.id.contains("gpt-4") || it.id.contains("claude-3-opus") 
                    }.thenByDescending { 
                        it.id.contains("gpt") || it.id.contains("claude") 
                    }
                )
            }
            ModelFilter.SortOption.NEWEST -> {
                // Since we don't have release dates, use a heuristic based on model names
                results.sortedByDescending { model ->
                    val versionPattern = "(\\d+(\\.\\d+)+)".toRegex()
                    val versionMatch = versionPattern.find(model.name)
                    versionMatch?.value?.replace(".", "") ?: "0"
                }
            }
            ModelFilter.SortOption.PRICE_LOW_TO_HIGH -> {
                results.sortedBy { it.promptPrice + it.completionPrice }
            }
            ModelFilter.SortOption.PRICE_HIGH_TO_LOW -> {
                results.sortedByDescending { it.promptPrice + it.completionPrice }
            }
            ModelFilter.SortOption.CONTEXT_HIGH_TO_LOW -> {
                results.sortedByDescending { it.contextLength }
            }
        }
    }
    
    /**
     * Gets a model by its ID from the cache.
     * 
     * @param modelId The ID of the model to retrieve.
     * @return The model, or null if not found.
     */
    fun getModelById(modelId: String): ModelInfo? {
        return modelsCache[modelId]
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
        // Get filtered models first
        val filteredModels = getFilteredModels(filters, sortOption)
        
        // Calculate start and end indices for pagination
        val startIndex = page * pageSize
        val endIndex = minOf(startIndex + pageSize, filteredModels.size)
        
        // Return empty list if start index is out of bounds
        if (startIndex >= filteredModels.size) {
            return emptyList()
        }
        
        // Return the sublist representing the requested page
        return filteredModels.subList(startIndex, endIndex)
    }
    
    /**
     * Gets the total number of pages based on the filtered models count and page size.
     * 
     * @param pageSize The number of models per page.
     * @param filters Filter criteria to apply.
     * @return The total number of pages.
     */
    fun getTotalPages(
        pageSize: Int,
        filters: Map<ModelFilter.Category, Set<String>> = emptyMap()
    ): Int {
        val filteredCount = getFilteredModelCount(filters)
        return (filteredCount + pageSize - 1) / pageSize  // Ceiling division
    }
    
    /**
     * Gets the total number of models matching the filters.
     * 
     * @param filters The filter criteria to apply.
     * @return The number of matching models.
     */
    fun getFilteredModelCount(filters: Map<ModelFilter.Category, Set<String>> = emptyMap()): Int {
        // Fast path: if no filters, return total count
        if (filters.isEmpty()) {
            return modelsCache.size
        }
        
        // Otherwise, calculate the filtered count
        var matchingIds: MutableSet<String>? = null
        
        // Apply each filter category
        filters.forEach { (category, values) ->
            if (values.isEmpty()) return@forEach
            
            // Set of model IDs matching this filter
            val categoryMatches = mutableSetOf<String>()
            
            when (category) {
                ModelFilter.Category.PROVIDER -> {
                    values.forEach { provider ->
                        providerIndex[provider]?.let { categoryMatches.addAll(it) }
                    }
                }
                ModelFilter.Category.CONTEXT_LENGTH -> {
                    values.forEach { contextLength ->
                        contextLengthIndex[contextLength]?.let { categoryMatches.addAll(it) }
                    }
                }
                ModelFilter.Category.INPUT_MODALITY -> {
                    values.forEach { modality ->
                        modalityIndex["input:$modality"]?.let { categoryMatches.addAll(it) }
                    }
                }
                ModelFilter.Category.OUTPUT_MODALITY -> {
                    values.forEach { modality ->
                        modalityIndex["output:$modality"]?.let { categoryMatches.addAll(it) }
                    }
                }
                ModelFilter.Category.PRICING -> {
                    values.forEach { pricing ->
                        pricingIndex[pricing]?.let { categoryMatches.addAll(it) }
                    }
                }
            }
            
            // Intersect with previous matches or initialize matches
            if (matchingIds == null) {
                matchingIds = categoryMatches
            } else {
                matchingIds!!.retainAll(categoryMatches)
            }
            
            // Early exit if no matches
            if (matchingIds?.isEmpty() == true) {
                return 0
            }
        }
        
        return matchingIds?.size ?: modelsCache.size
    }
    
    /**
     * Gets all available providers from the cached models.
     * 
     * @return A set of provider names.
     */
    fun getAvailableProviders(): Set<String> {
        return providerIndex.keys
    }
    
    /**
     * Gets all available modalities from the cached models.
     * 
     * @param inputOnly If true, returns only input modalities.
     * @param outputOnly If true, returns only output modalities.
     * @return A set of modality names.
     */
    fun getAvailableModalities(inputOnly: Boolean = false, outputOnly: Boolean = false): Set<String> {
        return when {
            inputOnly -> modalityIndex.keys.filter { it.startsWith("input:") }.map { it.substring(6) }.toSet()
            outputOnly -> modalityIndex.keys.filter { it.startsWith("output:") }.map { it.substring(7) }.toSet()
            else -> modalityIndex.keys.map { 
                if (it.startsWith("input:")) it.substring(6)
                else if (it.startsWith("output:")) it.substring(7)
                else it
            }.toSet()
        }
    }
    
    /**
     * Determines the context length category for a model.
     * 
     * @param contextLength The context length in tokens.
     * @return The category string.
     */
    private fun getContextLengthCategory(contextLength: Int): String {
        return when {
            contextLength <= 4096 -> "4K"
            contextLength <= 8192 -> "8K"
            contextLength <= 16384 -> "16K"
            contextLength <= 32768 -> "32K"
            contextLength <= 65536 -> "64K"
            else -> "128K+"
        }
    }
    
    /**
     * Determines the pricing category for a model.
     * 
     * @param model The model to categorize.
     * @return The pricing category string.
     */
    private fun getPricingCategory(model: ModelInfo): String {
        return when {
            model.isFree -> "FREE"
            model.promptPrice < 2.0 && model.completionPrice < 2.0 -> "BUDGET"
            model.promptPrice in 2.0..10.0 || model.completionPrice in 2.0..10.0 -> "STANDARD"
            else -> "PREMIUM"
        }
    }
    
    companion object {
        const val DEFAULT_CACHE_DURATION = 24 * 60 * 60 * 1000L // 24 hours
        const val BACKGROUND_REFRESH_INTERVAL = 12 * 60 * 60 * 1000L // 12 hours
    }
} 