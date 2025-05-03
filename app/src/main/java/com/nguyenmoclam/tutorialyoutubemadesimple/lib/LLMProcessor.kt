package com.nguyenmoclam.tutorialyoutubemadesimple.lib

import com.nguyenmoclam.tutorialyoutubemadesimple.BuildConfig
import com.nguyenmoclam.tutorialyoutubemadesimple.OpenRouterApi
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.LLMConfig
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.Message
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.OpenRouterRequest
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.UsageRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.UserDataRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.content.Topic
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.MultipleChoiceQuestion
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.TrueFalseQuestion
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.SecurePreferences
import kotlinx.coroutines.flow.first
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

/** Custom exception for trial exhaustion */
class TrialExhaustedException(message: String) : Exception(message)

/**
 * Processes YouTube video content using Language Learning Models (LLM) to extract and simplify topics and questions.
 * This class handles the interaction with OpenRouter API to analyze video transcripts and generate child-friendly content.
 *
 * The processing workflow consists of two main steps:
 * 1. Topic Extraction: Analyzes the video transcript to identify key topics and generate relevant questions
 * 2. Content Simplification: Transforms the extracted content into child-friendly format with ELI5 explanations
 *
 * The class uses OpenRouter's API with configurable model selection for natural language processing tasks.
 */
