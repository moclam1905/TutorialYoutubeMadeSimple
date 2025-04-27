package com.nguyenmoclam.tutorialyoutubemadesimple.data.service

import com.nguyenmoclam.tutorialyoutubemadesimple.OpenRouterApi
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.ApiKeyValidationState
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.Result
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.ModelInfo
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.OpenRouterCreditsResponse
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.OpenRouterModelsResponse
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.ApiKeyValidator
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.SecurePreferences
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service class that handles interaction with the OpenRouter API.
 * Provides methods for fetching available models, credits information,
 * and validating API keys.
 */
@Singleton
class OpenRouterService @Inject constructor(
    private val openRouterApi: OpenRouterApi,
    private val networkUtils: NetworkUtils,
    private val securePreferences: SecurePreferences,
    private val apiKeyValidator: ApiKeyValidator
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
                            // Transform OpenRouterModelsResponse to List<ModelInfo>
                            val modelsList = innerResult.value.data.map { model ->
                                ModelInfo(
                                    id = model.id,
                                    name = model.name,
                                    contextLength = model.context.maxTokens,
                                    promptPrice = model.pricing.prompt,
                                    completionPrice = model.pricing.completion,
                                    tokenizerType = model.tokenizer["type"] ?: "unknown",
                                    inputModalities = model.modalities["input"] ?: emptyList(),
                                    outputModalities = model.modalities["output"] ?: emptyList(),
                                    providerName = extractProviderFromId(model.id),
                                    isFree = model.pricing.prompt == 0.0 && model.pricing.completion == 0.0
                                )
                            }
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

            // Handle the outer Result (from withConnectionTimeout)
            when (responseResult) {
                is Result.Success -> {
                    // Now handle the inner Response (from Retrofit)
                    val retrofitResponse = responseResult.value
                    if (retrofitResponse.isSuccessful) {
                        val creditsMap = retrofitResponse.body() ?: emptyMap()
                        Result.Success(OpenRouterCreditsResponse.fromMap(creditsMap))
                    } else {
                        Result.Failure(
                            Exception("Failed to fetch credits: ${retrofitResponse.code()} ${retrofitResponse.message()}")
                        )
                    }
                }
                is Result.Failure -> {
                    // Propagate the failure from withConnectionTimeout (e.g., timeout)
                    Result.Failure(responseResult.error)
                }
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
    
    /**
     * Validates an OpenRouter API key by checking its format and testing it against the API.
     * Uses the ApiKeyValidator to perform comprehensive validation.
     * 
     * @param apiKey The API key to validate.
     * @return A Result containing an ApiKeyValidationState indicating validation status.
     */
    suspend fun validateApiKey(apiKey: String): Result<ApiKeyValidationState> {
        if (apiKey.isBlank()) {
            return Result.Success(ApiKeyValidationState.INVALID_FORMAT)
        }
        
        return try {
            val validationState = apiKeyValidator.validateOpenRouterKey(apiKey)
            Result.Success(validationState)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
    
    /**
     * Extracts the provider name from a model ID.
     * 
     * @param modelId The model ID, typically in format "provider/model-name".
     * @return The extracted provider name or an empty string if not found.
     */
    private fun extractProviderFromId(modelId: String): String {
        return modelId.split("/").firstOrNull() ?: ""
    }
} 