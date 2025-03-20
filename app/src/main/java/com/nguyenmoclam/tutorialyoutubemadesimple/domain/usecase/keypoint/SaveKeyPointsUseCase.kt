package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.keypoint

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for saving key points extracted from a video transcript to the database.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class SaveKeyPointsUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    /**
     * Execute the use case to save key points to the database.
     *
     * @param keyPoints The list of key points to save
     * @param quizId The ID of the quiz these key points belong to
     */
    suspend operator fun invoke(keyPoints: List<String>, quizId: Long) = withContext(Dispatchers.IO) {
        quizRepository.insertKeyPoints(keyPoints, quizId)
    }
}