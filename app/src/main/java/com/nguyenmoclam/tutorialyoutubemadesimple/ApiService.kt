package com.nguyenmoclam.tutorialyoutubemadesimple

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

/**
 * Data classes representing YouTube API video snippet response
 * These classes map the JSON response from YouTube's API to Kotlin objects
 */

/**
 * Represents the top-level response from YouTube API
 * @property items List of video items returned by the API
 */
data class VideoResponse(val items: List<VideoItem>)

/**
 * Represents a single video item with its snippet information
 * @property snippet Contains detailed information about the video
 */
data class VideoItem(val snippet: Snippet)

/**
 * Contains core video metadata from YouTube
 * @property title The video's title
 * @property thumbnails Collection of thumbnail images at different resolutions
 * @property defaultAudioLanguage The default audio language of the video (nullable)
 */
data class Snippet(
    val title: String,
    val thumbnails: Thumbnails,
    val defaultAudioLanguage: String?
)

/**
 * Contains various thumbnail sizes for a video
 * Each property represents a different resolution thumbnail
 * @property default Lowest resolution (120x90)
 * @property medium Standard resolution (320x180)
 * @property high Higher resolution (480x360)
 * @property standard HD resolution (640x480)
 * @property maxres Maximum resolution (1280x720)
 */
data class Thumbnails(
    val default: Thumbnail?,
    val medium: Thumbnail?,
    val high: Thumbnail?,
    val standard: Thumbnail?,
    val maxres: Thumbnail?
)

/**
 * Represents a single thumbnail image
 * @property url Direct URL to the thumbnail image
 */
data class Thumbnail(val url: String)

/**
 * Represents a question extracted from video content
 * @property original The original question text
 * @property rephrased Simplified/rephrased version of the question
 * @property answer Generated answer to the question
 */
data class Question(
    val original: String,
    var rephrased: String = "",
    var answer: String = ""
)

/**
 * Represents a topic extracted from video content
 * @property title Original topic title
 * @property rephrased_title Simplified version of the topic title
 * @property questions List of questions related to this topic
 */
data class Topic(
    val title: String,
    val rephrased_title: String = "",
    val questions: List<Question>
)

/**
 * Data classes for OpenRouter API request/response handling
 */

/**
 * Represents a single message in the OpenRouter API conversation
 * @property role The role of the message sender ("user" or "assistant")
 * @property content The actual message content
 */
data class Message(val role: String, val content: String)

/**
 * Represents a single choice in the OpenRouter API response
 * @property message The message containing the AI's response
 */
data class Choice(val message: Message)

/**
 * Represents the complete response from OpenRouter API
 * @property choices List of response choices from the AI model
 */
data class OpenRouterResponse(val choices: List<Choice>)

/**
 * Represents a request to the OpenRouter API
 * @property model The AI model to use for completion
 * @property messages List of conversation messages for context
 */
data class OpenRouterRequest(val model: String, val messages: List<Message>)

/**
 * Retrofit interface for YouTube API endpoints
 * Defines the contract for making YouTube API requests
 */
interface YouTubeApi {
    /**
     * Fetches video information from YouTube API
     * @param videoId The unique identifier of the YouTube video
     * @param apiKey YouTube API key for authentication
     * @return VideoResponse containing video metadata
     */
    @GET("videos?part=snippet")
    suspend fun getVideoInfo(
        @Query("id") videoId: String,
        @Query("key") apiKey: String
    ): VideoResponse
}

/**
 * Retrofit interface for OpenRouter API endpoints
 * Defines the contract for making OpenRouter API requests
 */
interface OpenRouterApi {
    /**
     * Creates a completion request to OpenRouter API
     * @param authHeader Bearer token for authentication
     * @param request OpenRouterRequest containing model and messages
     * @return OpenRouterResponse containing AI-generated completions
     */
    @POST("chat/completions")
    suspend fun createCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: OpenRouterRequest
    ): OpenRouterResponse
}

/**
 * Singleton object for managing API service instances
 * Initializes and provides access to YouTube and OpenRouter API services
 * Uses Retrofit with Gson converter for JSON serialization/deserialization
 */
object ApiService {
    /** Base URL for YouTube API v3 */
    private const val YOUTUBE_API_BASE_URL = "https://www.googleapis.com/youtube/v3/"
    
    /** Base URL for OpenRouter API v1 */
    private const val OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1/"

    /**
     * Retrofit instance for YouTube API
     * Configured with Gson converter for JSON handling
     */
    private val youtubeRetrofit = Retrofit.Builder()
        .baseUrl(YOUTUBE_API_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * Retrofit instance for OpenRouter API
     * Configured with Gson converter for JSON handling
     */
    private val openRouterRetrofit = Retrofit.Builder()
        .baseUrl(OPENROUTER_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /** Lazily initialized YouTube API service instance */
    val youtubeApi: YouTubeApi = youtubeRetrofit.create(YouTubeApi::class.java)
    
    /** Lazily initialized OpenRouter API service instance */
    val openRouterApi: OpenRouterApi = openRouterRetrofit.create(OpenRouterApi::class.java)
}
