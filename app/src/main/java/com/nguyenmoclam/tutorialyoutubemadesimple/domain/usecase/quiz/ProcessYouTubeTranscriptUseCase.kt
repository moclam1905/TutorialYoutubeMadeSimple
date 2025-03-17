package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz

import com.nguyenmoclam.tutorialyoutubemadesimple.lib.YouTubeTranscriptLight
import javax.inject.Inject

/**
 * Use case for processing YouTube video transcripts.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class ProcessYouTubeTranscriptUseCase @Inject constructor(
    private val youTubeTranscriptLight: YouTubeTranscriptLight
) {
    /**
     * Data class to hold transcript processing results
     */
    data class TranscriptResult(
        val text: String,
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
        languages: List<String> = listOf("en")
    ): TranscriptResult {
        return try {
            val transcripts = youTubeTranscriptLight.getTranscript(videoId, languages)
            val transcriptContent = transcripts.joinToString(" ") { it.text }
            TranscriptResult(text = transcriptContent)
        } catch (e: YouTubeTranscriptLight.TranscriptError) {
            TranscriptResult(text = "", error = e.javaClass.simpleName)
        } catch (e: Exception) {
            TranscriptResult(text = "", error = e.message ?: "Unknown error")
        }
    }
}