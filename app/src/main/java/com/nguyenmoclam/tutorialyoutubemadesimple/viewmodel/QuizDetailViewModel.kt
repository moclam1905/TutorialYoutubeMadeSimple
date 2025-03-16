package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.MultipleChoiceQuestion
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.TrueFalseQuestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class QuizDetailViewModel @Inject constructor(
    private val quizRepository: QuizRepository
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
                // Load quiz data
                val quiz = quizRepository.getQuizById(quizId)
                if (quiz == null) {
                    state = state.copy(
                        isLoading = false,
                        errorMessage = "Quiz not found"
                    )
                    return@launch
                }
                
                // Load summary if available
                val summary = if (quiz.summaryEnabled) {
                    quizRepository.getSummaryByQuizId(quizId)?.content ?: ""
                } else {
                    ""
                }
                
                // Load and parse questions
                val questionsList = mutableListOf<Any>()
                if (quiz.questionsEnabled) {
                    val questions = quizRepository.getQuestionsForQuiz(quizId).first()
                    
                    // Convert database questions to UI question models
                    questions.forEach { question ->
                        if (question.options.size == 2 && 
                            question.options.contains("True") && 
                            question.options.contains("False")) {
                            // True/False question
                            questionsList.add(TrueFalseQuestion(
                                statement = question.text,
                                isTrue = question.correctAnswer == "True"
                            ))
                        } else {
                            // Multiple choice question
                            val options = mutableMapOf<String, String>()
                            question.options.forEachIndexed { index, option ->
                                val key = ('A' + index).toString()
                                options[key] = option
                            }
                            
                            questionsList.add(MultipleChoiceQuestion(
                                question = question.text,
                                options = options,
                                correctAnswers = question.correctAnswer.split(",")
                            ))
                        }
                    }
                }
                
                // Load quiz progress if available
                val progress = quizRepository.getProgressForQuiz(quizId)
                val answeredQuestions = progress ?: emptyMap()
                val currentQuestionIndex = if (progress != null && progress.isNotEmpty()) {
                    // If we have progress, use the highest question index as the current index
                    progress.keys.maxOrNull() ?: 0
                } else {
                    0
                }
                
                // Determine if quiz is completed based on answered questions
                val quizCompleted = questionsList.size == answeredQuestions.size && questionsList.isNotEmpty()
                
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
        
        // Check if all questions are now answered or skipped
        val allQuestionsHandled = state.questions.indices.all { index ->
            updatedAnswers.containsKey(index) || state.skippedQuestions.contains(index)
        }
        
        // Only mark as completed if we're on the last question and all questions are handled
        // This ensures we don't show the results screen prematurely
        val isLastQuestion = questionIndex == state.questions.size - 1
        val quizCompleted = if (allQuestionsHandled && isLastQuestion && !state.quizCompleted) {
            true
        } else {
            state.quizCompleted
        }
        
        // Calculate completion time if quiz is newly completed
        val completionTime = if (quizCompleted && !state.quizCompleted) {
            System.currentTimeMillis()
        } else {
            state.completionTime
        }
        
        state = state.copy(
            answeredQuestions = updatedAnswers,
            currentQuestionIndex = questionIndex,
            quizCompleted = quizCompleted,
            completionTime = completionTime
        )
        
        // Save progress to database if we have a valid quiz ID
        state.quiz?.id?.let { quizId ->
            viewModelScope.launch {
                quizRepository.saveQuizProgress(quizId, questionIndex, updatedAnswers)
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
        
        // Move to the next question or mark as completed if all questions are handled
        val nextIndex = findNextUnansweredQuestion(questionIndex + 1)
        
        // Check if all questions are now answered or skipped
        val allQuestionsHandled = state.questions.indices.all { index ->
            state.answeredQuestions.containsKey(index) || updatedSkippedQuestions.contains(index)
        }
        
        // Only mark as completed if we're on the last question and all questions are handled
        // This ensures we don't show the results screen prematurely
        val isLastQuestion = questionIndex == state.questions.size - 1
        val quizCompleted = if (allQuestionsHandled && isLastQuestion && !state.quizCompleted) {
            true
        } else {
            state.quizCompleted
        }
        
        // Calculate completion time if quiz is newly completed
        val completionTime = if (quizCompleted && !state.quizCompleted) {
            System.currentTimeMillis()
        } else {
            state.completionTime
        }
        
        state = state.copy(
            skippedQuestions = updatedSkippedQuestions,
            currentQuestionIndex = nextIndex,
            quizCompleted = quizCompleted,
            completionTime = completionTime
        )
        
        // Save progress to database if we have a valid quiz ID
        state.quiz?.id?.let { quizId ->
            viewModelScope.launch {
                quizRepository.saveQuizProgress(quizId, nextIndex, state.answeredQuestions)
            }
        }
    }
    
    /**
     * Find the next unanswered and unskipped question
     * @param startIndex The index to start searching from
     * @return The index of the next unanswered question, or the current index if all questions are answered
     */
    private fun findNextUnansweredQuestion(startIndex: Int): Int {
        if (state.questions.isEmpty()) return 0
        
        // Loop through questions starting from startIndex
        for (i in 0 until state.questions.size) {
            val index = (startIndex + i) % state.questions.size
            if (!state.answeredQuestions.containsKey(index) && !state.skippedQuestions.contains(index)) {
                return index
            }
        }
        
        // If all questions are answered or skipped, return the first question
        return 0
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
                quizRepository.deleteProgressForQuiz(quizId)
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
        var correctCount = 0
        state.answeredQuestions.forEach { (index, answer) ->
            if (index < state.questions.size) {
                val question = state.questions[index]
                val isCorrect = when (question) {
                    is MultipleChoiceQuestion -> question.correctAnswers.contains(answer)
                    is TrueFalseQuestion -> (answer == "True" && question.isTrue) || 
                                           (answer == "False" && !question.isTrue)
                    else -> false
                }
                if (isCorrect) correctCount++
            }
        }
        return correctCount
    }
    
    /**
     * Get the incorrect answers count
     * @return The number of incorrectly answered questions
     */
    fun getIncorrectAnswersCount(): Int {
        return state.answeredQuestions.size - getCorrectAnswersCount()
    }
    
    /**
     * Get the list of correctly answered questions
     * @return List of indices of correctly answered questions
     */
    fun getCorrectlyAnsweredQuestions(): List<Int> {
        val correctIndices = mutableListOf<Int>()
        state.answeredQuestions.forEach { (index, answer) ->
            if (index < state.questions.size) {
                val question = state.questions[index]
                val isCorrect = when (question) {
                    is MultipleChoiceQuestion -> question.correctAnswers.contains(answer)
                    is TrueFalseQuestion -> (answer == "True" && question.isTrue) || 
                                           (answer == "False" && !question.isTrue)
                    else -> false
                }
                if (isCorrect) correctIndices.add(index)
            }
        }
        return correctIndices
    }
    
    /**
     * Get the list of incorrectly answered questions
     * @return List of indices of incorrectly answered questions
     */
    fun getIncorrectlyAnsweredQuestions(): List<Int> {
        val incorrectIndices = mutableListOf<Int>()
        state.answeredQuestions.forEach { (index, answer) ->
            if (index < state.questions.size) {
                val question = state.questions[index]
                val isCorrect = when (question) {
                    is MultipleChoiceQuestion -> question.correctAnswers.contains(answer)
                    is TrueFalseQuestion -> (answer == "True" && question.isTrue) || 
                                           (answer == "False" && !question.isTrue)
                    else -> false
                }
                if (!isCorrect) incorrectIndices.add(index)
            }
        }
        return incorrectIndices
    }
    
    /**
     * Explicitly check if the quiz is completed
     * This is useful when we need to force a check for completion
     * (e.g., when the user is on the last question)
     */
    fun checkQuizCompletion() {
        // Only proceed if we have questions
        if (state.questions.isEmpty()) return
        
        // Check if all questions are now answered or skipped
        val allQuestionsHandled = state.questions.indices.all { index ->
            state.answeredQuestions.containsKey(index) || state.skippedQuestions.contains(index)
        }
        
        // Only mark as completed if all questions are handled and we're on the last question
        val isLastQuestion = state.currentQuestionIndex == state.questions.size - 1
        
        if (allQuestionsHandled && isLastQuestion && !state.quizCompleted) {
            // Ensure we have a valid startTime before setting completion time
            val currentStartTime = if (state.startTime == 0L) {
                // If startTime wasn't set, use a reasonable default (current time minus 5 minutes)
                System.currentTimeMillis() - (5 * 60 * 1000)
            } else {
                state.startTime
            }
            
            // Mark as completed and set completion time
            state = state.copy(
                quizCompleted = true,
                startTime = currentStartTime,
                completionTime = System.currentTimeMillis()
            )
        }
    }
}