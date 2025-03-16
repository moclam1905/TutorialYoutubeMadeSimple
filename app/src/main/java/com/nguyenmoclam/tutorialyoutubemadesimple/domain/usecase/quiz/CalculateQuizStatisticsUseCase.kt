package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz

import javax.inject.Inject

/**
 * Use case for calculating quiz statistics.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class CalculateQuizStatisticsUseCase @Inject constructor(
    private val checkQuizAnswerUseCase: CheckQuizAnswerUseCase
) {
    /**
     * Data class to hold quiz statistics results
     */
    data class QuizStatistics(
        val correctAnswersCount: Int,
        val incorrectAnswersCount: Int,
        val skippedAnswersCount: Int = 0,
        val completionTimeSeconds: Int,
        val correctQuestionIndices: List<Int>,
        val incorrectQuestionIndices: List<Int>,
        val skippedQuestionIndices: List<Int> = emptyList()
    )
    
    /**
     * Execute the use case to calculate quiz statistics.
     *
     * @param questions The list of quiz questions
     * @param answeredQuestions Map of question indices to answers
     * @param startTime The time when the quiz started (in milliseconds)
     * @param completionTime The time when the quiz was completed (in milliseconds)
     * @return QuizStatistics object containing the calculated statistics
     */
    operator fun invoke(
        questions: List<Any>,
        answeredQuestions: Map<Int, String>,
        startTime: Long,
        completionTime: Long
    ): QuizStatistics {
        val correctIndices = mutableListOf<Int>()
        val incorrectIndices = mutableListOf<Int>()
        val skippedIndices = mutableListOf<Int>()
        
        // Calculate correct and incorrect answers
        answeredQuestions.forEach { (index, answer) ->
            if (index < questions.size) {
                if (answer.isEmpty()) {
                    skippedIndices.add(index)
                } else {
                    val question = questions[index]
                    val isCorrect = checkQuizAnswerUseCase(question, answer)

                    if (isCorrect) {
                        correctIndices.add(index)
                    } else {
                        incorrectIndices.add(index)
                    }
                }
            }
        }
        
        // Calculate completion time in seconds
        val completionTimeSeconds = if (startTime > 0 && completionTime > 0) {
            ((completionTime - startTime) / 1000).toInt()
        } else {
            0
        }
        
        return QuizStatistics(
            correctAnswersCount = correctIndices.size,
            incorrectAnswersCount = incorrectIndices.size,
            completionTimeSeconds = completionTimeSeconds,
            correctQuestionIndices = correctIndices,
            incorrectQuestionIndices = incorrectIndices,
            skippedQuestionIndices = skippedIndices,
            skippedAnswersCount = skippedIndices.size
        )
    }
}