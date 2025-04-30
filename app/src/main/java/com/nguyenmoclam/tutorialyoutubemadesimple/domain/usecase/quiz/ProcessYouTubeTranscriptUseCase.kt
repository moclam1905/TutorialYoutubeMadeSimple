package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import com.nguyenmoclam.tutorialyoutubemadesimple.auth.AuthManager
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.YouTubeTranscriptLight
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for processing YouTube video transcripts.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class ProcessYouTubeTranscriptUseCase @Inject constructor(
    private val youTubeTranscriptLight: YouTubeTranscriptLight,
    private val authManager: AuthManager,
    private val networkUtils: NetworkUtils,
    @ApplicationContext private val context: Context
) {
    /**
     * Data class to hold transcript processing results
     */
    data class TranscriptResult(
        val text: String,
        val segments: List<YouTubeTranscriptLight.Transcript> = emptyList(),
        val chapters: List<YouTubeTranscriptLight.Chapter> = emptyList(),
        val error: String? = null
    )

    /**
     * Execute the use case to process a YouTube video transcript.
     *
     * @param videoId The YouTube video ID
     * @param languages List of language codes to try, in order of preference
     * @return TranscriptResult containing the processed transcript text or error message
     */
    suspend operator fun invoke(
        videoId: String,
        languages: List<String> = listOf("en"),
        transcriptMode: String
    ): TranscriptResult {
        return try {
            // Check if content should not be loaded based on data saver settings
            if (!networkUtils.shouldLoadContent()) {
                return TranscriptResult(
                    text = "",
                    segments = emptyList(),
                    error = "Network restricted by data saver settings"
                )
            }

            if (authManager.isUserSignedIn() && transcriptMode == "google") {
                try {
                    // Apply connection timeout to Google API request
                    val transcriptText = networkUtils.withConnectionTimeout {
                        fetchTranscriptFromGoogleApi(videoId)
                    }
                    
                    // For Google API, we don't have segments with timestamps yet
                    // This would need to be implemented separately
                    TranscriptResult(text = transcriptText, segments = emptyList())
                } catch (e: Exception) {
                    TranscriptResult(
                        text = "",
                        segments = emptyList(),
                        error = e.message ?: "Connection timeout or error"
                    )
                }
            } else {
                // YouTubeTranscriptLight already uses the timeout settings via OkHttpClient
                val transcripts = youTubeTranscriptLight.getTranscript(videoId, languages)
                val transcriptContent = transcripts.joinToString(" ") { it.text }

                val chapterList = youTubeTranscriptLight.getChapters(videoId)
                Log.d("ProcessYouTubeTranscriptUseCase", "Chapters: $chapterList")

                TranscriptResult(text = transcriptContent, segments = transcripts, chapters = chapterList)
            }
        } catch (e: YouTubeTranscriptLight.TranscriptError) {
            TranscriptResult(text = "", segments = emptyList(), error = e.javaClass.simpleName)
        } catch (e: Exception) {
            TranscriptResult(text = "", segments = emptyList(), error = e.message ?: "Unknown error")
        }
    }

    private suspend fun fetchTranscriptFromGoogleApi(videoId: String): String =
        withContext(Dispatchers.IO) {
            val account: GoogleSignInAccount = authManager.getCurrentAccount()
                ?: throw IllegalStateException("No Google account found")

            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                listOf(
                    "https://www.googleapis.com/auth/youtube.readonly",
                    "https://www.googleapis.com/auth/youtube.force-ssl",
                    "https://www.googleapis.com/auth/youtubepartner"
                )
            ).apply {
                selectedAccount = account.account
            }

            val youtube = YouTube.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            ).setApplicationName("TutorialYoutubeMadeSimple")
                .build()

            val captionListResponse = youtube.captions()
                .list("snippet", videoId)
                .execute()
            val captions = captionListResponse.items

            if (captions.isNullOrEmpty()) {
                throw YouTubeTranscriptLight.TranscriptError.NoTranscriptAvailable()
            }

            val desiredLanguageCode = "en"
            val filteredCaptions = captions.filter { caption ->
                (caption.snippet.language == desiredLanguageCode) && (caption.snippet.isAutoSynced == false)
            }

            val chosenCaption = filteredCaptions.firstOrNull() ?: captions.first()
            // error : "The permissions associated with the request are not sufficient to download the caption track.
            // The request might not be properly authorized,
            // or the video order might not have enabled third-party contributions for this caption."
            val captionDownload = youtube.captions()
                .download(chosenCaption.id)
                .executeMediaAsInputStream()

            captionDownload.bufferedReader().use { it.readText() }
        }

}