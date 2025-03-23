package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz

import com.nguyenmoclam.tutorialyoutubemadesimple.YouTubeApi
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for fetching YouTube video metadata.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class FetchVideoMetadataUseCase @Inject constructor(
    private val youTubeApiService: YouTubeApi,
    private val networkUtils: NetworkUtils
) {
    /**
     * Data class to hold video metadata results
     */
    data class VideoMetadata(
        val title: String,
        val thumbnailUrl: String,
        val description: String,
        val error: String? = null
    )

    /**
     * Execute the use case to fetch video metadata.
     *
     * @param videoId The YouTube video ID
     * @param apiKey The YouTube API key
     * @param defaultTitle The default title to use if none is found
     * @return VideoMetadata containing the video information or error message
     */
    suspend operator fun invoke(
        videoId: String,
        apiKey: String,
        defaultTitle: String
    ): VideoMetadata = withContext(Dispatchers.IO) {
        try {
            val videoResponse = youTubeApiService.getVideoInfo(videoId, apiKey)
            val snippet = videoResponse.items.firstOrNull()?.snippet
            val title = snippet?.title ?: defaultTitle

            // Use NetworkUtils to determine the appropriate image quality
            val imageQuality = networkUtils.getRecommendedImageQuality()

            // Choose the URL of the image based on the recommended quality
            val thumbnailUrl = snippet?.thumbnails?.run {
                when (imageQuality) {
                    "low" -> default?.url ?: medium?.url ?: high?.url ?: maxres?.url
                    "medium" -> medium?.url ?: high?.url ?: maxres?.url ?: default?.url
                    else -> maxres?.url ?: high?.url ?: medium?.url ?: default?.url
                }
            } ?: ""
            val description = snippet?.description ?: "No description available"

            VideoMetadata(
                title = title,
                thumbnailUrl = thumbnailUrl,
                description = description
            )
        } catch (e: Exception) {
            VideoMetadata(
                title = defaultTitle,
                thumbnailUrl = "",
                description = "No description available",
                error = e.message ?: "Unknown error fetching video metadata"
            )
        }
    }
}