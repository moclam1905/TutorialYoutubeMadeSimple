package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.repository

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import javax.inject.Inject

/**
 * Use case for getting the QuizRepository instance.
 * This allows ViewModels to access repository methods without direct dependency on the repository.
 */
class GetQuizRepositoryUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    /**
     * Execute the use case to get the QuizRepository instance.
     *
     * @return The QuizRepository instance
     */
    operator fun invoke(): QuizRepository {
        return quizRepository
    }
}