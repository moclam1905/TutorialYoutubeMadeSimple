package com.nguyenmoclam.tutorialyoutubemadesimple

import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.OpenRouterRequest
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.OpenRouterResponse
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.video.VideoResponse
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
    ): OpenRouterResponse
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
