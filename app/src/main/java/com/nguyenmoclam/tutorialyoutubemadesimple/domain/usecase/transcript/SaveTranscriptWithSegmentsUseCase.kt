package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.transcript

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Transcript
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.TranscriptSegment
import javax.inject.Inject

/**
 * Use case for saving a transcript with its segments to the database.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class SaveTranscriptWithSegmentsUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    /**
     * Execute the use case to save a transcript with its segments to the database.
     *
     * @param transcript The transcript to save
     * @param segments The list of transcript segments to save
     * @return The ID of the saved transcript
     */
    suspend operator fun invoke(transcript: Transcript, segments: List<TranscriptSegment>): Long {
        return quizRepository.saveTranscriptWithSegments(transcript, segments)
    }
}