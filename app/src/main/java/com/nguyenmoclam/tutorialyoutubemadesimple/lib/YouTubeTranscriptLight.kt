package com.nguyenmoclam.tutorialyoutubemadesimple.lib

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.regex.Pattern
import org.apache.commons.text.StringEscapeUtils

/**
 * A lightweight class for fetching and parsing YouTube video transcripts.
 * This class provides functionality to:
 * - Fetch video transcripts from YouTube
 * - Parse and clean transcript text
 * - Handle various error cases
 */
class YouTubeTranscriptLight {
    private val client = OkHttpClient()
    private val baseUrl = "https://www.youtube.com"

    /**
     * Data class representing a single transcript segment
     * @property text The actual transcript text
     * @property start The start time of the segment in seconds
     * @property duration The duration of the segment in seconds
     */
    data class Transcript(
        val text: String,
        val start: Double,
        val duration: Double
    )

    /**
     * Sealed class representing various errors that can occur during transcript fetching
     * This helps in providing specific error handling for different scenarios
     */
    sealed class TranscriptError : Exception() {
        class VideoNotFound :
            TranscriptError()           // Video ID is invalid or video doesn't exist

        class TranscriptsDisabled : TranscriptError()     // Transcripts are disabled for the video
        class NoTranscriptAvailable : TranscriptError()   // No transcripts available for the video
        class NetworkError : TranscriptError() // Network-related errors
        class LanguageNotFound : TranscriptError()        // Requested language not available
    }

    /**
     * Main function to fetch and parse video transcripts
     *
     * @param videoId The YouTube video ID or URL
     * @param languages List of language codes to try, in order of preference (e.g., ["en", "es"])
     * @return List of [Transcript] objects containing the video transcript
     * @throws TranscriptError Various transcript-related errors
     */
    suspend fun getTranscript(
        videoId: String,
        languages: List<String> = listOf("en") // currently only supports English
    ): List<Transcript> = withContext(Dispatchers.IO) {
        try {
            val html = fetchVideoPage(videoId)
            val captionsJson = extractCaptionsJson(html, videoId)
            val transcriptUrl = findTranscriptUrl(captionsJson, languages)
            fetchTranscript(transcriptUrl)
        } catch (e: Exception) {
            when (e) {
                is TranscriptError -> throw e
                else -> throw TranscriptError.NetworkError()
            }
        }
    }

    /**
     * Fetches the YouTube video page HTML
     *
     * @param videoId The YouTube video ID
     * @return The raw HTML content of the video page
     * @throws TranscriptError.VideoNotFound if the video doesn't exist or can't be accessed
     */
    private fun fetchVideoPage(videoId: String): String {
        val request = Request.Builder()
            .url("$baseUrl/watch?v=$videoId")
            .header("Accept-Language", "en-US")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw TranscriptError.VideoNotFound()
        return response.body?.string() ?: throw TranscriptError.VideoNotFound()
    }

    /**
     * Extracts and parses the captions JSON data from the video page HTML
     *
     * This function performs several checks:
     * 1. Verifies if captions section exists in the HTML
     * 2. Validates video ID format
     * 3. Checks for reCAPTCHA presence
     * 4. Verifies video playability
     *
     * @param html The raw HTML content of the video page
     * @param videoId The YouTube video ID for validation
     * @return JSONObject containing the captions data
     * @throws Various TranscriptError types based on the validation results
     */
    private fun extractCaptionsJson(html: String, videoId: String): JSONObject {
        // Split HTML at captions section
        val splittedHtml = html.split("\"captions\":")

        if (splittedHtml.size <= 1) {
            // Perform various checks to determine the exact error
            if (videoId.startsWith("http://") || videoId.startsWith("https://")) {
                throw TranscriptError.VideoNotFound()
            }
            if (html.contains("class=\"g-recaptcha\"")) {
                throw TranscriptError.TranscriptsDisabled()
            }
            if (!html.contains("\"playabilityStatus\":")) {
                throw TranscriptError.VideoNotFound()
            }
            throw TranscriptError.TranscriptsDisabled()
        }

        // Extract and parse the captions JSON data
        val captionsJson = JSONObject(
            splittedHtml[1].split(",\"videoDetails\"")[0].replace("\n", "")
        ).optJSONObject("playerCaptionsTracklistRenderer")
            ?: throw TranscriptError.TranscriptsDisabled()

        if (!captionsJson.has("captionTracks")) {
            throw TranscriptError.NoTranscriptAvailable()
        }

        return captionsJson
    }

