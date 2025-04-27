package com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter

/**
 * Represents the response from the OpenRouter API's credits endpoint.
 * 
 * @property credits The user's available credit balance.
 * @property creditGranted Total credit that has been granted to the user.
 * @property creditUsed Credit that has been used.
 * @property currency The currency code for the credits.
 * @property userId The user's ID in the OpenRouter system.
 */
data class OpenRouterCreditsResponse(
    val credits: Double,
    val creditGranted: Double,
    val creditUsed: Double,
    val currency: String,
    val userId: String
) {
    companion object {
        /**
         * Creates a credits response from a generic map returned by the API.
         * 
         * @param map The map containing the credits information.
         * @return A parsed OpenRouterCreditsResponse object.
         */
        fun fromMap(map: Map<String, Any>): OpenRouterCreditsResponse {
            return OpenRouterCreditsResponse(
                credits = (map["credits"] as? Number)?.toDouble() ?: 0.0,
                creditGranted = (map["creditGranted"] as? Number)?.toDouble() ?: 0.0,
                creditUsed = (map["creditUsed"] as? Number)?.toDouble() ?: 0.0,
                currency = (map["currency"] as? String) ?: "USD",
                userId = (map["userId"] as? String) ?: ""
            )
        }
    }
} 