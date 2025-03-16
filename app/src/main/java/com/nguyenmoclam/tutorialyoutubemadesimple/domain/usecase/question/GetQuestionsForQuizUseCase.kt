package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.question

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Question
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving questions for a quiz.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class GetQuestionsForQuizUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    /**
     * Execute the use case to get questions for a quiz.
     *
     * @param quizId The ID of the quiz to retrieve questions for
     * @return Flow of questions for the quiz
     */
    operator fun invoke(quizId: Long): Flow<List<Question>> {
        return quizRepository.getQuestionsForQuiz(quizId)
    }
}