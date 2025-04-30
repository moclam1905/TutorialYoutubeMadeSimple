package com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper

import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.ModelInfo
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.OpenRouterModel

/**
 * Mapper functions to convert between OpenRouter API model objects and application model objects.
 */
object ModelMapper {
    
    /**
     * Converts an OpenRouterModel to a ModelInfo object.
     * Handles parsing of String prices to Double.
     * 
     * @param model The OpenRouterModel from the API response.
     * @return A ModelInfo object with extracted information.
     */
    fun toModelInfo(model: OpenRouterModel): ModelInfo {
        // Extract provider name from the model ID (format: provider/model-name)
        val providerName = model.id.split("/").firstOrNull() ?: ""
        
        // Extract input and output modalities from architecture
        val inputModalities = model.architecture?.input_modalities ?: emptyList()
        val outputModalities = model.architecture?.output_modalities ?: emptyList()
        
        // Extract tokenizer type from architecture
        val tokenizerType = model.architecture?.tokenizer ?: "Unknown"
        
        // Safely convert string prices to Double, defaulting to 0.0 if invalid
        val promptPriceDouble = model.pricing.prompt.toDoubleOrNull() ?: 0.0
        val completionPriceDouble = model.pricing.completion.toDoubleOrNull() ?: 0.0

        // Check if model is free based on the parsed Double values
        val isFree = promptPriceDouble == 0.0 && completionPriceDouble == 0.0
        
        // Check if model is moderated from the top_provider field
        val isModerated = model.top_provider?.is_moderated ?: false
        
        return ModelInfo(
            id = model.id,
            name = model.name,
            contextLength = model.context_length,
            promptPrice = promptPriceDouble, // Use parsed Double
            completionPrice = completionPriceDouble, // Use parsed Double
            tokenizerType = tokenizerType,
            inputModalities = inputModalities,
            outputModalities = outputModalities,
            providerName = providerName,
            isFree = isFree, // Use updated check
            isModerated = isModerated // Include moderation status
        )
    }
    
    /**
     * Converts a list of OpenRouterModel objects to a list of ModelInfo objects.
     * 
     * @param models The list of OpenRouterModel objects from the API response.
     * @return A list of ModelInfo objects.
     */
    fun toModelInfoList(models: List<OpenRouterModel>): List<ModelInfo> {
        return models.map { toModelInfo(it) }
    }
} 