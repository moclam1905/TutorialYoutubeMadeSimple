package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.transcript

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Transcript
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTranscriptForQuizUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    operator fun invoke(quizId: Long): Flow<Transcript?> {
        return quizRepository.getTranscriptForQuiz(quizId)
    }
}