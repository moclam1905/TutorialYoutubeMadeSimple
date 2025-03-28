package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.transcript

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.TranscriptSegment
import javax.inject.Inject

class GetCurrentSegmentUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    suspend operator fun invoke(transcriptId: Long, currentTimeMillis: Long): TranscriptSegment? {
        return quizRepository.getCurrentSegment(transcriptId, currentTimeMillis)
    }

}