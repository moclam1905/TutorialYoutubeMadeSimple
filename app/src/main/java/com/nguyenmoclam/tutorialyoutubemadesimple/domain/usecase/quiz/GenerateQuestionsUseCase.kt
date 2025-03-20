package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz

import com.nguyenmoclam.tutorialyoutubemadesimple.lib.LLMProcessor
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
     * Data class to hold question generation results
     */
    data class QuestionsResult(
        val content: String,
        val error: String? = null
    )
    
    /**
     * Execute the use case to generate questions from a transcript.
     *
     * @param transcriptContent The processed transcript content
     * @param language The language to generate questions in
     * @param questionType The type of questions to generate (multiple choice, true/false)
     * @param numberOfQuestions The number of questions to generate
     * @return QuestionsResult containing the generated questions JSON or error message
     */
    suspend operator fun invoke(
        transcriptContent: String,
        language: String,
        questionType: String,
        numberOfQuestions: Int
    ): QuestionsResult = withContext(Dispatchers.IO) {
        try {
            val keyPoints = llmProcessor.extractKeyPoints(transcriptContent, language)
            // Store the extracted key points for later retrieval
            lastExtractedKeyPoints = keyPoints
            
            val questionsJson = llmProcessor.generateQuestionsFromKeyPoints(
                keyPoints = keyPoints,
                language = language,
                questionType = questionType,
                numberOfQuestions = numberOfQuestions
            )
            QuestionsResult(content = questionsJson)
        } catch (e: Exception) {
            QuestionsResult(content = "", error = e.message ?: "Unknown error generating questions")
        }
    }
}