package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmoclam.tutorialyoutubemadesimple.data.state.QuizStateManager
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.mindmap.GetQuizIdsWithMindmapUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.progress.GetAllQuizProgressMapUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.DeleteQuizUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.GetAllQuizzesUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.GetDaysSinceLastUpdateUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.GetQuizStatsUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
@OptIn(FlowPreview::class) // Needed for debounce
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllQuizzesUseCase: GetAllQuizzesUseCase,
    private val getQuizStatsUseCase: GetQuizStatsUseCase,
    private val getDaysSinceLastUpdateUseCase: GetDaysSinceLastUpdateUseCase,
    private val deleteQuizUseCase: DeleteQuizUseCase,
    private val getAllQuizProgressMapUseCase: GetAllQuizProgressMapUseCase,
    private val getQuizIdsWithMindmapUseCase: GetQuizIdsWithMindmapUseCase,
    private val quizStateManager: QuizStateManager,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    // UI state for the Home screen
    private val _state = MutableStateFlow(HomeViewState())
    val state: StateFlow<HomeViewState> = _state

    // Holds the complete list of quizzes fetched from the source
    private var allQuizzes: List<Quiz> = emptyList()

    // SharedFlow for raw search query input
    private val searchQueryFlow = MutableSharedFlow<String>(replay = 1)

    init {
        // Observe quiz state manager and refresh data when needed
        viewModelScope.launch {
            quizStateManager.quizzes.collect { quizzes ->
                allQuizzes = quizzes
                applyFiltersAndSearch() // Apply combined filters
            }
        }

        viewModelScope.launch {
            quizStateManager.needsRefresh.collect { needsRefresh ->
                if (needsRefresh) {
                    refreshQuizzes()
                }
            }
        }

        // Debounce search query input
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300L) // Wait 300ms after last input
                .distinctUntilChanged() // Only process if the query actually changed
                .collectLatest { query ->
                    // Set loading state and update the actual search query in state
                    _state.update { it.copy(isLoading = true, searchQuery = query) }
                    applyFiltersAndSearch() // Now apply filters with the debounced query
                }
        }

        // Initial load
        refreshQuizzes()
        // Emit initial empty query to setup the flow
        viewModelScope.launch { searchQueryFlow.emit("") }
    }

    fun refreshQuizzes() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) } // Show loading indicator
            try {
                // Check if offline data is available, regardless of offline mode being enabled or not
                val offlineQuizzes = getAllQuizzesUseCase().first()

                if (offlineQuizzes.isNotEmpty()) {
                    allQuizzes = offlineQuizzes
                    quizStateManager.updateQuizzes(offlineQuizzes)
                    applyFiltersAndSearch() // Apply combined filters
                } else if (networkUtils.shouldLoadContent()) {
                    val quizzes = getAllQuizzesUseCase().first()
                    allQuizzes = quizzes
                    quizStateManager.updateQuizzes(quizzes)
                    applyFiltersAndSearch() // Apply combined filters
                } else {
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            networkRestricted = true,
                            quizzes = emptyList() // Clear list if network restricted and no offline data
                        )
                    }
                }
            } catch (e: Exception) {
                // Handle potential errors during data fetching
                _state.update { it.copy(isLoading = false, quizzes = emptyList()) }
                // Optionally log the error or show a message
            }
        }
    }

    /**
     * Updates the selected main and sub-filters (both keys and indices) and triggers filtering, showing a loading state.
     */
    fun updateFilter(mainFilterKey: String, mainFilterIndex: Int, subFilterKey: String = "All", subFilterIndex: Int = 0) {
        val isQuestionFilter = mainFilterKey == "Question"
        // Set loading state immediately before starting the filtering process
        _state.update {
            it.copy(
                isLoading = true, // Show loading indicator
                selectedMainFilter = mainFilterKey,
                selectedMainFilterIndex = mainFilterIndex,
                // Only update sub-filter if main filter is Question, otherwise reset
                selectedQuestionSubFilter = if (isQuestionFilter) subFilterKey else "All",
                selectedSubFilterIndex = if (isQuestionFilter) subFilterIndex else 0
            )
        }
        applyFiltersAndSearch()
    }

    /**
     * Update search query by emitting to the raw input flow.
     */
    fun updateSearchQuery(query: String) {
        // Emit the raw query to the flow for debouncing
        viewModelScope.launch {
            // Update the state immediately for instant UI feedback in the search bar
            // but don't trigger filtering yet.
             _state.update { it.copy(searchQuery = query) }
            // Emit to the flow which will handle debouncing and triggering the actual search/filter
            searchQueryFlow.emit(query)
        }
    }

    /**
     * Apply main filter, sub-filter (for Questions), and search query to the list of all quizzes.
     * Fetches necessary progress and mindmap data for efficient filtering.
     */
    private fun applyFiltersAndSearch() {
        viewModelScope.launch {
            val currentState = _state.value
            val mainFilter = currentState.selectedMainFilter
            val subFilter = currentState.selectedQuestionSubFilter
            // Read the debounced search query from the state
            val searchQuery = currentState.searchQuery.trim().lowercase()

            // Fetch progress and mindmap data using Use Cases
            val progressMapDeferred = async(Dispatchers.IO) { getAllQuizProgressMapUseCase() }
            val mindmapQuizIdsDeferred = async(Dispatchers.IO) { getQuizIdsWithMindmapUseCase() }

            val progressMap = progressMapDeferred.await()
            val mindmapQuizIds = mindmapQuizIdsDeferred.await()

            var filteredList = allQuizzes

            // 1. Apply Main Filter
            filteredList = when (mainFilter) {
                "Summary" -> filteredList.filter { it.summaryEnabled }
                "Question" -> filteredList.filter { it.questionsEnabled }
                "Mindmap" -> filteredList.filter { mindmapQuizIds.contains(it.id) }
                else -> filteredList // "All"
            }

            // 2. Apply Sub Filter (only if Main Filter is "Question")
            if (mainFilter == "Question") {
                filteredList = when (subFilter) {
                    "InProgress" -> filteredList.filter { quiz ->
                        val progress = progressMap[quiz.id]
                        progress != null && progress.completionTime == 0L
                    }
                    "Completed" -> filteredList.filter { quiz ->
                        val progress = progressMap[quiz.id]
                        progress != null && progress.completionTime > 0L
                    }
                    else -> filteredList // "All" questions
                }
            }

            // 3. Apply Search Query
            if (searchQuery.isNotEmpty()) {
                filteredList = filteredList.filter { quiz ->
                    quiz.title.lowercase().contains(searchQuery) ||
                    quiz.description.lowercase().contains(searchQuery) == true // Optional: search description too
                }
            }

            // Update the state with the final filtered list
            _state.update {
                it.copy(
                    quizzes = filteredList,
                    isLoading = false,
                    networkRestricted = false // Reset network restricted flag if data is loaded
                )
            }
        }
    }


    // Toggle expanded state for a quiz
    fun toggleStatsExpanded(quizId: Long) {
        val currentState = _state.value
        val expandedMap = currentState.expandedStatsMap.toMutableMap()
        val isCurrentlyExpanded = expandedMap[quizId] == true
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
