package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.progress

import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.QuizProgressEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving quiz progress.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class GetQuizProgressUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    /**
     * Execute the use case to get quiz progress.
     *
     * @param quizId The ID of the quiz to retrieve progress for
     * @return Flow of quiz progress as a map of question indices to answers
     */
    operator fun invoke(quizId: Long): Flow<Map<Int, String>?> {
        return quizRepository.getProgressForQuizAsFlow(quizId)
    }
    
    /**
     * Execute the use case to get quiz progress as a suspend function.
     *
     * @param quizId The ID of the quiz to retrieve progress for
     * @return Quiz progress as a map of question indices to answers
     */
    suspend fun getProgressEntity(quizId: Long): QuizProgressEntity? {
        return quizRepository.getQuizProgressEntity(quizId)
    }
}