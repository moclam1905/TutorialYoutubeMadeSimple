package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.QuizStats
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.tag.Tag // Keep for selectedTagIds if needed, or remove if TagWithCount is sufficient
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.tag.TagWithCount // Import TagWithCount

/**
 * Data class representing the UI state for the Home screen.
 * This follows the MVVM pattern by separating UI state from business logic.
 */
data class HomeViewState(
    val isLoading: Boolean = true,
    val quizzes: List<Quiz> = emptyList(),
    val expandedStatsMap: Map<Long, Boolean> = emptyMap(),
    val quizStatsCache: Map<Long, QuizStats> = emptyMap(),
    val showDeleteConfirmDialog: Long? = null,
    val networkRestricted: Boolean = false,
    val searchQuery: String = "",
    val selectedMainFilter: String = "All", // Key/Name of the main filter
    val selectedQuestionSubFilter: String = "All", // Key/Name of the sub-filter
    val selectedMainFilterIndex: Int = 0, // Index of the selected main filter tab
    val selectedSubFilterIndex: Int = 0, // Index of the selected sub-filter chip
    val allTagsWithCount: List<TagWithCount> = emptyList(), // Renamed and changed type
    val selectedTagIds: Set<Long> = emptySet(), // Set of selected tag IDs for filtering
    val isTagSheetVisible: Boolean = false, // Renamed for clarity (BottomSheet)
    val tagSearchQuery: String = "" // State for tag search query
)
