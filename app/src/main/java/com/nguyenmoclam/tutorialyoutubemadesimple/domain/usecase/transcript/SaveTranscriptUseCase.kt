package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.transcript

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Transcript
import javax.inject.Inject

/**
 * Use case for saving a transcript to the database.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class SaveTranscriptUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    /**
     * Execute the use case to save a transcript to the database.
     *
     * @param transcript The transcript to save
     */
    suspend operator fun invoke(transcript: Transcript) {
        quizRepository.insertTranscript(transcript)
    }
}