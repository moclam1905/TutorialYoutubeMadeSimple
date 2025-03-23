package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.QuizStats

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
    val networkRestricted: Boolean = false
)