package com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(val role: String, val content: String)

@Serializable
data class Choice(val message: Message)

@Serializable
data class UsageInfo(
    @SerialName("prompt_tokens") val promptTokens: Int? = null,
    @SerialName("completion_tokens") val completionTokens: Int? = null,
    @SerialName("total_tokens") val totalTokens: Int? = null
)

@Serializable
data class OpenRouterResponse(
    val choices: List<Choice>,
    val usage: UsageInfo? = null
)

/**
 * Request body for OpenRouter chat completions API.
 *
 * @property model The ID of the model to use.
 * @property messages The messages to generate chat completions for.
 * @property maxTokens The maximum number of tokens to generate in the response.
 * @property temperature Controls randomness in output. Lower values make output more deterministic.
 * @property topP Controls diversity by only considering top P% of probability mass.
 */
data class OpenRouterRequest(
    val model: String,
    val messages: List<Message>,
    val max_tokens: Int? = null,
    val temperature: Double? = null,
    val top_p: Double? = null
)

/**
 * Represents information about an AI model available through OpenRouter.
 * 
 * @property id Unique identifier for the model.
 * @property name Display name of the model.
 * @property contextLength Maximum token context length supported by the model.
 * @property promptPrice Price per 1M tokens for prompts (input).
 * @property completionPrice Price per 1M tokens for completions (output).
 * @property tokenizerType The type of tokenizer used by the model.
 * @property inputModalities List of input modalities supported by the model (e.g., text, image).
 * @property outputModalities List of output modalities supported by the model (e.g., text, code).
 * @property providerName Name of the model provider (e.g., Anthropic, OpenAI).
 * @property isFree Whether the model is available for free usage.
 */
data class ModelInfo(
    val id: String,
    val name: String,
    val contextLength: Int,
    val promptPrice: Double,
    val completionPrice: Double,
    val tokenizerType: String,
    val inputModalities: List<String>,
    val outputModalities: List<String>,
    val providerName: String = "",
    val isFree: Boolean = false
)

/**
 * Configuration parameters for LLM operations.
 *
 * @property modelId The ID of the model to use for requests (e.g., "anthropic/claude-3-opus-20240229").
 * @property apiKey The API key for authenticating with OpenRouter.
 * @property maxTokens Maximum number of tokens to generate in the response.
 * @property temperature Controls randomness in output. Lower values make output more deterministic.
 * @property topP Controls diversity by only considering top P% of probability mass.
 */
data class LLMConfig(
    val modelId: String,
    val apiKey: String,
    val maxTokens: Int = 4096,
    val temperature: Double = 0.7,
    val topP: Double = 0.95
) {
    companion object {
        /** Default configuration to use as fallback */
        val DEFAULT = LLMConfig(
            modelId = "deepseek/deepseek-chat-v3-0324:free",
            apiKey = "",
            maxTokens = 4096,
            temperature = 0.7,
            topP = 0.95
        )
    }
}

/**
 * Represents pricing information for a model.
 * 
 * @property prompt Price per 1M tokens for input/prompt.
 * @property completion Price per 1M tokens for output/completion.
 */
data class ModelPricing(
    val prompt: Double,
    val completion: Double
)

/**
 * Represents context information for a model.
 * 
 * @property maxTokens Maximum number of tokens the model can process in a single request.
 */
data class ModelContext(
    val maxTokens: Int
)

/**
 * Represents a model from the OpenRouter API models listing.
 * 
 * @property id Unique identifier for the model.
 * @property name Display name of the model.
 * @property pricing Pricing information for the model.
 * @property context Context information for the model.
 * @property modalities Input and output modalities supported by the model.
 * @property tokenizer Information about the tokenizer used by the model.
 */
data class OpenRouterModel(
    val id: String,
    val name: String,
    val pricing: ModelPricing,
    val context: ModelContext,
    val modalities: Map<String, List<String>>,
    val tokenizer: Map<String, String>
)

/**
 * Represents the response from the OpenRouter API's model listing endpoint.
 * 
 * @property data List of available models.
 */
data class OpenRouterModelsResponse(
    val data: List<OpenRouterModel>
)