class LLMProcessor @Inject constructor(
    private val networkUtils: NetworkUtils,
    private val openRouterApi: OpenRouterApi,
    private val securePreferences: SecurePreferences,
    private val usageRepository: UsageRepository,
    private val userDataRepository: UserDataRepository,
    initialConfig: LLMConfig? = null
) {
    /**
     * The current LLM configuration being used for API calls.
     * Defaults to the predefined default configuration.
     * NOTE: The modelId within this config might be overridden by SecurePreferences during the call.
     */
    private var currentConfig: LLMConfig = initialConfig ?: LLMConfig.DEFAULT

    /**
     * Error callback for detailed error reporting.
     * Can be set to receive detailed error information.
     */
    private var errorCallback: ((LLMError) -> Unit)? = null

    /**
     * Model unavailability tracking to prevent repeated failures.
     * Maps model IDs to timestamp when they were marked unavailable.
     */
    private val unavailableModels = mutableMapOf<String, Long>()

    /**
     * The cooldown period in milliseconds before retrying an unavailable model.
     * Default is 5 minutes.
     */
    private val MODEL_UNAVAILABILITY_COOLDOWN = 5 * 60 * 1000L

    /**
     * Returns the current LLM configuration.
     *
     * @return The current configuration being used.
     */
    fun getConfig(): LLMConfig {
        return currentConfig
    }

    /**
     * Sets a callback for receiving detailed error reports.
     *
     * @param callback The function to call when errors occur.
     */
    fun setErrorCallback(callback: (LLMError) -> Unit) {
        errorCallback = callback
    }

    /**
     * Checks if a model is currently marked as unavailable.
     *
     * @param modelId The ID of the model to check.
     * @return True if the model is unavailable, false otherwise.
     */
    private fun isModelUnavailable(modelId: String): Boolean {
        val timestamp = unavailableModels[modelId] ?: return false
        val now = System.currentTimeMillis()

        // If the cooldown period has passed, remove the model from unavailable list
        if (now - timestamp > MODEL_UNAVAILABILITY_COOLDOWN) {
            unavailableModels.remove(modelId)
            return false
        }

        return true
    }

    /**
     * Marks a model as unavailable for the cooldown period.
     *
     * @param modelId The ID of the model to mark as unavailable.
     */
    private fun markModelUnavailable(modelId: String) {
        unavailableModels[modelId] = System.currentTimeMillis()
    }

    suspend fun extractTopicsAndQuestions(
        transcript: String,
        title: String,
        language: String = "English"
    ): Pair<List<Topic>, Boolean> {
        val prompt = extractTopicsAndQuestionsPrompt(transcript, title, language)
        val (responseContent, wasFreeCallUsed) = callLLM(
            prompt,
            null
        )

        val keyPoints = parseTopicsFromJson(responseContent, errorCallback)
        return Pair(keyPoints, wasFreeCallUsed)
    }

    suspend fun processContent(
        topics: List<Topic>,
        transcript: String,
        language: String = "English"
    ): Pair<List<Topic>, Boolean> {
        val prompt = processContentPrompt(topics, transcript, language)
        val (responseContent, wasFreeCallUsed) = callLLM(prompt, null) // Deconstruct Pair
        val content = parseBatchProcessedContent(topics, responseContent, errorCallback)

        return Pair(content, wasFreeCallUsed)
    }

    /**
     * Checks API key status and free trial availability.
     * Returns the API key to use (app's shared key or user's key).
     * Throws TrialExhaustedException if trial is over and no user key is provided.
     * Throws Exception for other missing key scenarios.
     * Sets the shouldDecrementFreeTrial flag.
     */
    private suspend fun checkApiKeyAndTrial(
        config: LLMConfig
    ): Pair<String, Boolean> {
        // Get user state from repository
        val user =
            userDataRepository.userStateFlow.first() // Use .first() to get current value in suspend fun
        val freeCallsRemaining = userDataRepository.freeCallsStateFlow.first()

        var shouldDecrementFreeTrial = false

        val apiKey = if (user != null) {
            // User is logged in, check free calls remaining
            val callsLeft = freeCallsRemaining ?: 0

            if (callsLeft > 0) {
                // Use app's shared key for free trial
                shouldDecrementFreeTrial = true
                BuildConfig.OPENROUTER_API_KEY
            } else {
                // No free calls remaining, check for user key
                val userApiKey = securePreferences.getOpenRouterApiKey()
                if (userApiKey.isBlank()) {
                    // No user API key provided - throw specific exception
                    throw TrialExhaustedException("Free trial exhausted and no API key provided. Please enter your OpenRouter API key in settings.")
                }
                userApiKey // Use user's key
            }
        } else if (config.apiKey.isNotBlank()) {
            // Use config-provided key if no user logged in
            config.apiKey
        } else {
            // Fall back to stored user key if no user logged in and no config key
            val userApiKey = securePreferences.getOpenRouterApiKey()
            if (userApiKey.isBlank()) {
                throw Exception("No API key available. Please sign in for free trial or provide your OpenRouter API key.")
            }
            userApiKey
        }
        return Pair(apiKey, shouldDecrementFreeTrial)
    }

    /**
     * Processes a prompt through the configured LLM to get a response.
     * Handles retries, error classification, model fallback, and free trial tracking.
     *
     * @param prompt The prompt to send to the LLM.
     * @param config Optional configuration override for this specific call.
     * @return A Pair containing the LLM's response content (String) and a Boolean indicating if a free trial call was successfully used.
     * @throws TrialExhaustedException If the free trial is exhausted and no user key is provided.
     * @throws Exception For other API errors or configuration issues.
     */
    private suspend fun callLLM(
        prompt: String,
        config: LLMConfig? = null
    ): Pair<String, Boolean> {
        // Check if content should not be loaded based on data saver settings
        if (!networkUtils.shouldLoadContent(highQuality = true)) {
            throw IllegalStateException("Network restricted by data saver settings")
        }

        // Use the provided config or fall back to the current config as a base
        val baseConfig = config ?: currentConfig

        // Get the latest selected model ID from SecurePreferences ---
        val selectedModelId = securePreferences.getSelectedModelId()
        // Ensure selectedModelId is not blank, fallback to default if it is (shouldn't happen due to screen checks, but safe)
        val targetModelId =
            if (selectedModelId.isNotBlank()) selectedModelId else LLMConfig.DEFAULT.modelId
        // Determine the actual model to use, starting with the user's preference
        var modelToUse = targetModelId

        // Check if the *target* model is unavailable and needs a fallback
        if (isModelUnavailable(modelToUse)) {
            val unavailableMessage =
                "Model $modelToUse (user preference or default) is currently unavailable, attempting fallback to application default."
            errorCallback?.invoke(LLMError.ModelUnavailable(modelToUse, unavailableMessage))

            // Fallback to the application's default model ONLY if it's different
            if (modelToUse != LLMConfig.DEFAULT.modelId) {
                modelToUse = LLMConfig.DEFAULT.modelId
                // Check if the application default is ALSO unavailable
                if (isModelUnavailable(modelToUse)) {
                    val defaultUnavailableMessage =
                        "Fallback model $modelToUse (application default) is also currently unavailable."
                    errorCallback?.invoke(
                        LLMError.ModelUnavailable(
                            modelToUse,
                            defaultUnavailableMessage
                        )
                    )
                    throw Exception("Primary model ($targetModelId) and fallback model ($modelToUse) are both unavailable.")
                }
                errorCallback?.invoke(
                    LLMError.ModelUnavailable(
                        modelToUse,
                        "Fell back to application default model: $modelToUse"
                    )
                )
            } else {
                // Already using the application default, and it's unavailable
                throw Exception("Default model $modelToUse is unavailable.")
            }
        }

        // Create the final config to use for the API call, overriding the modelId
        val finalConfig = baseConfig.copy(modelId = modelToUse)

        // Pass finalConfig here to ensure checkApiKeyAndTrial uses the potentially updated model context if needed
        val (apiKey, shouldDecrementFreeTrial) = checkApiKeyAndTrial(finalConfig)

        val messages = listOf(
            Message(role = "user", content = prompt)
        )

        val request = OpenRouterRequest(
            model = finalConfig.modelId, // Use the final model ID
            messages = messages,
            max_tokens = finalConfig.maxTokens,
            temperature = finalConfig.temperature,
            top_p = finalConfig.topP
        )

        val authHeader = "Bearer $apiKey"

        try {
            val response = networkUtils.withConnectionTimeout {
                openRouterApi.createCompletion(authHeader, request)
            }

            // Handle the response
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                // Throw exception immediately on unsuccessful response
                throw Exception("API error: ${response.code()} - $errorBody")
            }

            val apiResponse = response.body() ?: run {
                errorCallback?.invoke(LLMError.PermanentError("Empty response body")) // Report error
                // Throw exception immediately on empty body
                throw Exception("Empty response body")
            }

            // Check for error field in response body (even when HTTP status is 200)
            apiResponse.error?.let { error ->
                val errorMessage = "API returned error: ${error.code} - ${error.message}"

                // Classify based on the error code in the response body
                val errorType = when (error.code.toString().lowercase()) {
                    // Add specific codes that indicate model unavailability if known
                    "model_not_found", "invalid_model" -> ErrorType.MODEL_UNAVAILABLE
                    // Add specific codes indicating temporary issues if known
                    "rate_limit_exceeded", "server_error" -> ErrorType.TEMPORARY
                    // Treat most other API-reported errors as permanent
                    else -> ErrorType.PERMANENT // Default to permanent for unknown body errors
                }

                val llmError = when (errorType) {
                    ErrorType.MODEL_UNAVAILABLE -> {
                        markModelUnavailable(finalConfig.modelId) // Mark unavailable on error using the attempted model
                        LLMError.ModelUnavailable(
                            finalConfig.modelId, // Keep original model ID for error reporting
                            "Model unavailable reported by API: ${error.message}"
                        )
                    }

                    ErrorType.TEMPORARY -> LLMError.TemporaryError("Temporary error reported by API: ${error.message}")
                    ErrorType.PERMANENT -> LLMError.PermanentError("Permanent error reported by API: ${error.code} - ${error.message}")
                }
                errorCallback?.invoke(llmError) // Report the classified error

                // Throw exception immediately based on error field in response body
                throw Exception(errorMessage)
            }

            val rawContent = apiResponse.choices.firstOrNull()?.message?.content ?: ""

            // Reset unavailability status if the call succeeded for this model
            if (unavailableModels.containsKey(finalConfig.modelId)) { // Check using the model actually used
                unavailableModels.remove(finalConfig.modelId) // Remove using the model actually used
            }

            // Record token usage for successful responses
            try {
                val usageInfo = apiResponse.usage
                if (usageInfo != null) {
                    // Get model pricing information (you may need to implement a model price lookup function)
                    val promptPrice = 0.0 // Default or get from a pricing service
                    val completionPrice = 0.0 // Default or get from a pricing service

                    // Create TokenUsage object first
                    val tokenUsage = usageRepository.createTokenUsage(
                        modelId = finalConfig.modelId, // Log the model actually used
                        modelName = apiResponse.model
                            ?: finalConfig.modelId, // Use actual model from response or the one we sent
                        promptTokens = usageInfo.prompt_tokens ?: 0,
                        completionTokens = usageInfo.completion_tokens ?: 0,
                        promptPrice = promptPrice,
                        completionPrice = completionPrice
                    )
                    // Then record it
                    usageRepository.recordTokenUsage(tokenUsage)
                }
            } catch (e: Exception) {
                errorCallback?.invoke(LLMError.TrackingError("Failed to record token usage: ${e.message}"))
            }

            // Return content and whether a free call was used
            return Pair(rawContent, shouldDecrementFreeTrial)
        } catch (e: TrialExhaustedException) {
            throw e
        } catch (e: Exception) {
            // Classify the caught exception (could be network error, timeout, or exceptions thrown above)
            val errorType = classifyError(e)

            // Create the specific LLMError instance based on the classified error
            val llmError = when (errorType) {
                ErrorType.MODEL_UNAVAILABLE -> {
                    markModelUnavailable(finalConfig.modelId) // Mark unavailable on error using the attempted model
                    LLMError.ModelUnavailable(
                        finalConfig.modelId,
                        "Model unavailable: ${e.message}"
                    )
                }

                ErrorType.TEMPORARY -> LLMError.TemporaryError("Temporary error: ${e.message}")
                ErrorType.PERMANENT -> LLMError.PermanentError("Permanent error: ${e.message}")
            }

            // Avoid double-reporting specific errors already reported before throwing
            if (e.message != "Empty response body" && e.message?.startsWith("API error:") != true && e.message?.startsWith(
                    "API returned error:"
                ) != true && llmError !is LLMError.TrackingError
            ) {
                errorCallback?.invoke(llmError)
            }

            // Always re-throw the original exception to propagate the failure
            throw e
        }
    }

    /**
     * Classifies an error to determine if it's temporary, permanent, or model-specific.
     *
     * @param error The error to classify.
     * @return The ErrorType classification.
     */
    private fun classifyError(error: Throwable): ErrorType {
        return when {
            // Model unavailable errors
            error.message?.contains("model_not_found") == true -> ErrorType.MODEL_UNAVAILABLE
            error.message?.contains("invalid model") == true -> ErrorType.MODEL_UNAVAILABLE
            error.message?.contains("model capacity") == true -> ErrorType.MODEL_UNAVAILABLE
            error.message?.contains("not available") == true -> ErrorType.MODEL_UNAVAILABLE

            // Temporary errors
            error is SocketTimeoutException -> ErrorType.TEMPORARY
            error is IOException -> ErrorType.TEMPORARY
            error.message?.contains("timeout") == true -> ErrorType.TEMPORARY
            error.message?.contains("429") == true -> ErrorType.TEMPORARY  // Too many requests
            error.message?.contains("503") == true -> ErrorType.TEMPORARY  // Service unavailable
            error.message?.contains("502") == true -> ErrorType.TEMPORARY  // Bad gateway

            // Permanent errors
            error.message?.contains("401") == true -> ErrorType.PERMANENT  // Unauthorized
            error.message?.contains("403") == true -> ErrorType.PERMANENT  // Forbidden
            error.message?.contains("400") == true -> ErrorType.PERMANENT  // Bad request
            error.message?.contains("404") == true -> ErrorType.PERMANENT  // Not found

            // Default to temporary for unknown errors
            else -> ErrorType.TEMPORARY
        }
    }

    /**
     * Error types for classifying API errors.
     */
    private enum class ErrorType {
        TEMPORARY,      // Can be retried
        PERMANENT,      // Should not be retried
        MODEL_UNAVAILABLE // Model-specific error, should fallback
    }

    suspend fun extractKeyPoints(
        transcript: String,
        language: String
    ): Pair<List<String>, Boolean> {
        val prompt = parseBatchProcessedContentPrompt(transcript, language)
        val (responseContent, wasFreeCallUsed) = callLLM(prompt, null)
        val cleanJson = if (responseContent.contains("```")) {
            responseContent.substringAfter("```json").substringBefore("```").trim()
        } else {
            responseContent.trim()
        }

        val keyPoints = parseKeyPointsFromJson(cleanJson, errorCallback)
        return Pair(keyPoints, wasFreeCallUsed)
    }

    suspend fun generateQuestionsFromKeyPoints(
        keyPoints: List<String>,
        language: String,
        questionType: String,
        numberOfQuestions: Int
    ): Pair<String, Boolean> {
        val prompt = generateQuestionsFromKeyPointsPrompt(
            keyPoints,
            language,
            questionType,
            numberOfQuestions
        )
        // Call callLLM and return its result directly
        return callLLM(prompt, null)
    }

    fun parseQuizQuestions(jsonResponse: String): Pair<List<MultipleChoiceQuestion>, List<TrueFalseQuestion>> {
        val cleanJson = if (jsonResponse.contains("```")) {
            jsonResponse.substringAfter("```json").substringBefore("```").trim()
        } else {
            jsonResponse.trim()
        }

        return try {
            parseQuestionsFromJson(cleanJson)
        } catch (e: Exception) {
            Pair(emptyList(), emptyList())
        }
    }

    suspend fun extractKeyPointsForMindMap(
        transcript: String,
        title: String,
        language: String = "English"
    ): List<String> {
        val prompt = extractKeyPointsForMindMapPrompt(transcript, title, language)
        val (responseContent, _) = callLLM(prompt, null)
        return parseKeyPointsFromJsonToListString(responseContent, errorCallback)
    }

    suspend fun generateMermaidMindMapCode(
        keyPoints: List<String>,
        title: String,
        language: String = "English"
    ): String {
        if (keyPoints.isEmpty()) return ""

        val prompt = generateMermaidMindMapCodePrompt(keyPoints, title, language)
        val (responseContent, _) = callLLM(prompt, null)
        // Extract the Mermaid code block content from the LLM response
        return parseMermaidMindMapCleanCode(responseContent)
    }

    suspend fun fixMindMapCode(
        originalCode: String,
        errorMessage: String,
        language: String = "English"
    ): String {
        val prompt = fixMindMapCodePrompt(originalCode, errorMessage, language)
        val (responseContent, _) = callLLM(prompt, null)
        // Extract the Mermaid code from the LLM response
        return parseMermaidMindMapCleanCode(responseContent)
    }

    /**
     * Represents errors that can occur during LLM operations.
     */
    sealed class LLMError {
        /**
         * Error when a model is unavailable and fallback is used.
         */
        data class ModelUnavailable(val modelId: String, val message: String) : LLMError()

        /**
         * Error for temporary issues that were retried.
         */
        data class TemporaryError(val message: String) : LLMError()

        /**
         * Error for permanent issues that cannot be resolved with retries.
         */
        data class PermanentError(val message: String) : LLMError()

        /**
         * Error when all retries are exhausted.
         */
        data class RetriesExhausted(val message: String) : LLMError()

        /**
         * Error when tracking token usage fails.
         */
        data class TrackingError(val message: String) : LLMError()
    }
}
