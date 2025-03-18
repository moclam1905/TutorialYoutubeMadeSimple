package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmoclam.tutorialyoutubemadesimple.data.state.QuizStateManager
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.DeleteQuizUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.GetAllQuizzesUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.GetDaysSinceLastUpdateUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.GetQuizStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllQuizzesUseCase: GetAllQuizzesUseCase,
    private val getQuizStatsUseCase: GetQuizStatsUseCase,
    private val getDaysSinceLastUpdateUseCase: GetDaysSinceLastUpdateUseCase,
    private val deleteQuizUseCase: DeleteQuizUseCase,
    private val quizStateManager: QuizStateManager
) : ViewModel() {

    // UI state for the Home screen
    private val _state = MutableStateFlow(HomeViewState())
    val state: StateFlow<HomeViewState> = _state

    init {
        // Observe quiz state manager and refresh data when needed
        viewModelScope.launch {
            quizStateManager.quizzes.collect { quizzes ->
                _state.update { currentState ->
                    currentState.copy(
                        quizzes = quizzes,
                        isLoading = false
                    )
                }
            }
        }

        viewModelScope.launch {
            quizStateManager.needsRefresh.collect { needsRefresh ->
                if (needsRefresh) {
                    refreshQuizzes()
                }
            }
        }
    }

    fun refreshQuizzes() {
        viewModelScope.launch {
            val quizzes = getAllQuizzesUseCase().first()
            quizStateManager.updateQuizzes(quizzes)
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
                
                // Update the state with the new stats only if not null
                // This ensures we don't display stats for quizzes that haven't been attempted
                if (stats != null) {
                    _state.update { currentState ->
                        val updatedCache = currentState.quizStatsCache.toMutableMap()
                        updatedCache[quizId] = stats
                        currentState.copy(quizStatsCache = updatedCache)
                    }
                } else {
                    // If stats is null, we still want to mark it as processed
                    // but we don't add null to the cache
                    // This ensures the UI will show "No quiz attempts recorded yet"
                    _state.update { it }
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
            
            // Clean up cached data in the state and mark for refresh
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
            quizStateManager.markForRefresh()
        }
    }

    // Calculate days since last update using the GetDaysSinceLastUpdateUseCase
    fun getDaysSinceLastUpdate(lastUpdatedTimestamp: Long): Int {
        return getDaysSinceLastUpdateUseCase(lastUpdatedTimestamp)
    }
}