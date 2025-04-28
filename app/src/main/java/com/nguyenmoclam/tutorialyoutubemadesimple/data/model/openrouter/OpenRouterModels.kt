package com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Message(
    val role: String,
    val content: String,
    val refusal: String? = null,
    val reasoning: String? = null
)

@Serializable
data class Choice(
    val message: Message,
    val logprobs: String? = null,
    val finish_reason: String? = null,
    val native_finish_reason: String? = null,
    val index: Int? = null
)

@Serializable
data class PromptTokensDetails(
    val cached_tokens: Int? = null
)

@Serializable
data class CompletionTokensDetails(
    val reasoning_tokens: Int? = null
)

@Serializable
data class UsageInfo(
    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null,
    val prompt_tokens_details: PromptTokensDetails? = null,
    val completion_tokens_details: CompletionTokensDetails? = null
)

@Serializable
data class OpenRouterResponse(
    val id: String? = null,
    val provider: String? = null,
    val model: String? = null,
    val created: Long? = null,
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
 * @property prompt Price per token for input/prompt.
 * @property completion Price per token for output/completion.
 * @property request Price per request.
 * @property image Price for image processing.
 * @property web_search Price for web search functionality.
 * @property internal_reasoning Price for internal reasoning.
 */
@Serializable
data class ModelPricing(
    val prompt: String,
    val completion: String,
    val request: String? = null,
    val image: String? = null,
    val web_search: String? = null,
    val internal_reasoning: String? = null,
    val input_cache_read: String? = null
)

/**
 * Represents a model from the OpenRouter API models listing.
 *
 * @property id Unique identifier for the model.
 * @property name Display name of the model.
 * @property created Timestamp when the model was created.
 * @property description Detailed description of the model.
 * @property context_length Maximum token context length supported by the model.
 * @property architecture Information about the model architecture.
 * @property pricing Pricing information for the model.
 * @property top_provider Information about the top provider of the model.
 * @property per_request_limits Request limits information if any.
 */
@Serializable
data class OpenRouterModel(
    val id: String,
    val name: String,
    val created: Long? = null,
    val description: String? = null,
    val context_length: Int,
    val architecture: ModelArchitecture? = null,
    val pricing: ModelPricing,
    val top_provider: ModelTopProvider? = null,
    val per_request_limits: JsonElement? = null
)

/**
 * Represents architecture information for a model.
 *
 * @property modality Type of modality (e.g., "text->text").
 * @property input_modalities List of input modalities supported.
 * @property output_modalities List of output modalities supported.
 * @property tokenizer Tokenizer information.
 * @property instruct_type Instruction type for the model.
 */
@Serializable
data class ModelArchitecture(
    val modality: String? = null,
    val input_modalities: List<String> = emptyList(),
    val output_modalities: List<String> = emptyList(),
    val tokenizer: String? = null,
    val instruct_type: String? = null
)

/**
 * Represents information about a model's top provider.
 *
 * @property context_length Maximum context length supported by this provider.
 * @property max_completion_tokens Maximum completion tokens supported.
 * @property is_moderated Whether the model is moderated.
 */
@Serializable
data class ModelTopProvider(
    val context_length: Int? = null,
    val max_completion_tokens: Int? = null,
    val is_moderated: Boolean? = null
)

/**
 * Represents the response from the OpenRouter API's model listing endpoint.
 *
 * @property data List of available models.
 */
@Serializable
data class OpenRouterModelsResponse(
    val data: List<OpenRouterModel>
)