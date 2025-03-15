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
        val currentQuestionIndex: Int = 0
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
                
                state = state.copy(
                    isLoading = false,
                    quiz = quiz,
                    summary = summary,
                    questions = questionsList,
                    answeredQuestions = answeredQuestions,
                    currentQuestionIndex = currentQuestionIndex
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
     * Save the user's progress in the quiz
     * @param questionIndex The index of the current question
     * @param answer The user's answer to the current question
     */
    fun saveQuizProgress(questionIndex: Int, answer: String) {
        val updatedAnswers = state.answeredQuestions.toMutableMap().apply {
            put(questionIndex, answer)
        }
        
        state = state.copy(
            answeredQuestions = updatedAnswers,
            currentQuestionIndex = questionIndex
        )
        
        // Save progress to database if we have a valid quiz ID
        state.quiz?.id?.let { quizId ->
            viewModelScope.launch {
                quizRepository.saveQuizProgress(quizId, questionIndex, updatedAnswers)
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
}