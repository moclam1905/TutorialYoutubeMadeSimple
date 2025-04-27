package com.nguyenmoclam.tutorialyoutubemadesimple.data.service

import com.nguyenmoclam.tutorialyoutubemadesimple.OpenRouterApi
import com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper.ModelMapper
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.Result
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
     * @return A Result containing a list of ModelInfo or an error.
     */
    suspend fun getAvailableModels(apiKey: String = ""): Result<List<ModelInfo>> {
        val key = apiKey.ifEmpty { securePreferences.getOpenRouterApiKey() }
        if (key.isEmpty()) {
            return Result.Failure(IllegalStateException("API key not provided"))
        }
        
        val authHeader = "Bearer $key"
        
        return try {
            // Use withConnectionTimeout to apply user's timeout setting
            val response = networkUtils.withConnectionTimeout {
                openRouterApi.getAvailableModels(authHeader)
            }
            
            when (response) {
                is Result.Success -> {
                    // Now check the inner result (response.value)
                    when (val innerResult = response.value) {
                        is Result.Success -> {
                            // Use ModelMapper for transformation
                            val modelsList = ModelMapper.toModelInfoList(innerResult.value.data)
                            Result.Success(modelsList)
                        }
                        is Result.Failure -> Result.Failure(innerResult.error) // Propagate inner failure
                    }
                }
                is Result.Failure -> Result.Failure(response.error) // Propagate outer failure (e.g., timeout)
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
    
    /**
     * Fetches the user's credits information from OpenRouter.
     * 
     * @param apiKey The OpenRouter API key to use. If empty, uses the stored key.
     * @return A Result containing OpenRouterCreditsResponse or an error.
     */
    suspend fun getCredits(apiKey: String = ""): Result<OpenRouterCreditsResponse> {
        val key = apiKey.ifEmpty { securePreferences.getOpenRouterApiKey() }
        if (key.isEmpty()) {
            return Result.Failure(IllegalStateException("API key not provided"))
        }
        
        val authHeader = "Bearer $key"
        
        return try {
            // Use withConnectionTimeout to apply user's timeout setting
            val responseResult = networkUtils.withConnectionTimeout {
                openRouterApi.getCredits(authHeader)
            }

            // Handle the nested Result structure
            when (responseResult) {
                is Result.Success -> {
                    // The outer Result was successful, now check the inner Result
                    when (val innerResult = responseResult.value) {
                        is Result.Success -> Result.Success(innerResult.value) // Return the OpenRouterCreditsResponse
                        is Result.Failure -> Result.Failure(innerResult.error) // Propagate inner failure
                    }
                }
                is Result.Failure -> Result.Failure(responseResult.error) // Propagate outer failure (e.g., timeout)
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}