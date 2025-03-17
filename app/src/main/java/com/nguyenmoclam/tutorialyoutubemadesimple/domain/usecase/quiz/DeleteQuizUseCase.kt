package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import javax.inject.Inject

/**
 * Use case for deleting a quiz.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class DeleteQuizUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    /**
     * Execute the use case to delete a quiz.
     *
     * @param quizId The ID of the quiz to delete
     */
    suspend operator fun invoke(quizId: Long) {
        quizRepository.deleteQuiz(quizId)
    }
}