package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.summary

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Summary
import javax.inject.Inject

/**
 * Use case for creating a quiz summary.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class CreateQuizSummaryUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    /**
     * Execute the use case to create a quiz summary.
     *
     * @param summary The summary to create
     */
    suspend operator fun invoke(summary: Summary) {
        quizRepository.insertSummary(summary)
    }
}