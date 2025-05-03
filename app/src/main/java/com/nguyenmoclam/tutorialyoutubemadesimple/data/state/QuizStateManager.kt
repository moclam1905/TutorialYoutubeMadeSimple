package com.nguyenmoclam.tutorialyoutubemadesimple.data.state

import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton class responsible for managing the shared state of quizzes across the app.
 * This prevents redundant database calls and ensures data consistency.
 */
@Singleton
class QuizStateManager @Inject constructor() {
    private val _quizzes = MutableStateFlow<List<Quiz>>(emptyList())
    val quizzes: StateFlow<List<Quiz>> = _quizzes.asStateFlow()

    private val _needsRefresh = MutableStateFlow(false)
    val needsRefresh: StateFlow<Boolean> = _needsRefresh.asStateFlow()

    fun updateQuizzes(newQuizzes: List<Quiz>) {
        _quizzes.value = newQuizzes
    }

    fun markForRefresh() {
        _needsRefresh.value = true
    }

    fun acknowledgeRefresh() {
        _needsRefresh.value = false
    }

    fun clearQuizzes() {
        _quizzes.value = emptyList()
        _needsRefresh.value = true
    }
}