    /**
     * Finds the transcript URL for the requested language
     *
     * This function:
     * 1. Verifies caption tracks exist
     * 2. Iterates through available languages
     * 3. Returns the URL for the first matching language
     *
     * @param captionsJson The parsed captions JSON object
     * @param languages List of preferred language codes
     * @return URL string for the transcript in the first matching language
     * @throws TranscriptError.NoTranscriptAvailable if no caption tracks exist
     * @throws TranscriptError.LanguageNotFound if none of the requested languages are available
     */
    private fun findTranscriptUrl(captionsJson: JSONObject, languages: List<String>): String {
        if (!captionsJson.has("captionTracks")) {
            throw TranscriptError.NoTranscriptAvailable()
        }

        val captionTracks = captionsJson.getJSONArray("captionTracks")
        for (language in languages) {
            for (i in 0 until captionTracks.length()) {
                val track = captionTracks.getJSONObject(i)
                if (track.getString("languageCode") == language) {
                    return track.getString("baseUrl")
                }
            }
        }

        throw TranscriptError.LanguageNotFound()
    }

    /**
     * Fetches and parses the transcript XML from the provided URL
     *
     * @param url The URL to fetch the transcript XML from
     * @return List of parsed Transcript objects
     * @throws TranscriptError.NetworkError if the request fails or returns empty
     */
    private fun fetchTranscript(url: String): List<Transcript> {
        val request = Request.Builder()
            .url(url)
            .header("Accept-Language", "en-US")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw TranscriptError.NetworkError()

        val xmlContent = response.body?.string() ?: throw TranscriptError.NetworkError()
        return parseTranscriptXml(xmlContent)
    }

    /**
     * Parses the transcript XML content into a list of Transcript objects
     *
     * This function:
     * 1. Uses regex to extract transcript segments
     * 2. Parses timing information
     * 3. Cleans and formats the text content
     *
     * The text cleaning process includes:
     * - HTML entity decoding
     * - Newline normalization
     * - HTML tag removal
     * - Special character replacement
     * - Whitespace trimming
     *
     * @param xml The raw XML content containing the transcript
     * @return List of parsed and cleaned Transcript objects
     */
    private fun parseTranscriptXml(xml: String): List<Transcript> {
        val transcripts = mutableListOf<Transcript>()
        // Regex pattern to extract transcript segments with timing information
        val pattern = Pattern.compile(
            """<text start="([\d.]+)" dur="([\d.]+)"[^>]*>(.*?)</text>""",
            Pattern.DOTALL
        )
        val matcher = pattern.matcher(xml)

        while (matcher.find()) {
            val start = matcher.group(1)?.toDouble() ?: 0.0
            val duration = matcher.group(2)?.toDouble() ?: 0.0
            val text = matcher.group(3)?.let { rawText ->
                StringEscapeUtils.unescapeHtml4(rawText)  // Decode HTML entities
                    .replace("\\n", " ")                // Normalize newlines
                    .replace("<[^>]*>", "")            // Remove HTML tags
                    .replace("&#39;", "'")              // Replace special characters
                    .replace("&quot;", "\"")           // Replace quotes
                    .replace("&amp;", "&")             // Replace ampersand
                    .trim()                             // Remove excess whitespace
            } ?: ""
            transcripts.add(Transcript(text, start, duration))
        }

        return transcripts
    }

    companion object {
        /**
         * Factory method to create a new instance of YouTubeTranscriptLight
         * @return A new YouTubeTranscriptLight instance
         */
        @JvmStatic
        fun create(): YouTubeTranscriptLight =
            YouTubeTranscriptLight()
    }
}