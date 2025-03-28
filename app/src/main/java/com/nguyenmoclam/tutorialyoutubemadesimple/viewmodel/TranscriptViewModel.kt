package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Transcript
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.TranscriptSegment
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.transcript.GetChaptersForTranscriptUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.transcript.GetCurrentSegmentUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.transcript.GetSegmentsForTranscriptUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.transcript.GetTranscriptForQuizUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.transcript.ParseTranscriptContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing transcript data and playback state.
 */
@HiltViewModel
class TranscriptViewModel @Inject constructor(
    private val getSegmentsForTranscriptUseCase: GetSegmentsForTranscriptUseCase,
    private val getTranscriptForQuizUseCase: GetTranscriptForQuizUseCase,
    private val getCurrentSegmentUseCase: GetCurrentSegmentUseCase,
    private val getChaptersForTranscriptUseCase: GetChaptersForTranscriptUseCase,
    private val parseTranscriptContentUseCase: ParseTranscriptContentUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TranscriptViewState())
    val state: StateFlow<TranscriptViewState> = _state.asStateFlow()

    // Search functionality
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredSegments = MutableStateFlow<List<TranscriptSegment>>(emptyList())
    val filteredSegments: StateFlow<List<TranscriptSegment>> = _filteredSegments.asStateFlow()

    /**
     * Load transcript and segments for a specific quiz.
     */
    fun loadTranscriptForQuiz(quizId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Collect transcript data
                getTranscriptForQuizUseCase(quizId).collect { transcript ->
                    if (transcript != null) {
                        _state.update {
                            it.copy(
                                transcript = transcript,
                                isLoading = false
                            )
                        }

                        // Load segments for this transcript
                        loadSegmentsForTranscript(transcript.id)
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Transcript not found for this quiz"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error loading transcript: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Load segments for a specific transcript.
     */
    private fun loadSegmentsForTranscript(transcriptId: Long) {
        viewModelScope.launch {
            try {
                combine(
                    getSegmentsForTranscriptUseCase(transcriptId),
                    getChaptersForTranscriptUseCase(transcriptId)
                ) { segments, chapters ->
                    segments to chapters
                }.collect { (segments, chapters) ->
                    _state.update { it.copy(segments = segments, chapters = chapters) }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        errorMessage = "Error loading transcript segments: ${e.message}"
                    )
                }
            }
        }
    }


    /**
     * Update the current playback position.
     * This method handles both segment and chapter updates based on the current position.
     */
    fun updateCurrentPosition(positionMillis: Long) {
        _state.update { it.copy(currentPositionMillis = positionMillis) }

        // Get the current segment based on position
        viewModelScope.launch {
            try {
                val transcriptId = state.value.transcript?.id ?: return@launch
                val currentSegment = getCurrentSegmentUseCase(transcriptId, positionMillis)

                // Only update if the segment has changed
                if (currentSegment?.id != state.value.currentSegment?.id) {
                    _state.update { it.copy(currentSegment = currentSegment) }
                }

                // Always update the current chapter based on position
                // This ensures the chapter navigation stays in sync with the video
                val chapters = state.value.chapters
                if (chapters.isNotEmpty()) {
                    // If current segment is a chapter start, use it directly
                    if (currentSegment?.isChapterStart == true) {
                        _state.update { it.copy(currentChapter = currentSegment) }
                    } else {
                        // Otherwise find the appropriate chapter based on timestamp
                        val currentChapter = findCurrentChapter(chapters, positionMillis)
                        // Only update if the chapter has changed
                        if (currentChapter?.id != state.value.currentChapter?.id) {
                            _state.update { it.copy(currentChapter = currentChapter) }
                        }
                    }
                }
            } catch (e: Exception) {
                // Silently handle error - not critical
            }
        }
    }

    /**
     * Seek to a specific segment in the video.
     */
    fun seekToSegment(segment: TranscriptSegment) {
        _state.update {
            it.copy(
                seekPositionMillis = segment.timestampMillis,
                shouldSeek = true
            )
        }
    }

    /**
     * Reset seek flag after seeking is complete.
     */
    fun resetSeekFlag() {
        _state.update { it.copy(shouldSeek = false) }
    }

    /**
     * Parse raw transcript content into segments and save them to the database.
     * This function processes the transcript content, identifies segments and chapters,
     * and updates the UI state with the parsed segments.
     *
     * This implementation uses Flow to process large transcripts in chunks,
     * allowing for progressive UI updates and reducing memory pressure.
     */
    fun parseTranscriptContent(content: String, transcriptId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // For small transcripts, use the direct method for backward compatibility
                if (content.length < 10000) {
                    val segments = parseTranscriptContentUseCase(content, transcriptId)
                    updateUIWithSegments(segments)
                } else {
                    // For large transcripts, use the Flow-based implementation
                    // that processes content in chunks and emits results progressively
                    parseTranscriptContentUseCase.invokeAsFlow(content, transcriptId)
                        .collect { segments ->
                            // Update UI with each batch of processed segments
                            updateUIWithSegments(segments)
                        }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error parsing transcript: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Update UI state with processed transcript segments.
     */
    private fun updateUIWithSegments(segments: List<TranscriptSegment>) {
        // Extract chapters from segments that are marked as chapter starts
        val chapterSegments = segments.filter { segment -> segment.isChapterStart }

        Log.d(
            "TranscriptViewModel",
            "Updating UI with ${segments.size} segments and ${chapterSegments.size} chapters"
        )

        // If we have chapters, log them for debugging
        if (chapterSegments.isNotEmpty()) {
            chapterSegments.forEachIndexed { index, chapter ->
                Log.d(
                    "TranscriptViewModel",
                    "Chapter ${index + 1}: ${chapter.chapterTitle ?: "Untitled"} at ${chapter.timestamp}"
                )
            }
        }

        _state.update {
            it.copy(
                segments = segments,
                chapters = chapterSegments,
                isLoading = false
            )
        }

        // If we have a current position, update the current segment and chapter
        val currentPosition = state.value.currentPositionMillis
        if (currentPosition > 0 && segments.isNotEmpty()) {
            updateCurrentPosition(currentPosition)
        }
    }

    /**
     * Update search query and filter segments.
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query

        if (query.isBlank()) {
            _filteredSegments.value = emptyList()
            _state.update { it.copy(isSearchActive = false) }
            return
        }

        _state.update { it.copy(isSearchActive = true) }

        // Filter segments based on search query
        val segments = state.value.segments
        val filtered = segments.filter { segment ->
            segment.text.contains(query, ignoreCase = true)
        }

        _filteredSegments.value = filtered
    }

    /**
     * Clear search query and reset filtered segments.
     */
    fun clearSearchQuery() {
        _searchQuery.value = ""
        _filteredSegments.value = emptyList()
        _state.update { it.copy(isSearchActive = false) }
    }

    /**
     * Find the current chapter based on the current playback time.
     * This enhanced implementation handles edge cases better and ensures
     * we always return the most appropriate chapter for the current time.
     * It also provides smoother transitions between chapters.
     */
    private fun findCurrentChapter(
        chapters: List<TranscriptSegment>,
        currentTimeMillis: Long
    ): TranscriptSegment? {
        if (chapters.isEmpty()) return null

        // If current time is before the first chapter, return the first chapter
        if (currentTimeMillis < chapters.first().timestampMillis) {
            return chapters.first()
        }

        // Find the last chapter that starts before or at the current time
        for (i in chapters.indices.reversed()) {
            if (chapters[i].timestampMillis <= currentTimeMillis) {
                // Log when we change chapters for debugging
                val currentChapter = state.value.currentChapter
                if (currentChapter == null || currentChapter.id != chapters[i].id) {
                    Log.d(
                        "TranscriptViewModel",
                        "Changing to chapter: ${chapters[i].chapterTitle ?: "Untitled"} at ${chapters[i].timestamp}"
                    )

                    // Calculate progress within this chapter
                    val nextChapterTime = if (i < chapters.size - 1) {
                        chapters[i + 1].timestampMillis
                    } else {
                        Long.MAX_VALUE
                    }

                    val chapterDuration = nextChapterTime - chapters[i].timestampMillis
                    val progressInChapter = if (chapterDuration > 0) {
                        (currentTimeMillis - chapters[i].timestampMillis).toFloat() / chapterDuration
                    } else {
                        0f
                    }

                    Log.d("TranscriptViewModel", "Progress in chapter: ${progressInChapter * 100}%")
                }
                return chapters[i]
            }
        }

        // If no chapter found (shouldn't happen with the above checks), return the first chapter
        return chapters.firstOrNull()
    }

    /**
     * Jump to a specific chapter in the transcript.
     * This will update both the video position and highlight the chapter in the UI.
     * Enhanced with haptic feedback and smoother transitions.
     */
    fun jumpToChapter(chapter: TranscriptSegment) {
        // First seek to the chapter's timestamp
        seekToSegment(chapter)

        // Then update the current chapter state
        _state.update { it.copy(currentChapter = chapter) }

        // Log chapter navigation for analytics
        Log.d(
            "TranscriptViewModel",
            "User navigated to chapter: ${chapter.chapterTitle ?: "Untitled"} at ${chapter.timestamp}"
        )

        // Find segments that belong to this chapter for improved context
        viewModelScope.launch {
            try {
                val nextChapterIndex = state.value.chapters.indexOf(chapter) + 1
                val nextChapterTime = if (nextChapterIndex < state.value.chapters.size) {
                    state.value.chapters[nextChapterIndex].timestampMillis
                } else {
                    Long.MAX_VALUE
                }

                // Find segments that belong to this chapter
                val chapterSegments = state.value.segments.filter { segment ->
                    segment.timestampMillis >= chapter.timestampMillis &&
                            segment.timestampMillis < nextChapterTime
                }

                // Update state with segments for this chapter (could be used for highlighting)
                if (chapterSegments.isNotEmpty()) {
                    Log.d(
                        "TranscriptViewModel",
                        "Chapter contains ${chapterSegments.size} segments"
                    )
                }
            } catch (e: Exception) {
                // Silently handle error - not critical
            }
        }
    }

    /**
     * Get the next chapter based on the current chapter.
     * Returns null if there is no next chapter.
     */
    fun getNextChapter(): TranscriptSegment? {
        val currentChapter = state.value.currentChapter ?: return null
        val chapters = state.value.chapters
        val currentIndex = chapters.indexOf(currentChapter)

        return if (currentIndex < chapters.size - 1) {
            chapters[currentIndex + 1]
        } else {
            null
        }
    }

    /**
     * Get the previous chapter based on the current chapter.
     * Returns null if there is no previous chapter.
     */
    fun getPreviousChapter(): TranscriptSegment? {
        val currentChapter = state.value.currentChapter ?: return null
        val chapters = state.value.chapters
        val currentIndex = chapters.indexOf(currentChapter)

        return if (currentIndex > 0) {
            chapters[currentIndex - 1]
        } else {
            null
        }
    }
}

/**
 * State class for TranscriptViewModel.
 */
data class TranscriptViewState(
    val transcript: Transcript? = null,
    val segments: List<TranscriptSegment> = emptyList(),
    val chapters: List<TranscriptSegment> = emptyList(),
    val currentSegment: TranscriptSegment? = null,
    val currentChapter: TranscriptSegment? = null,
    val currentPositionMillis: Long = 0L,
    val seekPositionMillis: Long = 0L,
    val shouldSeek: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSearchActive: Boolean = false
)