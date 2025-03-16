package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.QuizStats
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.DeleteQuizUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.GetAllQuizzesUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.GetDaysSinceLastUpdateUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.GetQuizStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllQuizzesUseCase: GetAllQuizzesUseCase,
    private val getQuizStatsUseCase: GetQuizStatsUseCase,
    private val getDaysSinceLastUpdateUseCase: GetDaysSinceLastUpdateUseCase,
    private val deleteQuizUseCase: DeleteQuizUseCase
) : ViewModel() {

    // UI state for the Home screen
    private val _state = MutableStateFlow(HomeViewState())
    val state: StateFlow<HomeViewState> = _state

    // StateFlow of all quizzes
    private val quizzesFlow: StateFlow<List<Quiz>> = getAllQuizzesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Combine quizzes flow with state to update the UI state
        viewModelScope.launch {
            quizzesFlow.collect { quizzes ->
                _state.update { currentState ->
                    currentState.copy(
                        quizzes = quizzes,
                        isLoading = false
                    )
                }
            }
        }
    }

    // Toggle expanded state for a quiz
    fun toggleStatsExpanded(quizId: Long) {
        val currentState = _state.value
        val expandedMap = currentState.expandedStatsMap.toMutableMap()
        val isCurrentlyExpanded = expandedMap[quizId] ?: false
        expandedMap[quizId] = !isCurrentlyExpanded
        
        _state.update { it.copy(expandedStatsMap = expandedMap) }

        // Load stats if expanding and not already cached
        if (!isCurrentlyExpanded && !currentState.quizStatsCache.containsKey(quizId)) {
            loadQuizStats(quizId)
        }
    }

    // Load statistics for a quiz using the GetQuizStatsUseCase
    private fun loadQuizStats(quizId: Long) {
        viewModelScope.launch {
            try {
                // Use the domain use case to get quiz statistics
                val stats = getQuizStatsUseCase(quizId)
                
                // Update the state with the new stats
                _state.update { currentState ->
                    val updatedCache = currentState.quizStatsCache.toMutableMap()
                    updatedCache[quizId] = stats
                    currentState.copy(quizStatsCache = updatedCache)
                }
            } catch (e: Exception) {
                // Error handling is now encapsulated in the use case
            }
        }
    }

    // Show delete confirmation dialog
    fun showDeleteQuizDialog(quizId: Long) {
        _state.update { it.copy(showDeleteConfirmDialog = quizId) }
    }

    // Hide delete confirmation dialog
    fun hideDeleteQuizDialog() {
        _state.update { it.copy(showDeleteConfirmDialog = null) }
    }

    // Delete a quiz using the DeleteQuizUseCase
    fun deleteQuiz(quizId: Long) {
        viewModelScope.launch {
            // Use the domain use case to delete the quiz
            deleteQuizUseCase(quizId)
            
            // Clean up cached data in the state
            _state.update { currentState ->
                val expandedMap = currentState.expandedStatsMap.toMutableMap()
                expandedMap.remove(quizId)
                
                val statsCache = currentState.quizStatsCache.toMutableMap()
                statsCache.remove(quizId)
                
                currentState.copy(
                    expandedStatsMap = expandedMap,
                    quizStatsCache = statsCache,
                    showDeleteConfirmDialog = null
                )
            }
        }
    }

    // Calculate days since last update using the GetDaysSinceLastUpdateUseCase
    fun getDaysSinceLastUpdate(lastUpdatedTimestamp: Long): Int {
        return getDaysSinceLastUpdateUseCase(lastUpdatedTimestamp)
    }
}