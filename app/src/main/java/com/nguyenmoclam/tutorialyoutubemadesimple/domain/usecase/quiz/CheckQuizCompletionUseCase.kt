package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz

import javax.inject.Inject

/**
 * Use case for checking if a quiz is completed.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class CheckQuizCompletionUseCase @Inject constructor() {
    /**
     * Data class to hold quiz completion check results
     */
    data class QuizCompletionResult(
        val isCompleted: Boolean,
        val completionTime: Long
    )
    
    /**
     * Execute the use case to check if a quiz is completed.
     *
     * @param questions The list of quiz questions
     * @param answeredQuestions Map of question indices to answers
     * @param skippedQuestions Set of indices of skipped questions
     * @param currentQuestionIndex The current question index
     * @param isCurrentlyCompleted Whether the quiz is currently marked as completed
     * @param startTime The time when the quiz started (in milliseconds)
     * @return QuizCompletionResult object containing completion status and time
     */
    operator fun invoke(
        questions: List<Any>,
        answeredQuestions: Map<Int, String>,
        skippedQuestions: Set<Int>,
        currentQuestionIndex: Int,
        isCurrentlyCompleted: Boolean,
        startTime: Long
    ): QuizCompletionResult {
        // Only proceed if we have questions
        if (questions.isEmpty()) {
            return QuizCompletionResult(isCompleted = false, completionTime = 0)
        }
        
        // Check if all questions are now answered or skipped
        val allQuestionsHandled = questions.indices.all { index ->
            answeredQuestions.containsKey(index) || skippedQuestions.contains(index)
        }
        
        // Only mark as completed if all questions are handled and we're on the last question
        val isLastQuestion = currentQuestionIndex == questions.size - 1
        
        val isCompleted = if (allQuestionsHandled && isLastQuestion && !isCurrentlyCompleted) {
            true
        } else {
            isCurrentlyCompleted
        }
        
        // Calculate completion time if quiz is newly completed
        val completionTime = if (isCompleted && !isCurrentlyCompleted) {
            // Ensure we have a valid startTime before setting completion time
            val currentStartTime = if (startTime == 0L) {
                // If startTime wasn't set, use a reasonable default (current time minus 5 minutes)
                System.currentTimeMillis() - (5 * 60 * 1000)
            } else {
                startTime
            }
            
            System.currentTimeMillis()
        } else {
            0L
        }
        
        return QuizCompletionResult(isCompleted = isCompleted, completionTime = completionTime)
    }
}