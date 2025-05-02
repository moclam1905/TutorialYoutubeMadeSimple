package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz

import com.nguyenmoclam.tutorialyoutubemadesimple.lib.LLMProcessor
// import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils // Remove NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for generating quiz questions from a YouTube video transcript.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class GenerateQuestionsUseCase @Inject constructor(
    private val llmProcessor: LLMProcessor
) {
    // Store the last extracted key points for later retrieval
    private var lastExtractedKeyPoints: List<String> = emptyList()

    /**
     * Get the last extracted key points from the most recent question generation
     */
    fun getLastExtractedKeyPoints(): List<String> = lastExtractedKeyPoints

    /**
     * Data class to hold question generation results including free call usage
     */
    data class QuestionsResult(
        val content: String,
        val wasFreeCallUsed: Boolean = false, // Add boolean flag
        val error: String? = null
    )

    /**
     * Execute the use case to generate questions from a transcript.
     *
     * @param transcriptContent The processed transcript content
     * @param language The language to generate questions in
     * @param questionType The type of questions to generate (multiple choice, true/false)
     * @param numberOfQuestions The number of questions to generate
     * @return QuestionsResult containing the generated questions JSON, free call usage flag, or error message
     */
    suspend operator fun invoke(
        transcriptContent: String,
        language: String,
        questionType: String,
        numberOfQuestions: Int
    ): QuestionsResult = withContext(Dispatchers.IO) {
        var finalWasFreeCallUsed = false // Track if any step used a free call
        try {
            // Extract key points
            val (keyPoints, keyPointsUsedFreeCall) = llmProcessor.extractKeyPoints(transcriptContent, language)
            lastExtractedKeyPoints = keyPoints
            if (keyPointsUsedFreeCall) finalWasFreeCallUsed = true

            if (keyPoints.isEmpty()) {
                // Return error or empty result depending on requirements
                return@withContext QuestionsResult(content = "", error = "Could not extract key points.", wasFreeCallUsed = finalWasFreeCallUsed)
            }

            // Generate questions
            val (questionsJson, questionsUsedFreeCall) = llmProcessor.generateQuestionsFromKeyPoints(
                keyPoints = keyPoints,
                language = language,
                questionType = questionType,
                numberOfQuestions = numberOfQuestions
            )
            if (questionsUsedFreeCall) finalWasFreeCallUsed = true

            QuestionsResult(content = questionsJson, wasFreeCallUsed = finalWasFreeCallUsed)

        } catch (e: Exception) {
            // Ensure wasFreeCallUsed is false on error? Or keep its value if keypoints succeeded?
            // Let's reset to false on error for simplicity.
            QuestionsResult(content = "", error = e.message ?: "Unknown error generating questions", wasFreeCallUsed = false)
        }
    }
}
