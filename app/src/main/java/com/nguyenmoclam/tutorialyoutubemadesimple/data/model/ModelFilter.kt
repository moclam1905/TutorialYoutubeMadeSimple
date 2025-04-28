package com.nguyenmoclam.tutorialyoutubemadesimple.data.model

/**
 * Contains filter categories and sort options for model listings.
 * Used to standardize filtering and sorting operations across the app.
 */
class ModelFilter {
    /**
     * Categories for filtering models.
     */
    enum class Category {
        PROVIDER,       // Filter by model provider (e.g., OpenAI, Anthropic)
        CONTEXT_LENGTH, // Filter by context length ranges
        INPUT_MODALITY, // Filter by input types (text, image, etc.)
        OUTPUT_MODALITY,// Filter by output types (text, code, etc.)
        PRICING         // Filter by pricing tiers
    }
    
    /**
     * Options for sorting model lists.
     */
    enum class SortOption {
        TOP_WEEKLY,         // Most popular models
        NEWEST,             // Most recently released models
        PRICE_LOW_TO_HIGH,  // Cheapest models first
        PRICE_HIGH_TO_LOW,  // Most expensive models first
        CONTEXT_HIGH_TO_LOW, // Highest context length first
        MODERATED_FIRST     // Moderated models first
    }
    
    /**
     * Predefined context length ranges for filtering.
     */
    object ContextLengths {
        const val RANGE_4K = "4K"
        const val RANGE_8K = "8K"
        const val RANGE_16K = "16K"
        const val RANGE_32K = "32K"
        const val RANGE_64K = "64K"
        const val RANGE_128K_PLUS = "128K+"
        
        val ALL = listOf(RANGE_4K, RANGE_8K, RANGE_16K, RANGE_32K, RANGE_64K, RANGE_128K_PLUS)
    }
    
    /**
     * Predefined pricing tiers for filtering.
     */
    object PricingTiers {
        const val FREE = "FREE"
        const val BUDGET = "BUDGET"
        const val STANDARD = "STANDARD"
        const val PREMIUM = "PREMIUM"
        
        val ALL = listOf(FREE, BUDGET, STANDARD, PREMIUM)
    }
    
    /**
     * Common input modalities for filtering.
     */
    object InputModalities {
        const val TEXT = "text"
        const val IMAGE = "image"
        const val FILE = "file"
        const val AUDIO = "audio"
        
        val ALL = listOf(TEXT, IMAGE, FILE, AUDIO)
    }
    
    /**
     * Common output modalities for filtering.
     */
    object OutputModalities {
        const val TEXT = "text"
        const val CODE = "code"
        const val IMAGE = "image"
        
        val ALL = listOf(TEXT, CODE, IMAGE)
    }
    
    companion object {
        /**
         * Creates a filter map with a single category filter.
         * 
         * @param category The filter category.
         * @param values The values to filter by.
         * @return A map with the specified filter.
         */
        fun singleFilter(category: Category, vararg values: String): Map<Category, Set<String>> {
            return mapOf(category to values.toSet())
        }
        
        /**
         * Creates an empty filter map.
         * 
         * @return An empty filter map.
         */
        fun emptyFilter(): Map<Category, Set<String>> {
            return emptyMap()
        }
    }
} 