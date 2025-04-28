package com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    @SerialName("finish_reason") val finishReason: String? = null,
    @SerialName("native_finish_reason") val nativeFinishReason: String? = null,
    val index: Int? = null
)

@Serializable
data class PromptTokensDetails(
    @SerialName("cached_tokens") val cachedTokens: Int? = null
)

@Serializable
data class CompletionTokensDetails(
    @SerialName("reasoning_tokens") val reasoningTokens: Int? = null
)

@Serializable
data class UsageInfo(
    @SerialName("prompt_tokens") val promptTokens: Int? = null,
    @SerialName("completion_tokens") val completionTokens: Int? = null,
    @SerialName("total_tokens") val totalTokens: Int? = null,
    @SerialName("prompt_tokens_details") val promptTokensDetails: PromptTokensDetails? = null,
    @SerialName("completion_tokens_details") val completionTokensDetails: CompletionTokensDetails? = null
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
    val prompt: Double,
    val completion: Double,
    val request: String? = null,
    val image: String? = null,
    @SerialName("web_search") val webSearch: String? = null,
    @SerialName("internal_reasoning") val internalReasoning: String? = null
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
    @SerialName("context_length") val contextLength: Int,
    val architecture: ModelArchitecture? = null,
    val pricing: ModelPricing,
    @SerialName("top_provider") val topProvider: ModelTopProvider? = null,
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
    @SerialName("input_modalities") val inputModalities: List<String> = emptyList(),
    @SerialName("output_modalities") val outputModalities: List<String> = emptyList(),
    val tokenizer: String? = null,
    @SerialName("instruct_type") val instructType: String? = null
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
    @SerialName("context_length") val contextLength: Int? = null,
    @SerialName("max_completion_tokens") val maxCompletionTokens: Int? = null,
    @SerialName("is_moderated") val isModerated: Boolean? = null
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