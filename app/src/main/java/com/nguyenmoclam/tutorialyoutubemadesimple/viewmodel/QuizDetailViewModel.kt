package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.MindMap
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.MultipleChoiceQuestion
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.TrueFalseQuestion
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.mindmap.GetMindMapUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.mindmap.SaveMindMapUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.progress.DeleteQuizProgressUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.progress.GetQuizProgressUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.progress.SaveQuizProgressUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.question.GetQuestionsForQuizUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.CalculateQuizStatisticsUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.CheckQuizCompletionUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.GenerateMindMapUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.GetQuizByIdUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.summary.GetQuizSummaryUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.transcript.GetTranscriptUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.LLMProcessor
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.OfflineSyncManager
import com.nguyenmoclam.tutorialyoutubemadesimple.data.state.QuizStateManager
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
    private val calculateQuizStatisticsUseCase: CalculateQuizStatisticsUseCase,
    private val checkQuizCompletionUseCase: CheckQuizCompletionUseCase,
    private val getTranscriptUseCase: GetTranscriptUseCase,
    private val generateMindMapUseCase: GenerateMindMapUseCase,
    private val saveMindMapUseCase: SaveMindMapUseCase,
    private val getMindMapUseCase: GetMindMapUseCase,
    private val llmProcessor: LLMProcessor,
    private val offlineSyncManager: OfflineSyncManager,
    private val quizStateManager: QuizStateManager
) : ViewModel() {

    enum class ProcessingMindMapStep(val messageRes: Int) {
        FETCH_TRANSCRIPT_START(R.string.step_process_transcript),
        FETCH_TRANSCRIPT_COMPLETE(R.string.step_process_transcript),
        EXTRACT_KEY_POINTS_START(R.string.generating_mindmap),
        EXTRACT_KEY_POINTS_COMPLETE(R.string.generating_mindmap),
        GENERATE_MIND_MAP_START(R.string.generating_mindmap),
        GENERATE_MIND_MAP_COMPLETE(R.string.generating_mindmap),
        SAVE_TO_DATABASE_START(R.string.saving_database),
        SAVE_TO_DATABASE_COMPLETE(R.string.saving_database),
        NONE(0);

        fun getMessage(context: Context): String {
            return if (messageRes != 0) context.getString(messageRes) else ""
        }

        fun getProgressPercentage(): Float {
            return when (this) {
                NONE -> 0f
                FETCH_TRANSCRIPT_START -> 10f
                FETCH_TRANSCRIPT_COMPLETE -> 25f
                EXTRACT_KEY_POINTS_START -> 35f
                EXTRACT_KEY_POINTS_COMPLETE -> 50f
                GENERATE_MIND_MAP_START -> 65f
                GENERATE_MIND_MAP_COMPLETE -> 80f
                SAVE_TO_DATABASE_START -> 90f
                SAVE_TO_DATABASE_COMPLETE -> 100f
            }
        }
    }

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
        val showExitConfirmation: Boolean = false,
        val mindMapCode: String = "",
        val currentMindMapStep: ProcessingMindMapStep = ProcessingMindMapStep.NONE
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
                // Sync quiz data before loading
                offlineSyncManager.syncQuizDataById(quizId)

                // Manage offline storage using OfflineDataManager (Removed call from here)

                // Load quiz data using use case
                val quiz = getQuizByIdUseCase(quizId)
                if (quiz == null) {
                    state = state.copy(
                        isLoading = false,
                        errorMessage = "Quiz not found"
                    )
                    return@launch
                }

                // Auto download content for quiz
                offlineSyncManager.autoDownloadContentForQuiz(quizId)

                // Load summary if available using use case
                val summary = if (quiz.summaryEnabled) {
                    val summaryContent = getQuizSummaryUseCase(quizId)?.content ?: ""
                    if (summaryContent.isNotBlank()) {
                        downloadSummaryWebContent(summaryContent)
                    }
                    summaryContent
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
                val progressEntity = getQuizProgressUseCase.getProgressEntity(quizId)
                val answeredQuestions =
                    progressEntity?.answeredQuestions?.mapKeys { it.key.toInt() } ?: emptyMap()
                val currentQuestionIndex = if (answeredQuestions.isNotEmpty()) {
                    // If we have progress, use the highest question index as the current index
                    answeredQuestions.keys.maxOrNull() ?: 0
                } else {
                    0
                }

                // Determine if quiz is completed based on answered questions and completion time
                val quizCompleted =
                    progressEntity?.completionTime != null && progressEntity.completionTime > 0

                // Get start time and completion time from progress entity
                val startTime = if (quizCompleted) {
                    // If completed, calculate start time from completion time and last updated
                    progressEntity.lastUpdated.minus(progressEntity.completionTime)
                } else {
                    state.startTime
                }

                val completionTime =
                    if (quizCompleted) {
                        // If completed, use the stored completion time
                        progressEntity.completionTime
                    } else {
                        state.completionTime
                    }

                val mindMapCode = getMindMapUseCase(quizId)?.mermaidCode ?: ""

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
                    quizStarted = quizCompleted || answeredQuestions.isNotEmpty(), // Mark as started if completed or has progress
                    mindMapCode = mindMapCode
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
                // Calculate the elapsed time (duration) if quiz is completed
                val elapsedTime = if (state.quizCompleted && state.startTime > 0) {
                    System.currentTimeMillis() - state.startTime
                } else {
                    0L
                }
                saveQuizProgressUseCase(quizId, questionIndex, updatedAnswers, elapsedTime)
                quizStateManager.markForRefresh() // Mark for refresh after saving progress
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
                // Calculate the elapsed time (duration) if quiz is completed
                val elapsedTime = if (state.quizCompleted && state.startTime > 0) {
                    System.currentTimeMillis() - state.startTime
                } else {
                    0L
                }
                saveQuizProgressUseCase(quizId, questionIndex, updatedAnswers, elapsedTime)
                quizStateManager.markForRefresh() // Mark for refresh after saving progress (due to skip)
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
                quizStateManager.markForRefresh() // Mark for refresh after deleting progress
            }
        }
    }

    /**
     * Download the web content related to the summary
     */
    private fun downloadSummaryWebContent(summaryContent: String) {
        if (summaryContent.isBlank()) return

        viewModelScope.launch {
            try {
                // Create a temporary URL for offline storage
                val summaryUrl = "https://tutorialyoutubemadesimple.app/summary/${state.quiz?.id}"
                offlineSyncManager.downloadAndSaveWebContent(summaryUrl)
            } catch (e: Exception) {
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
            val elapsedTime = state.completionTime - state.startTime
            if (elapsedTime < 0) {
                (state.completionTime / 1000).toInt()
            } else {
                ((state.completionTime - state.startTime) / 1000).toInt()
            }
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
            val currentTime = System.currentTimeMillis()
            state = state.copy(
                quizCompleted = true,
                completionTime = currentTime
            )
            state.quiz?.id?.let { quizId ->
                viewModelScope.launch {
                    val elapsedTime = if (state.startTime > 0) {
                        currentTime - state.startTime
                    } else {
                        0L
                    }
                    saveQuizProgressUseCase(
                        quizId,
                        state.currentQuestionIndex,
                        state.answeredQuestions,
                        elapsedTime
                    )
                    quizStateManager.markForRefresh() // Mark for refresh after saving final progress
                }
            }
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

    /**
     * Generates a mind map for the current quiz using the transcript content.
     * This function sets the loading state, calls the GenerateMindMapUseCase,
     * and updates the UI state with the result.
     */
    fun generateMindMap() {
        val quizId = state.quiz?.id ?: return  // ensure we have a loaded quiz
        // Set loading state to true to show loading animation and update current step
        state = state.copy(
            isLoading = true,
            errorMessage = null,
            currentMindMapStep = ProcessingMindMapStep.FETCH_TRANSCRIPT_START
        )
        viewModelScope.launch {
            try {
                // Get the transcript content from DB (for the current quiz)
                val transcript = getTranscriptUseCase(quizId)
                if (transcript == null) {
                    state = state.copy(
                        isLoading = false,
                        errorMessage = "Transcript not found",
                        currentMindMapStep = ProcessingMindMapStep.NONE
                    )
                    return@launch
                }

                // Update state to show transcript fetch complete
                state =
                    state.copy(currentMindMapStep = ProcessingMindMapStep.FETCH_TRANSCRIPT_COMPLETE)

                // Update state to show we're starting to extract key points
                state =
                    state.copy(currentMindMapStep = ProcessingMindMapStep.EXTRACT_KEY_POINTS_START)

                // Call use case to generate mind map
                state =
                    state.copy(currentMindMapStep = ProcessingMindMapStep.EXTRACT_KEY_POINTS_COMPLETE)

                // Update state to show we're starting mind map generation
                state =
                    state.copy(currentMindMapStep = ProcessingMindMapStep.GENERATE_MIND_MAP_START)

                val result = generateMindMapUseCase(
                    transcript.content,
                    transcript.language,
                    state.quiz?.title
                )

                // Update state to show mind map generation is complete
                state =
                    state.copy(currentMindMapStep = ProcessingMindMapStep.GENERATE_MIND_MAP_COMPLETE)

                if (result.error != null) {
                    // Handle LLM generation error
                    state = state.copy(
                        isLoading = false,
                        errorMessage = result.error,
                        currentMindMapStep = ProcessingMindMapStep.NONE
                    )
                } else {
                    // Update state to show we're starting to save to database
                    state =
                        state.copy(currentMindMapStep = ProcessingMindMapStep.SAVE_TO_DATABASE_START)

                    // Get extracted key points from use case
                    val keyPoints = generateMindMapUseCase.getLastExtractedKeyPoints()
                    // Save generated mind map to database with key points
                    val mindMap = MindMap(
                        quizId = quizId,
                        keyPoints = keyPoints,
                        mermaidCode = result.mermaidCode
                    )
                    saveMindMapUseCase(mindMap, quizId)
                    quizStateManager.markForRefresh() // Mark for refresh after saving mind map

                    // Update state to show database save is complete
                    state =
                        state.copy(currentMindMapStep = ProcessingMindMapStep.SAVE_TO_DATABASE_COMPLETE)

                    // Update final state
                    state = state.copy(
                        isLoading = false,
                        mindMapCode = result.mermaidCode,
                        errorMessage = null,
                        currentMindMapStep = ProcessingMindMapStep.NONE
                    )
                }
            } catch (e: Exception) {
                // Handle any exceptions and set loading to false
                state = state.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to generate mind map",
                    currentMindMapStep = ProcessingMindMapStep.NONE
                )
            }
        }
    }

    fun getLLMProcessor(): LLMProcessor {
        return llmProcessor
    }

    fun getLanguage(): String {
        return state.quiz?.language ?: "English"
    }

    /**
     * Get QuizRepository to use in OfflineSyncManager
     */
    fun getQuizRepository() = getQuizByIdUseCase.getQuizRepository()

    fun updateMindMapCode(newCode: String) {
        state = state.copy(mindMapCode = newCode)

        // Save the updated mind map to the database
        viewModelScope.launch {
            try {
                val quizId = state.quiz?.id ?: return@launch
                val existingMindMap = getMindMapUseCase(quizId) ?: return@launch

                // Create a new mind map with the updated code
                val updatedMindMap = existingMindMap.copy(mermaidCode = newCode)

                saveMindMapUseCase(updatedMindMap, quizId)
                quizStateManager.markForRefresh() // Mark for refresh after updating mind map
            } catch (e: Exception) {
            }
        }
    }
}

