package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.transcript

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Transcript
import javax.inject.Inject

/**
 * Use case for retrieving a transcript from the database.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class GetTranscriptUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    /**
     * Execute the use case to get a transcript from the database.
     *
     * @param quizId The ID of the quiz to get the transcript for
     * @return The transcript, or null if not found
     */
    suspend operator fun invoke(quizId: Long): Transcript? {
        return quizRepository.getTranscriptByQuizId(quizId)
    }
}