package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz

import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.QuizProgressDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.QuizStats
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for retrieving quiz statistics.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class GetQuizStatsUseCase @Inject constructor(
    private val quizRepository: QuizRepository,
    private val quizProgressDao: QuizProgressDao,
    private val checkQuizAnswerUseCase: CheckQuizAnswerUseCase
) {
    /**
     * Execute the use case to get quiz statistics.
     *
     * @param quizId The ID of the quiz to retrieve statistics for
     * @return QuizStats containing completion score and time elapsed, or null if no data available
     */
    suspend operator fun invoke(quizId: Long): QuizStats? {
        try {
            // Fetch quiz progress for this quiz
            val progress = quizRepository.getProgressForQuizAsFlow(quizId).first()

            // Calculate statistics if we have progress
            return if (progress != null && progress.isNotEmpty()) {
                // Get the quiz and its questions
                val quiz = quizRepository.getQuizById(quizId)
                val totalQuestions = quiz?.questionCount ?: 1
                
                // Get questions for this quiz
                val questions = quizRepository.getQuestionsForQuiz(quizId).first()
                
                // Count correct answers
                var correctAnswersCount = 0
                
                // Check each answered question against the correct answer
                progress.forEach { (index, answer) ->
                    if (index < questions.size) {
                        val question = questions[index]
                        if (checkQuizAnswerUseCase(question, answer)) {
                            correctAnswersCount++
                        }
                    }
                }
                
                // Calculate average score as correct answers / total questions
                val averageScore = correctAnswersCount.toFloat() / totalQuestions
                
                // Get the completion time from the progress entity
                val progressEntity = quizProgressDao.getProgressForQuiz(quizId)
                val timeElapsed = if (progressEntity?.completionTime != null && progressEntity.completionTime > 0) {
                    ((progressEntity.completionTime) / 1000).toInt()
                } else {
                    // Fallback to a default value if no completion time is available
                    0
                }

                QuizStats(averageScore, timeElapsed)
            } else {
                // No progress yet, return null to indicate no data is available
                null
            }
        } catch (e: Exception) {
            // Handle error - return null to indicate no data is available
            return null
        }
    }
}