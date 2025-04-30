package com.nguyenmoclam.tutorialyoutubemadesimple

import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.OpenRouterCreditsResponse
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.OpenRouterModelsResponse
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.OpenRouterRequest
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.OpenRouterResponse
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.video.VideoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface YouTubeApi {
    @GET("videos?part=snippet")
    suspend fun getVideoInfo(
        @Query("id") videoId: String,
        @Query("key") apiKey: String
    ): VideoResponse
}

interface OpenRouterApi {
    @POST("chat/completions")
    suspend fun createCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: OpenRouterRequest
    ): Response<OpenRouterResponse>

    /**
     * Fetches the list of available models from OpenRouter.
     *
     * @param authHeader The Authorization header with the API key.
     * @return A Response containing the OpenRouterModelsResponse or an error.
     */
    @GET("models")
        suspend fun getAvailableModels(
        @Header("Authorization") authHeader: String
    ): Response<OpenRouterModelsResponse>

    /**
     * Fetches the user's credits information from OpenRouter.
     *
     * @param authHeader The Authorization header with the API key.
     * @return A Response containing the credits information.
     */
    @GET("credits")
    suspend fun getCredits(
        @Header("Authorization") authHeader: String
    ): Response<OpenRouterCreditsResponse>
}

