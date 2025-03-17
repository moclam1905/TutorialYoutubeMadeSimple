package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import javax.inject.Inject

/**
 * Use case for retrieving a quiz by its ID.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class GetQuizByIdUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    /**
     * Execute the use case to get a quiz by its ID.
     *
     * @param quizId The ID of the quiz to retrieve
     * @return The quiz if found, null otherwise
     */
    suspend operator fun invoke(quizId: Long): Quiz? {
        return quizRepository.getQuizById(quizId)
    }
}