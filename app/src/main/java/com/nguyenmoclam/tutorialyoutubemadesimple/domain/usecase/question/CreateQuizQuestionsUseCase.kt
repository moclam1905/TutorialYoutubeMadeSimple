package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.question

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Question
import javax.inject.Inject

/**
 * Use case for creating quiz questions.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class CreateQuizQuestionsUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    /**
     * Execute the use case to create quiz questions.
     *
     * @param questions The questions to create
     */
    suspend operator fun invoke(questions: List<Question>) {
        quizRepository.insertQuestions(questions)
    }
}