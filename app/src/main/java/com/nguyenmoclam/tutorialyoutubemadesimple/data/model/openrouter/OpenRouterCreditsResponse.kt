package com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the inner data structure for the credits response.
 * 
 * @property totalCredits Total available credits.
 * @property totalUsage Total credits used.
 * @property creditGranted The amount of credit granted.
 * @property currency The currency of the credit.
 * @property userId The user ID associated with the credit.
 */
@Serializable
data class CreditsData(
    @SerialName("total_credits") val totalCredits: Double,
    @SerialName("total_usage") val totalUsage: Double,
    @SerialName("credit_granted") val creditGranted: Double,
    @SerialName("currency") val currency: String,
    @SerialName("user_id") val userId: String
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
                    totalCredits = getValue("total_credits", 0.0),
                    totalUsage = getValue("total_usage", 0.0),
                    creditGranted = getValue("credit_granted", 0.0),
                    currency = getValue("currency", "USD"), // Default currency if missing
                    userId = getValue("user_id", "")       // Default user ID if missing
                )
            )
        }
    }
} 