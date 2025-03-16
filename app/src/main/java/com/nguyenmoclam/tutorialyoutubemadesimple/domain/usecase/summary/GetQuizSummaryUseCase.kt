package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.summary

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Summary
import javax.inject.Inject

/**
 * Use case for retrieving a quiz summary.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class GetQuizSummaryUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    /**
     * Execute the use case to get a quiz summary.
     *
     * @param quizId The ID of the quiz to retrieve the summary for
     * @return The summary if found, null otherwise
     */
    suspend operator fun invoke(quizId: Long): Summary? {
        return quizRepository.getSummaryByQuizId(quizId)
    }
}