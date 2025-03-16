package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.progress

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import javax.inject.Inject

/**
 * Use case for deleting quiz progress.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class DeleteQuizProgressUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    /**
     * Execute the use case to delete quiz progress.
     *
     * @param quizId The ID of the quiz to delete progress for
     */
    suspend operator fun invoke(quizId: Long) {
        quizRepository.deleteProgressForQuiz(quizId)
    }
}