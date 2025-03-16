package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz

import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.MultipleChoiceQuestion
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.TrueFalseQuestion
import javax.inject.Inject

/**
 * Use case for checking if a quiz answer is correct.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class CheckQuizAnswerUseCase @Inject constructor() {
    /**
     * Execute the use case to check if an answer is correct for a given question.
     *
     * @param question The question object (MultipleChoiceQuestion or TrueFalseQuestion)
     * @param answer The user's answer
     * @return True if the answer is correct, false otherwise
     */
    operator fun invoke(question: Any, answer: String): Boolean {
        return when (question) {
            is MultipleChoiceQuestion -> {
                question.correctAnswers.contains(answer)
            }
            is TrueFalseQuestion -> {
                (answer == "True" && question.isTrue) ||
                (answer == "False" && !question.isTrue)
            }
            else -> false
        }
    }
}