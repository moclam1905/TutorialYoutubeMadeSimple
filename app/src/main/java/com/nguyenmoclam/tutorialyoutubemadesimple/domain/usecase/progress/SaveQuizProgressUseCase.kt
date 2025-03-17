package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.progress

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import javax.inject.Inject

/**
 * Use case for saving quiz progress.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class SaveQuizProgressUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    /**
     * Execute the use case to save quiz progress.
     *
     * @param quizId The ID of the quiz to save progress for
     * @param currentQuestionIndex The current question index
     * @param answeredQuestions Map of question indices to answers
     */
    suspend operator fun invoke(
        quizId: Long,
        currentQuestionIndex: Int,
        answeredQuestions: Map<Int, String>
    ) {
        quizRepository.saveQuizProgress(quizId, currentQuestionIndex, answeredQuestions)
    }
}