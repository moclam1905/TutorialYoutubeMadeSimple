package com.nguyenmoclam.tutorialyoutubemadesimple

import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.Result
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.OpenRouterModelsResponse
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.OpenRouterRequest
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.OpenRouterResponse
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.video.VideoResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
    ): Result<OpenRouterResponse>

    /**
     * Fetches the list of available models from OpenRouter.
     *
     * @param authHeader The Authorization header with the API key.
     * @return A Result containing the OpenRouterModelsResponse or an error.
     */
    @GET("models")
    suspend fun getAvailableModels(
        @Header("Authorization") authHeader: String
    ): Result<OpenRouterModelsResponse>

    /**
     * Fetches the user's credits information from OpenRouter.
     *
     * @param authHeader The Authorization header with the API key.
     * @return A Response containing the credits information.
     */
    @GET("credits")
    suspend fun getCredits(
        @Header("Authorization") authHeader: String
    ): Response<Map<String, Any>>
}

object ApiService {
    /** Base URL for OpenRouter API v1 */
    private const val OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1/"

    private val openRouterRetrofit = Retrofit.Builder()
        .baseUrl(OPENROUTER_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /** Lazily initialized OpenRouter API service instance */
    val openRouterApi: OpenRouterApi = openRouterRetrofit.create(OpenRouterApi::class.java)
}
