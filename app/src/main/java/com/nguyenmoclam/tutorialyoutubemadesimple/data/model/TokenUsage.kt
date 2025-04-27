package com.nguyenmoclam.tutorialyoutubemadesimple.data.model

import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.OpenRouterCreditsResponse
import java.util.Date

/**
 * Represents token usage data for API interactions.
 * Used to track and display usage statistics and costs.
 * 
 * @property modelId The ID of the model used.
 * @property modelName The human-readable name of the model.
 * @property promptTokens Number of tokens used in the prompt.
 * @property completionTokens Number of tokens used in the completion.
 * @property totalTokens Total tokens used in the interaction.
 * @property estimatedCost Estimated cost of the interaction.
 * @property timestamp When the interaction occurred.
 */
data class TokenUsage(
    val modelId: String,
    val modelName: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val estimatedCost: Double,
    val timestamp: Date = Date()
)

/**
 * Represents a summary of token usage across models.
 * Used for displaying aggregated usage data.
 * 
 * @property totalPromptTokens Total tokens used in prompts.
 * @property totalCompletionTokens Total tokens used in completions.
 * @property totalTokens Total tokens used overall.
 * @property totalCost Total estimated cost.
 * @property usageByModel Usage broken down by model.
 */
data class TokenUsageSummary(
    val totalPromptTokens: Int,
    val totalCompletionTokens: Int,
    val totalTokens: Int,
    val totalCost: Double,
    val usageByModel: Map<String, ModelUsage>
) {
    /**
     * Represents usage statistics for a specific model.
     */
    data class ModelUsage(
        val modelId: String,
        val modelName: String,
        val promptTokens: Int,
        val completionTokens: Int,
        val totalTokens: Int,
        val estimatedCost: Double
    )
}

/**
 * Represents a user's credit balance with added status information.
 * 
 * @property credits Current credit balance.
 * @property creditGranted Total credit granted.
 * @property creditUsed Credit amount used.
 * @property currency Currency code.
 * @property userId User ID.
 * @property status Status of the credit balance (OK, LOW, CRITICAL).
 * @property lastUpdated When the balance was last updated.
 */
data class CreditStatus(
    val credits: Double,
    val creditGranted: Double,
    val creditUsed: Double,
    val currency: String,
    val userId: String,
    val status: BalanceStatus,
    val lastUpdated: Date = Date()
) {
    enum class BalanceStatus {
        OK,        // Normal balance
        LOW,       // Running low (below LOW_BALANCE_THRESHOLD)
        CRITICAL   // Very low balance (below CRITICAL_BALANCE_THRESHOLD)
    }
    
    companion object {
        const val LOW_BALANCE_THRESHOLD = 5.0
        const val CRITICAL_BALANCE_THRESHOLD = 1.0
        
        /**
         * Creates a CreditStatus from an OpenRouterCreditsResponse.
         */
        fun fromCreditsResponse(response: OpenRouterCreditsResponse): CreditStatus {
            val status = when {
                response.credits <= CRITICAL_BALANCE_THRESHOLD -> BalanceStatus.CRITICAL
                response.credits <= LOW_BALANCE_THRESHOLD -> BalanceStatus.LOW
                else -> BalanceStatus.OK
            }
            
            return CreditStatus(
                credits = response.credits,
                creditGranted = response.creditGranted,
                creditUsed = response.creditUsed,
                currency = response.currency,
                userId = response.userId,
                status = status
            )
        }
    }
} 