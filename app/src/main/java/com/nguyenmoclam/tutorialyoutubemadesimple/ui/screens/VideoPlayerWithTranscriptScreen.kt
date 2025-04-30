package com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.ChapterNavigationSection
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.ContentArea
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.TabAndSearchComponent
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.VideoPlayerComponent
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.extractVideoId
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.TranscriptViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import kotlinx.coroutines.delay

/**
 * Screen for displaying a video player with transcript and chapter navigation,
 * using android-youtube-player instead of ExoPlayer.
 */
@Composable
fun VideoPlayerWithTranscriptScreen(
    quizId: Long,
    videoUrl: String,
    modifier: Modifier = Modifier,
    viewModel: TranscriptViewModel = hiltViewModel()
) {
    // 1) Current states
    val state by viewModel.state.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredSegments by viewModel.filteredSegments.collectAsState()

    // Tab selection state
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Player position tracking (update from tracker)
    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }

    // YouTubePlayer instance + tracker
    var youTubePlayerInstance by remember { mutableStateOf<YouTubePlayer?>(null) }
    val tracker = remember { YouTubePlayerTracker() }
    val focusManager = LocalFocusManager.current

    // Load transcript data when screen opens
    LaunchedEffect(quizId) {
        viewModel.loadTranscriptForQuiz(quizId)
        // Trigger parsing if transcript loaded but segments are empty
        // This check might need refinement based on actual state flow
        val currentTranscript = viewModel.state.value.transcript
        if (currentTranscript != null && viewModel.state.value.segments.isEmpty() && currentTranscript.content.isNotEmpty()) {
            viewModel.parseTranscriptContent(currentTranscript.content, currentTranscript.id)
        }
    }

    // Track current position and update ViewModel
    LaunchedEffect(tracker) {
        while (true) {
            val curSec = tracker.currentSecond
            val durSec = tracker.videoDuration

            val newPosition = (curSec * 1000).toLong()
            val newDuration = (durSec * 1000).toLong()

            if (newPosition != currentPosition) {
                currentPosition = newPosition
                viewModel.updateCurrentPosition(currentPosition)
            }
            if (newDuration != totalDuration) {
                totalDuration = newDuration
            }

            delay(500) // Check every 500ms
        }
    }

    // Handle seek action from ViewModel
    LaunchedEffect(state.shouldSeek, state.seekPositionMillis, youTubePlayerInstance) {
        if (state.shouldSeek && youTubePlayerInstance != null) {
            val seekTargetSeconds = state.seekPositionMillis / 1000f
            youTubePlayerInstance?.seekTo(seekTargetSeconds)
            viewModel.resetSeekFlag() // Reset flag after seeking
        }
    }

    // Lifecycle management for the player view is handled by AndroidView
    DisposableEffect(Unit) {
        onDispose {
            // YouTubePlayerView handles its own lifecycle, including releasing the player.
            // No explicit player release needed here unless specific cleanup is required.
            // youTubePlayerInstance?.pause() // Optional: pause if needed on screen disposal
        }
    }


    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- Video Player Section ---
            val videoId = remember(videoUrl) { extractVideoId(videoUrl) }
            VideoPlayerComponent(
                videoId = videoId,
                videoUrl = videoUrl, // Pass original URL for error message
                onPlayerReady = { player ->
                    youTubePlayerInstance = player
                    player.addListener(tracker)
                },
                modifier = Modifier.fillMaxWidth()
            )

            // --- Chapter Navigation Section ---
            if (state.chapters.isNotEmpty() && totalDuration > 0) {
                ChapterNavigationSection(
                    chapters = state.chapters,
                    currentChapter = state.currentChapter,
                    currentTimeMillis = currentPosition,
                    totalDurationMillis = totalDuration,
                    onChapterClick = { viewModel.jumpToChapter(it) },
                    onPreviousChapter = {
                        viewModel.getPreviousChapter()?.let { viewModel.jumpToChapter(it) }
                    },
                    onNextChapter = {
                        viewModel.getNextChapter()?.let { viewModel.jumpToChapter(it) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // --- Tabs and Integrated Search ---
            TabAndSearchComponent(
                selectedTabIndex = selectedTabIndex,
                searchQuery = searchQuery,
                onTabSelected = { selectedTabIndex = it },
                onQueryChange = viewModel::updateSearchQuery,
                onClearQuery = viewModel::clearSearchQuery,
                onSearchAction = { focusManager.clearFocus() },
                modifier = Modifier.fillMaxWidth()
            )

            // --- Content Area (Transcript/Chapters/Search Results) ---
            ContentArea(
                selectedTabIndex = selectedTabIndex,
                searchQuery = searchQuery,
                filteredSegments = filteredSegments,
                allSegments = state.segments,
                chapters = state.chapters,
                currentTimeMillis = currentPosition,
                onSegmentClick = { viewModel.seekToSegment(it) },
                onChapterClick = { viewModel.jumpToChapter(it) }, // Use jump for chapters
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Takes remaining space
            )
        }
    }
}
