package com.nguyenmoclam.tutorialyoutubemadesimple.utils

import com.nguyenmoclam.tutorialyoutubemadesimple.OpenRouterApi
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.ApiKeyValidationState
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.Result
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.OpenRouterCreditsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

/**
 * Utility for validating API keys, including format validation and 
 * network validation to ensure they work with their respective services.
 */
@Singleton
class ApiKeyValidator @Inject constructor(
    private val openRouterApi: OpenRouterApi,
    private val networkUtils: NetworkUtils
) {
    companion object {
        // Regex pattern for OpenRouter API key format validation
        // OpenRouter keys typically follow the pattern "sk-or-..." followed by alphanumeric characters
        private val OPENROUTER_API_KEY_PATTERN = Regex("^sk-or-[a-zA-Z0-9-]{10,50}$")
        
        // Example of valid OpenRouter API key format for user guidance
        private const val OPENROUTER_API_KEY_EXAMPLE = "sk-or-abcd1234-xxxx-yyyy-zzzz"
        
        // Timeout for network validation in milliseconds
        private const val VALIDATION_TIMEOUT_MS = 10000L // 10 seconds
        
        // Retry configuration
        private const val MAX_RETRIES = 3
        private const val INITIAL_BACKOFF_MS = 1000L // Start with 1 second delay
        private const val BACKOFF_MULTIPLIER = 1.5f // Each retry increases delay by this factor
    }

    /**
     * Validates an OpenRouter API key format.
     * 
     * @param apiKey The API key to validate
     * @return True if the format is valid, false otherwise
     */
    fun validateOpenRouterKeyFormat(apiKey: String): Boolean {
        if (apiKey.isBlank()) return false
        return OPENROUTER_API_KEY_PATTERN.matches(apiKey)
    }

    /**
     * Validates an OpenRouter API key by making a network request to verify it works.
     * Performs both format validation and network validation with retry mechanism.
     * 
     * @param apiKey The API key to validate
     * @return The validation state after the process
     */
    suspend fun validateOpenRouterKey(apiKey: String): ApiKeyValidationState = withContext(Dispatchers.IO) {
        // First check if key is blank
        if (apiKey.isBlank()) {
            return@withContext ApiKeyValidationState.INVALID_FORMAT
        }
        
        // Then check connectivity
        if (!networkUtils.isNetworkAvailable()) {
            return@withContext ApiKeyValidationState.NETWORK_ERROR
        }

        // Then validate format
        if (!validateOpenRouterKeyFormat(apiKey)) {
            return@withContext ApiKeyValidationState.INVALID_FORMAT
        }

        // Finally validate via network with retries
        var currentDelay = INITIAL_BACKOFF_MS
        var attempts = 0
        
        while (attempts < MAX_RETRIES) {
            attempts++
            
            try {
                val authHeader = "Bearer $apiKey"
                
                // Use withTimeoutOrNull to prevent hanging on slow connections
                val validationResult: Result<OpenRouterCreditsResponse>? = withTimeoutOrNull(VALIDATION_TIMEOUT_MS) {
                    // Call the service method which returns Result<OpenRouterCreditsResponse>
                    openRouterApi.getCredits(authHeader = authHeader)
                }
                
                if (validationResult == null) {
                    // Timeout occurred
                    if (attempts >= MAX_RETRIES) {
                        return@withContext ApiKeyValidationState.NETWORK_ERROR
                    }
                    delay(currentDelay)
                    currentDelay = (currentDelay * BACKOFF_MULTIPLIER).toLong()
                    continue
                }
                
                // Handle the single Result<OpenRouterCreditsResponse>
                when (validationResult) {
                    is Result.Success -> {
                        // Successfully fetched credits - API key is valid
                        return@withContext ApiKeyValidationState.VALID
                    }
                    is Result.Failure -> {
                        // Handle specific errors from the failure
                        val error = validationResult.error
                        if (error is HttpException) {
                            when (error.code()) {
                                401, 403 -> return@withContext ApiKeyValidationState.INVALID_NETWORK
                                429 -> { // Rate limited
                                    if (attempts >= MAX_RETRIES) return@withContext ApiKeyValidationState.ERROR
                                    delay(currentDelay)
                                    currentDelay = (currentDelay * BACKOFF_MULTIPLIER).toLong()
                                    continue // Retry
                                }
                                in 500..599 -> { // Server error
                                    if (attempts >= MAX_RETRIES) return@withContext ApiKeyValidationState.NETWORK_ERROR
                                    delay(currentDelay)
                                    currentDelay = (currentDelay * BACKOFF_MULTIPLIER).toLong()
                                    continue // Retry
                                }
                                else -> return@withContext ApiKeyValidationState.ERROR // Other HTTP errors
                            }
                        } else if (error is SocketTimeoutException || error is UnknownHostException || error is IOException) {
                            // Handle specific network errors that might be wrapped in Failure
                            if (attempts >= MAX_RETRIES) return@withContext ApiKeyValidationState.NETWORK_ERROR
                            delay(currentDelay)
                            currentDelay = (currentDelay * BACKOFF_MULTIPLIER).toLong()
                            continue // Retry
                        } else {
                            // Other non-HTTP/network errors within the result
                            return@withContext ApiKeyValidationState.ERROR
                        }
                    }
                }
            } catch (e: Exception) {
                // Catch any other unexpected exceptions during the process (e.g., exceptions before the API call)
                if (attempts >= MAX_RETRIES) {
                    return@withContext ApiKeyValidationState.ERROR
                }
                delay(currentDelay)
                currentDelay = (currentDelay * BACKOFF_MULTIPLIER).toLong()
                continue // Retry
            }
        }
        
        // If we've exhausted retries
        return@withContext ApiKeyValidationState.NETWORK_ERROR
    }
    
    /**
     * Alternative validation that returns a detailed error message with format examples.
     * 
     * @param apiKey The API key to validate
     * @return A Pair of ValidationState and optional error message
     */
    suspend fun validateOpenRouterKeyWithMessage(apiKey: String): Pair<ApiKeyValidationState, String?> {
        return when (val state = validateOpenRouterKey(apiKey)) {
            ApiKeyValidationState.INVALID_FORMAT -> 
                Pair(state, "Invalid API key format. Keys should match the format: $OPENROUTER_API_KEY_EXAMPLE")
            ApiKeyValidationState.INVALID_NETWORK -> 
                Pair(state, "API key rejected by OpenRouter. Please check that your key is correct.")
            ApiKeyValidationState.NETWORK_ERROR -> 
                Pair(state, "Network error occurred. Please check your internet connection and try again.")
            ApiKeyValidationState.ERROR -> 
                Pair(state, "An unexpected error occurred during validation.")
            else -> Pair(state, null)
        }
    }
    
    /**
     * Gets the example key format for UI display.
     * 
     * @return String representation of a valid key format
     */
    fun getKeyFormatExample(): String {
        return OPENROUTER_API_KEY_EXAMPLE
    }
} 