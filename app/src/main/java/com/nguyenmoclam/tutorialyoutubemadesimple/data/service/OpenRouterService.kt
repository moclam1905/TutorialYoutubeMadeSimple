package com.nguyenmoclam.tutorialyoutubemadesimple.data.service

import com.nguyenmoclam.tutorialyoutubemadesimple.OpenRouterApi
import com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper.ModelMapper
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.ModelInfo
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.OpenRouterCreditsResponse
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.SecurePreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service class that handles interaction with the OpenRouter API.
 * Provides methods for fetching available models and credits information.
 */
@Singleton
class OpenRouterService @Inject constructor(
    private val openRouterApi: OpenRouterApi,
    private val networkUtils: NetworkUtils,
    private val securePreferences: SecurePreferences
) {
    /**
     * Fetches available AI models from OpenRouter.
     * 
     * @param apiKey The OpenRouter API key to use. If empty, uses the stored key.
     * @return A list of ModelInfo or throws an exception if the operation fails.
     */
    suspend fun getAvailableModels(apiKey: String = ""): List<ModelInfo> {
        val key = apiKey.ifEmpty { securePreferences.getOpenRouterApiKey() }
        if (key.isEmpty()) {
            throw IllegalStateException("API key not provided")
        }
        
        val authHeader = "Bearer $key"
        
        // Use withConnectionTimeout to apply user's timeout setting
        val response = networkUtils.withConnectionTimeout {
            openRouterApi.getAvailableModels(authHeader)
        }
        
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            throw Exception("API error: ${response.code()} - $errorBody")
        }
        
        val responseBody = response.body() ?: throw Exception("Empty response body")
        return ModelMapper.toModelInfoList(responseBody.data)
    }
    
    /**
     * Fetches the user's credits information from OpenRouter.
     * 
     * @param apiKey The OpenRouter API key to use. If empty, uses the stored key.
     * @return The OpenRouterCreditsResponse or throws an exception if the operation fails.
     */
    suspend fun getCredits(apiKey: String = ""): OpenRouterCreditsResponse {
        val key = apiKey.ifEmpty { securePreferences.getOpenRouterApiKey() }
        if (key.isEmpty()) {
            throw IllegalStateException("API key not provided")
        }
        
        val authHeader = "Bearer $key"
        
        // Use withConnectionTimeout to apply user's timeout setting
        val response = networkUtils.withConnectionTimeout {
            openRouterApi.getCredits(authHeader)
        }
        
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            throw Exception("API error: ${response.code()} - $errorBody")
        }
        
        return response.body() ?: throw Exception("Empty response body")
    }
}