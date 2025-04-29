package com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the inner data structure for the credits response.
 *
 * @property total_credits Total available credits.
 * @property total_usage Total credits used.
 * @property credit_granted The amount of credit granted.
 * @property currency The currency of the credit.
 * @property user_id The user ID associated with the credit.
 */
@Serializable
data class CreditsData(
    val total_credits: Double? = null,
    val total_usage: Double? = null,
    val credit_granted: Double? = null,
    val currency: String? = null,
    val user_id: String? = null
)

/**
 * Represents the main response from the OpenRouter API's credits endpoint.
 * Contains the nested credit data.
 *
 * @property data The nested object containing credit details.
 */
@Serializable
data class OpenRouterCreditsResponse(
    val data: CreditsData
) {
    companion object {
        /**
         * Creates a credits response from a generic map returned by the API.
         *
         * @param map The map containing the credits information.
         * @return A parsed OpenRouterCreditsResponse object.
         */
        fun fromMap(map: Map<String, Any>): OpenRouterCreditsResponse {
            // Ensure the nested 'data' map exists and is a Map
            val dataMap = map["data"] as? Map<*, *> ?: emptyMap<Any, Any>()

            // Helper to safely extract values
            fun <T> getValue(key: String, default: T): T {
                @Suppress("UNCHECKED_CAST")
                return when (default) {
                    is Double -> (dataMap[key] as? Number)?.toDouble() as? T ?: default
                    is String -> dataMap[key] as? T ?: default
                    else -> dataMap[key] as? T ?: default
                }
            }

            return OpenRouterCreditsResponse(
                data = CreditsData(
                    total_credits = getValue("total_credits", 0.0),
                    total_usage = getValue("total_usage", 0.0),
                    credit_granted = getValue("credit_granted", 0.0),
                    currency = getValue("currency", "USD"), // Default currency if missing
                    user_id = getValue("user_id", "")       // Default user ID if missing
                )
            )
        }
    }
} 