package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.MultipleChoiceQuestion
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.TrueFalseQuestion
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.progress.DeleteQuizProgressUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.progress.GetQuizProgressUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.progress.SaveQuizProgressUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.question.GetQuestionsForQuizUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.CalculateQuizStatisticsUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.CheckQuizAnswerUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.CheckQuizCompletionUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.FindNextUnansweredQuestionUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.GetQuizByIdUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.summary.GetQuizSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizDetailViewModel @Inject constructor(
    private val getQuizByIdUseCase: GetQuizByIdUseCase,
    private val getQuizSummaryUseCase: GetQuizSummaryUseCase,
    private val getQuestionsForQuizUseCase: GetQuestionsForQuizUseCase,
    private val getQuizProgressUseCase: GetQuizProgressUseCase,
    private val saveQuizProgressUseCase: SaveQuizProgressUseCase,
    private val deleteQuizProgressUseCase: DeleteQuizProgressUseCase,
    private val checkQuizAnswerUseCase: CheckQuizAnswerUseCase,
    private val calculateQuizStatisticsUseCase: CalculateQuizStatisticsUseCase,
    private val findNextUnansweredQuestionUseCase: FindNextUnansweredQuestionUseCase,
    private val checkQuizCompletionUseCase: CheckQuizCompletionUseCase
) : ViewModel() {

    data class QuizDetailState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val quiz: Quiz? = null,
        val summary: String = "",
        val questions: List<Any> = emptyList(),
        val answeredQuestions: Map<Int, String> = emptyMap(),
        val skippedQuestions: Set<Int> = emptySet(),
        val currentQuestionIndex: Int = 0,
        val quizStarted: Boolean = false,
        val quizCompleted: Boolean = false,
        val startTime: Long = 0,
        val completionTime: Long = 0,
        val showExitConfirmation: Boolean = false
    )

    var state by mutableStateOf(QuizDetailState(isLoading = false))
        private set

    /**
     * Load quiz data from the database by ID
     */
    fun loadQuizById(quizId: Long) {
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                // Load quiz data using use case
                val quiz = getQuizByIdUseCase(quizId)
                if (quiz == null) {
                    state = state.copy(
                        isLoading = false,
                        errorMessage = "Quiz not found"
                    )
                    return@launch
                }

                // Load summary if available using use case
                val summary = if (quiz.summaryEnabled) {
                    getQuizSummaryUseCase(quizId)?.content ?: ""
                } else {
                    ""
                }

                // Load and parse questions using use case
                val questionsList = mutableListOf<Any>()
                if (quiz.questionsEnabled) {
                    val questions = getQuestionsForQuizUseCase(quizId).first()

                    // Convert database questions to UI question models
                    questions.forEach { question ->
                        if (question.options.size == 2 &&
                            question.options.contains("True") &&
                            question.options.contains("False")
                        ) {
                            // True/False question
                            questionsList.add(
                                TrueFalseQuestion(
                                    statement = question.text,
                                    isTrue = question.correctAnswer == "True"
                                )
                            )
                        } else {
                            // Multiple choice question
                            val options = mutableMapOf<String, String>()
                            question.options.forEachIndexed { index, option ->
                                val key = ('A' + index).toString()
                                options[key] = option
                            }

                            questionsList.add(
                                MultipleChoiceQuestion(
                                    question = question.text,
                                    options = options,
                                    correctAnswers = question.correctAnswer.split(",")
                                )
                            )
                        }
                    }
                }

                // Load quiz progress if available using use case
                val progress = getQuizProgressUseCase.getProgress(quizId)
                val answeredQuestions = progress ?: emptyMap()
                val currentQuestionIndex = if (progress != null && progress.isNotEmpty()) {
                    // If we have progress, use the highest question index as the current index
                    progress.keys.maxOrNull() ?: 0
                } else {
                    0
                }

                // Determine if quiz is completed based on answered questions
                val quizCompleted =
                    questionsList.size == answeredQuestions.size && questionsList.isNotEmpty()

                // If quiz is completed but we don't have time data, set reasonable defaults
                // This ensures getQuizCompletionTimeInSeconds() will work for loaded quizzes
                val startTime = if (quizCompleted && state.startTime == 0L) {
                    // Use lastUpdated timestamp from progress entity minus a default time (e.g., 5 minutes)
                    System.currentTimeMillis() - (5 * 60 * 1000)
                } else {
                    state.startTime
                }

                val completionTime = if (quizCompleted && state.completionTime == 0L) {
                    // Use current time as completion time for already completed quizzes
                    System.currentTimeMillis()
                } else {
                    state.completionTime
                }

                state = state.copy(
                    isLoading = false,
                    quiz = quiz,
                    summary = summary,
                    questions = questionsList,
                    answeredQuestions = answeredQuestions,
                    quizCompleted = quizCompleted,
                    currentQuestionIndex = currentQuestionIndex,
                    startTime = startTime,
                    completionTime = completionTime,
                    quizStarted = quizCompleted || answeredQuestions.isNotEmpty() // Mark as started if completed or has progress
                )

            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error loading quiz"
                )
            }
        }
    }

    /**
     * Start the quiz and begin tracking time
     */
    fun startQuiz() {
        state = state.copy(
            quizStarted = true,
            startTime = System.currentTimeMillis(),
            currentQuestionIndex = 0
        )
    }

    /**
     * Save the user's progress in the quiz
     * @param questionIndex The index of the current question
     * @param answer The user's answer to the current question
     */
    fun saveQuizProgress(questionIndex: Int, answer: String) {
        val updatedAnswers = state.answeredQuestions.toMutableMap().apply {
            put(questionIndex, answer)
        }

        // Use CheckQuizCompletionUseCase to determine if quiz is completed
        val completionResult = checkQuizCompletionUseCase(
            questions = state.questions,
            answeredQuestions = updatedAnswers,
            skippedQuestions = state.skippedQuestions,
            currentQuestionIndex = questionIndex,
            isCurrentlyCompleted = state.quizCompleted,
            startTime = state.startTime
        )

        state = state.copy(
            answeredQuestions = updatedAnswers,
            currentQuestionIndex = questionIndex,
            quizCompleted = completionResult.isCompleted,
            completionTime = if (completionResult.completionTime > 0) completionResult.completionTime else state.completionTime
        )

        // Save progress to database if we have a valid quiz ID
        state.quiz?.id?.let { quizId ->
            viewModelScope.launch {
                saveQuizProgressUseCase(quizId, questionIndex, updatedAnswers)
            }
        }
    }

    /**
     * Skip the current question
     * @param questionIndex The index of the question to skip
     */
    fun skipQuestion(questionIndex: Int) {
        val updatedSkippedQuestions = state.skippedQuestions.toMutableSet().apply {
            add(questionIndex)
        }

        val updatedAnswers = state.answeredQuestions.toMutableMap().apply {
            updatedSkippedQuestions.forEach { questionIndex ->
                if (!containsKey(questionIndex)) {
                    put(questionIndex, "")
                }
            }
        }


//        // Use FindNextUnansweredQuestionUseCase to find the next question
//        val nextIndex = findNextUnansweredQuestionUseCase(
//            startIndex = questionIndex + 1,
//            questions = state.questions,
//            answeredQuestions = state.answeredQuestions,
//            skippedQuestions = updatedSkippedQuestions
//        )

        // Use CheckQuizCompletionUseCase to determine if quiz is completed
        val completionResult = checkQuizCompletionUseCase(
            questions = state.questions,
            answeredQuestions = updatedAnswers,
            skippedQuestions = updatedSkippedQuestions,
            currentQuestionIndex = questionIndex,
            isCurrentlyCompleted = state.quizCompleted,
            startTime = state.startTime
        )

        state = state.copy(
            skippedQuestions = updatedSkippedQuestions,
            currentQuestionIndex = questionIndex,
            quizCompleted = completionResult.isCompleted,
            completionTime = if (completionResult.completionTime > 0) completionResult.completionTime else state.completionTime
        )

        // Save progress to database if we have a valid quiz ID
        state.quiz?.id?.let { quizId ->
            viewModelScope.launch {
                saveQuizProgressUseCase(quizId, questionIndex, updatedAnswers)
            }
        }
    }

    /**
     * Show the exit confirmation dialog
     */
    fun showExitConfirmation() {
        state = state.copy(showExitConfirmation = true)
    }

    /**
     * Hide the exit confirmation dialog
     */
    fun hideExitConfirmation() {
        state = state.copy(showExitConfirmation = false)
    }

    /**
     * Confirm exit and reset quiz timer
     */
    fun confirmExit() {
        state = state.copy(
            showExitConfirmation = false,
            quizStarted = false,
            startTime = 0
        )
    }

    /**
     * Reset the quiz to start over
     */
    fun resetQuiz() {
        state = state.copy(
            quizStarted = false,
            quizCompleted = false,
            answeredQuestions = emptyMap(),
            skippedQuestions = emptySet(),
            currentQuestionIndex = 0,
            startTime = 0,
            completionTime = 0
        )

        // Clear progress in database if we have a valid quiz ID
        state.quiz?.id?.let { quizId ->
            viewModelScope.launch {
                deleteQuizProgressUseCase(quizId)
            }
        }
    }

    /**
     * Get the last answered question index
     * @return The index of the last answered question, or 0 if no questions have been answered
     */
    fun getLastAnsweredQuestionIndex(): Int {
        return if (state.answeredQuestions.isNotEmpty()) {
            state.answeredQuestions.keys.maxOrNull() ?: 0
        } else {
            state.currentQuestionIndex
        }
    }

    /**
     * Get the user's answer for a specific question
     * @param questionIndex The index of the question
     * @return The user's answer, or an empty string if the question hasn't been answered
     */
    fun getAnswerForQuestion(questionIndex: Int): String {
        return state.answeredQuestions[questionIndex] ?: ""
    }

    /**
     * Get the quiz completion time in seconds
     * @return The time taken to complete the quiz in seconds
     */
    fun getQuizCompletionTimeInSeconds(): Int {
        return if (state.quizCompleted && state.startTime > 0 && state.completionTime > 0) {
            ((state.completionTime - state.startTime) / 1000).toInt()
        } else {
            0
        }
    }

    /**
     * Get the correct answers count
     * @return The number of correctly answered questions
     */
    fun getCorrectAnswersCount(): Int {
        val statistics = calculateQuizStatisticsUseCase(
            questions = state.questions,
            answeredQuestions = state.answeredQuestions,
            startTime = state.startTime,
            completionTime = state.completionTime
        )
        return statistics.correctAnswersCount
    }

    /**
     * Get the incorrect answers count
     * @return The number of incorrectly answered questions
     */
    fun getIncorrectAnswersCount(): Int {
        val statistics = calculateQuizStatisticsUseCase(
            questions = state.questions,
            answeredQuestions = state.answeredQuestions,
            startTime = state.startTime,
            completionTime = state.completionTime
        )
        return statistics.incorrectAnswersCount
    }

    /**
     * Get the list of correctly answered questions
     * @return List of indices of correctly answered questions
     */
    fun getCorrectlyAnsweredQuestions(): List<Int> {
        val statistics = calculateQuizStatisticsUseCase(
            questions = state.questions,
            answeredQuestions = state.answeredQuestions,
            startTime = state.startTime,
            completionTime = state.completionTime
        )
        return statistics.correctQuestionIndices
    }

    /**
     * Get the list of incorrectly answered questions
     * @return List of indices of incorrectly answered questions
     */
    fun getIncorrectlyAnsweredQuestions(): List<Int> {
        val statistics = calculateQuizStatisticsUseCase(
            questions = state.questions,
            answeredQuestions = state.answeredQuestions,
            startTime = state.startTime,
            completionTime = state.completionTime
        )
        return statistics.incorrectQuestionIndices
    }

    /**
     * Explicitly check if the quiz is completed
     * This is useful when we need to force a check for completion
     * (e.g., when the user is on the last question)
     */
    fun checkQuizCompletion() {
        // Only proceed if we have questions
        if (state.questions.isEmpty()) return

        val completionResult = checkQuizCompletionUseCase(
            questions = state.questions,
            answeredQuestions = state.answeredQuestions,
            skippedQuestions = state.skippedQuestions,
            currentQuestionIndex = state.currentQuestionIndex,
            isCurrentlyCompleted = state.quizCompleted,
            startTime = state.startTime
        )

        if (completionResult.isCompleted && !state.quizCompleted) {
            state = state.copy(
                quizCompleted = true,
                completionTime = completionResult.completionTime
            )
        }
    }

    /**
     * Get the number of skipped questions from answered questions (Database)
     * @return The number of skipped questions
     */
    fun getSkippedQuestionsCount(): Int {
        val statistics = calculateQuizStatisticsUseCase(
            questions = state.questions,
            answeredQuestions = state.answeredQuestions,
            startTime = state.startTime,
            completionTime = state.completionTime
        )
        return statistics.skippedAnswersCount
    }

    /**
     * Get the list of skipped questions from answered questions (Database)
     * @return List of indices of skipped questions
     */
    fun getSkippedQuestions(): List<Int> {
        val statistics = calculateQuizStatisticsUseCase(
            questions = state.questions,
            answeredQuestions = state.answeredQuestions,
            startTime = state.startTime,
            completionTime = state.completionTime
        )
        return statistics.skippedQuestionIndices
    }
}