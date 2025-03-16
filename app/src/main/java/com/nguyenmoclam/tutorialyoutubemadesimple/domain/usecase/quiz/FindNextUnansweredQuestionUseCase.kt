package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz

import javax.inject.Inject

/**
 * Use case for finding the next unanswered and unskipped question in a quiz.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class FindNextUnansweredQuestionUseCase @Inject constructor() {
    /**
     * Execute the use case to find the next unanswered question.
     *
     * @param startIndex The index to start searching from
     * @param questions The list of quiz questions
     * @param answeredQuestions Map of question indices to answers
     * @param skippedQuestions Set of indices of skipped questions
     * @return The index of the next unanswered question, or 0 if all questions are answered or skipped
     */
    operator fun invoke(
        startIndex: Int,
        questions: List<Any>,
        answeredQuestions: Map<Int, String>,
        skippedQuestions: Set<Int>
    ): Int {
        if (questions.isEmpty()) return 0
        
        // Loop through questions starting from startIndex
        for (i in 0 until questions.size) {
            val index = (startIndex + i) % questions.size
            if (!answeredQuestions.containsKey(index) && !skippedQuestions.contains(index)) {
                return index
            }
        }
        
        // If all questions are answered or skipped, return the first question
        return 0
    }
}