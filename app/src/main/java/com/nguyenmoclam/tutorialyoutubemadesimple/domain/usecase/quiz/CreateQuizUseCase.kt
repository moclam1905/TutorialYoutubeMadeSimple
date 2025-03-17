package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import javax.inject.Inject

/**
 * Use case for creating a new quiz.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class CreateQuizUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    /**
     * Execute the use case to create a new quiz.
     *
     * @param quiz The quiz to create
     * @return The ID of the created quiz
     */
    suspend operator fun invoke(quiz: Quiz): Long {
        return quizRepository.insertQuiz(quiz)
    }
}