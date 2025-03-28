package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.transcript

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.TranscriptSegment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChaptersForTranscriptUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    operator fun invoke(transcriptId: Long): Flow<List<TranscriptSegment>> {
        return quizRepository.getChaptersForTranscript(transcriptId)
    }
}
