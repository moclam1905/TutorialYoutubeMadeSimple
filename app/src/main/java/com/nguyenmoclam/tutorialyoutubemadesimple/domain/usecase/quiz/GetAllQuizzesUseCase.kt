package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving all quizzes.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class GetAllQuizzesUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    /**
     * Execute the use case to get all quizzes.
     *
     * @return Flow of all quizzes
     */
    operator fun invoke(): Flow<List<Quiz>> {
        return quizRepository.getAllQuizzes()
    }
}