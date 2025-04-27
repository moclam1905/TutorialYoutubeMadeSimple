package com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper

import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.ModelInfo
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.OpenRouterModel

/**
 * Mapper functions to convert between OpenRouter API model objects and application model objects.
 */
object ModelMapper {
    
    /**
     * Converts an OpenRouterModel to a ModelInfo object.
     * 
     * @param model The OpenRouterModel from the API response.
     * @return A ModelInfo object with extracted information.
     */
    fun toModelInfo(model: OpenRouterModel): ModelInfo {
        // Extract provider name from the model ID (format: provider/model-name)
        val providerName = model.id.split("/").firstOrNull() ?: ""
        
        // Extract input and output modalities from architecture
        val inputModalities = model.architecture?.inputModalities ?: emptyList()
        val outputModalities = model.architecture?.outputModalities ?: emptyList()
        
        // Extract tokenizer type from architecture
        val tokenizerType = model.architecture?.tokenizer ?: "Unknown"
        
        // Check if model is free (both prompt and completion price are 0)
        val isFree = model.pricing.prompt == 0.0 && model.pricing.completion == 0.0
        
        return ModelInfo(
            id = model.id,
            name = model.name,
            contextLength = model.contextLength,
            promptPrice = model.pricing.prompt,
            completionPrice = model.pricing.completion,
            tokenizerType = tokenizerType,
            inputModalities = inputModalities,
            outputModalities = outputModalities,
            providerName = providerName,
            isFree = isFree
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