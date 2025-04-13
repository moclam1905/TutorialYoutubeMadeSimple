package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.tag.Tag
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.GetQuizByIdUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.UpdateQuizSettingsUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.tag.CreateOrGetTagUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.tag.GetAllTagsUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.tag.GetTagsForQuizUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.tag.UpdateTagsForQuizUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizSettingUiState(
    val isLoading: Boolean = true,
    val quiz: Quiz? = null,
    val initialQuiz: Quiz? = null, // To track original state for reset/dirty check
    val allTags: List<Tag> = emptyList(),
    val selectedTags: List<Tag> = emptyList(),
    val initialSelectedTags: List<Tag> = emptyList(), // To track original state
    val errorMessage: String? = null,
    val showExitConfirmation: Boolean = false,
    val saveSuccess: Boolean = false
) {
    val hasUnsavedChanges: Boolean
        get() = quiz != initialQuiz || selectedTags.toSet() != initialSelectedTags.toSet()
}

@HiltViewModel
class QuizSettingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getQuizByIdUseCase: GetQuizByIdUseCase,
    private val updateQuizSettingsUseCase: UpdateQuizSettingsUseCase,
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val getTagsForQuizUseCase: GetTagsForQuizUseCase,
    private val updateTagsForQuizUseCase: UpdateTagsForQuizUseCase,
    private val createOrGetTagUseCase: CreateOrGetTagUseCase,
    private val reminderScheduler: ReminderScheduler // Inject ReminderScheduler
) : ViewModel() {

    private val quizId: Long = savedStateHandle.get<String>("quizId")?.toLongOrNull() ?: -1L

    private val _uiState = MutableStateFlow(QuizSettingUiState())
    val uiState: StateFlow<QuizSettingUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        if (quizId == -1L) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Invalid Quiz ID") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Load quiz details
                val quiz = getQuizByIdUseCase(quizId)
                if (quiz == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Quiz not found") }
                    return@launch
                }

                // Load tags in parallel
                val allTagsFlow = getAllTagsUseCase()
                val selectedTagsFlow = getTagsForQuizUseCase(quizId)

                // Combine flows - wait for both to emit first value
                combine(allTagsFlow, selectedTagsFlow) { all, selected ->
                    Pair(all, selected)
                }.collectLatest { (all, selected) -> // Use collectLatest to handle rapid updates if needed
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            quiz = quiz,
                            initialQuiz = quiz, // Store initial state
                            allTags = all,
                            selectedTags = selected,
                            initialSelectedTags = selected // Store initial state
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Error loading data") }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { state ->
            state.quiz?.let {
                state.copy(quiz = it.copy(title = newTitle))
            } ?: state
        }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { state ->
            state.quiz?.let {
                state.copy(quiz = it.copy(description = newDescription))
            } ?: state
        }
    }

    fun onReminderIntervalChange(intervalMillis: Long?) {
        _uiState.update { state ->
            state.quiz?.let {
                // Ensure null or positive value
                val validInterval = if (intervalMillis != null && intervalMillis <= 0) null else intervalMillis
                state.copy(quiz = it.copy(reminderInterval = validInterval))
            } ?: state
        }
    }

    fun onTagSelected(tag: Tag) {
        _uiState.update { state ->
            if (!state.selectedTags.contains(tag)) {
                state.copy(selectedTags = state.selectedTags + tag)
            } else {
                state // No change if already selected
            }
        }
    }

    fun onTagDeselected(tag: Tag) {
        _uiState.update { state ->
            state.copy(selectedTags = state.selectedTags - tag)
        }
    }

    fun createAndSelectTag(tagName: String) {
        viewModelScope.launch {
            val newTag = createOrGetTagUseCase(tagName)
            if (newTag != null) {
                // Add to allTags if it's truly new (optional, depends on UI needs)
                // Select the new/existing tag
                _uiState.update { state ->
                    val updatedAllTags = if (state.allTags.none { it.id == newTag.id }) state.allTags + newTag else state.allTags
                    val updatedSelectedTags = if (state.selectedTags.none { it.id == newTag.id }) state.selectedTags + newTag else state.selectedTags
                    state.copy(
                        allTags = updatedAllTags.distinctBy { it.id }.sortedBy { it.name },
                        selectedTags = updatedSelectedTags.distinctBy { it.id }
                    )
                }
            } else {
                // Handle tag creation failure (e.g., show error message)
                _uiState.update { it.copy(errorMessage = "Failed to create tag '$tagName'") }
            }
        }
    }


    fun saveSettings() {
        val currentState = _uiState.value
        val currentQuiz = currentState.quiz
        if (currentQuiz == null || !currentState.hasUnsavedChanges) {
            return // Nothing to save or quiz not loaded
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, saveSuccess = false) }
            try {
                // Save basic settings
                updateQuizSettingsUseCase(
                    quizId = currentQuiz.id,
                    title = currentQuiz.title,
                    description = currentQuiz.description,
                    reminderInterval = currentQuiz.reminderInterval
                )
                // Save tags
                updateTagsForQuizUseCase(quizId, currentState.selectedTags)

                // Schedule or cancel reminder based on the new interval
                val reminderInterval = currentQuiz.reminderInterval
                if (reminderInterval != null && reminderInterval > 0) {
                    reminderScheduler.scheduleReminder(currentQuiz.id, currentQuiz.title, reminderInterval)
                } else {
                    reminderScheduler.cancelReminder(currentQuiz.id)
                }

                // Update initial state to reflect saved changes
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        initialQuiz = currentQuiz,
                        initialSelectedTags = currentState.selectedTags,
                        saveSuccess = true // Indicate success briefly
                    )
                }
                // Reset save success flag after a short delay (optional)
                kotlinx.coroutines.delay(1500)
                _uiState.update { it.copy(saveSuccess = false) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Error saving settings") }
            }
        }
    }

    fun resetSettings() {
        _uiState.update {
            it.copy(
                quiz = it.initialQuiz,
                selectedTags = it.initialSelectedTags
            )
        }
    }

    fun attemptToGoBack() {
        if (_uiState.value.hasUnsavedChanges) {
            _uiState.update { it.copy(showExitConfirmation = true) }
        } else {
            // Allow back navigation immediately
            // Navigation logic should be handled by the UI observing a different state/event
            // For now, just clear the flag if it was somehow set
             _uiState.update { it.copy(showExitConfirmation = false) }
        }
    }

    fun confirmExit() {
        _uiState.update { it.copy(showExitConfirmation = false) }
        // UI should observe this change and navigate back
    }

    fun cancelExit() {
        _uiState.update { it.copy(showExitConfirmation = false) }
    }

     fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
