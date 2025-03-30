package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz

import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.content.Topic
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.HtmlGenerator
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.LLMProcessor
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.Section
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for generating a quiz summary from a YouTube video transcript.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class GenerateQuizSummaryUseCase @Inject constructor(
    private val llmProcessor: LLMProcessor,
    private val networkUtils: NetworkUtils
) {
    // Store the last processed topics for later retrieval
    private var lastProcessedTopics: List<Topic> = emptyList()

    /**
     * Get the last processed topics from the most recent summary generation
     */
    fun getLastProcessedTopics(): List<Topic> = lastProcessedTopics

    /**
     * Data class to hold summary generation results
     */
    data class SummaryResult(
        val content: String,
        val error: String? = null
    )

    /**
     * Execute the use case to generate a summary from a transcript.
     *
     * @param title The video title
     * @param thumbnailUrl The video thumbnail URL
     * @param transcriptContent The processed transcript content
     * @return SummaryResult containing the generated HTML summary or error message
     */
    suspend operator fun invoke(
        title: String,
        thumbnailUrl: String,
        transcriptContent: String,
        language: String = "English"
    ): SummaryResult = withContext(Dispatchers.IO) {
        try {
            val topics = llmProcessor.extractTopicsAndQuestions(transcriptContent, title, language)
                .takeIf { it.isNotEmpty() }
                ?: return@withContext SummaryResult(
                    content = "",
                    error = "No topics could be extracted"
                )

            val processedTopics = llmProcessor.processContent(topics, transcriptContent, language)
            // Store the processed topics for later retrieval
            lastProcessedTopics = processedTopics
            val sections = processedTopics.map { topic ->
                Section(
                    title = topic.rephrased_title.ifEmpty { topic.title },
                    bullets = topic.questions.map { q ->
                        Pair(q.rephrased.ifEmpty { q.original }, q.answer)
                    }
                )
            }

            // Use NetworkUtils to determine the appropriate image quality
            val imageQuality = networkUtils.getRecommendedImageQuality()

            // Adjust the image URL based on the recommended quality
            val optimizedImageUrl = when (imageQuality) {
                "low" -> thumbnailUrl.replace("maxresdefault", "mqdefault")
                "medium" -> thumbnailUrl.replace("maxresdefault", "hqdefault")
                else -> thumbnailUrl // Use the original URL for high quality
            }

            val htmlContent = HtmlGenerator.generate(
                title = title,
                imageUrl = optimizedImageUrl,
                sections = sections
            )

            SummaryResult(content = htmlContent)
        } catch (e: Exception) {
            SummaryResult(content = "", error = e.message ?: "Unknown error generating summary")
        }
    }
}