package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.data.state.QuizStateManager
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.mindmap.GetQuizIdsWithMindmapUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.progress.GetAllQuizProgressMapUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.DeleteQuizUseCase
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

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getQuizStatsUseCase: GetQuizStatsUseCase,
    private val getDaysSinceLastUpdateUseCase: GetDaysSinceLastUpdateUseCase,
    private val deleteQuizUseCase: DeleteQuizUseCase,
    private val getAllQuizProgressMapUseCase: GetAllQuizProgressMapUseCase,
    private val getQuizIdsWithMindmapUseCase: GetQuizIdsWithMindmapUseCase,
    private val quizRepository: QuizRepository, // Inject QuizRepository for filtered queries
    private val quizStateManager: QuizStateManager,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    // UI state for the Home screen
    private val _state = MutableStateFlow(HomeViewState())
    val state: StateFlow<HomeViewState> = _state

    // SharedFlow for raw search query input
    private val searchQueryFlow = MutableSharedFlow<String>(replay = 1)

    init {
        // Load all available tags with their quiz counts
        viewModelScope.launch {
            quizRepository.getAllTagsWithCount().collect { tagsWithCount ->
                _state.update { it.copy(allTagsWithCount = tagsWithCount) }
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

    // Simplified refresh function - just triggers the main filter logic
    fun refreshQuizzes() {
        applyFiltersAndSearch()
    }

    /**
     * Updates the selected main and sub-filters (both keys and indices) and triggers filtering, showing a loading state.
     */
    fun updateFilter(
        mainFilterKey: String,
        mainFilterIndex: Int,
        subFilterKey: String = "All",
        subFilterIndex: Int = 0
    ) {
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
     * Fetches the base list of quizzes (filtered by tags) and then applies
     * main filter, sub-filter (for Questions), and search query.
     * Also fetches necessary progress and mindmap data for efficient filtering.
     */
    private fun applyFiltersAndSearch() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) } // Show loading indicator
            try {
                val currentState = _state.value
                val selectedTagIds = currentState.selectedTagIds
                val mainFilter = currentState.selectedMainFilter
                val subFilter = currentState.selectedQuestionSubFilter
                val searchQuery = currentState.searchQuery.trim().lowercase()

                // Fetch progress and mindmap data using Use Cases (can run concurrently with quiz fetch)
                val progressMapDeferred = async(Dispatchers.IO) { getAllQuizProgressMapUseCase() }
                val mindmapQuizIdsDeferred =
                    async(Dispatchers.IO) { getQuizIdsWithMindmapUseCase() }

                // 1. Fetch base list (already filtered by tags via Repository)
                // Use first() to get the latest list from the Flow
                var filteredList = quizRepository.getFilteredQuizzes(selectedTagIds).first()

                // Await concurrent tasks
                val progressMap = progressMapDeferred.await()
                val mindmapQuizIds = mindmapQuizIdsDeferred.await()

                // 2. Apply Main Filter
                filteredList = when (mainFilter) {
                    "Summary" -> filteredList.filter { quiz: Quiz -> quiz.summaryEnabled }
                    "Question" -> filteredList.filter { quiz: Quiz -> quiz.questionsEnabled }
                    "Mindmap" -> filteredList.filter { quiz: Quiz -> mindmapQuizIds.contains(quiz.id) }
                    else -> filteredList // "All" or other main filters
                }

                // 3. Apply Sub Filter (only if Main Filter is "Question")
                if (mainFilter == "Question") {
                    filteredList = when (subFilter) {
                        "InProgress" -> filteredList.filter { quiz: Quiz ->
                            val progress = progressMap[quiz.id]
                            progress != null && progress.completionTime == 0L
                        }

                        "Completed" -> filteredList.filter { quiz: Quiz ->
                            val progress = progressMap[quiz.id]
                            progress != null && progress.completionTime > 0L
                        }

                        else -> filteredList // "All" questions
                    }
                }

                // 4. Apply Search Query
                if (searchQuery.isNotEmpty()) {
                    filteredList = filteredList.filter { quiz: Quiz ->
                        quiz.title.lowercase().contains(searchQuery) ||
                                (quiz.description?.lowercase()
                                    ?.contains(searchQuery) == true) // Safe call for nullable description
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
            } catch (e: Exception) {
                // Handle potential errors during data fetching or filtering
                _state.update { it.copy(isLoading = false, quizzes = emptyList()) }
                // Optionally log the error e.g., Log.e("HomeViewModel", "Error applying filters", e)
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

    /**
     * Updates the search query for filtering tags within the bottom sheet.
     */
    fun updateTagSearchQuery(query: String) {
        _state.update { it.copy(tagSearchQuery = query) }
        // No need to trigger applyFiltersAndSearch here, filtering happens in UI/BottomSheet
    }

    /**
     * Toggle the visibility of the tag filter bottom sheet.
     */
    fun toggleTagSheet() {
        _state.update { currentState ->
            currentState.copy(isTagSheetVisible = !currentState.isTagSheetVisible)
        }
    }

    /**
     * Select or deselect a tag for filtering.
     */
    fun selectTagFilter(tagId: Long) {
        _state.update { currentState ->
            val newSelectedTagIds = currentState.selectedTagIds.toMutableSet()
            if (newSelectedTagIds.contains(tagId)) {
                newSelectedTagIds.remove(tagId)
            } else {
                newSelectedTagIds.add(tagId)
            }
            currentState.copy(
                selectedTagIds = newSelectedTagIds,
                isLoading = true // Show loading indicator while filtering
            )
        }
        // Apply filters with the updated tag selection
        applyFiltersAndSearch()
    }

    /**
     * Clear all selected tag filters.
     */
    fun clearTagFilters() {
        _state.update { currentState ->
            currentState.copy(
                selectedTagIds = emptySet(),
                isLoading = true // Show loading indicator while filtering
            )
        }
        // Apply filters with cleared tag selection
        applyFiltersAndSearch()
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
