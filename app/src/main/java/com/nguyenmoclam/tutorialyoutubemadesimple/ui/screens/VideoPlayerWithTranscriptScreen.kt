package com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.TranscriptSegment
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.ChapterNavigationBar
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.TranscriptList
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.LocalNetworkStateListener
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkSnackbarManager
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.TranscriptViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.delay
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.ChapterNavigation as ChapterNavigationComponent

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

    // Network state
    val networkStateListener = LocalNetworkStateListener.current

    // SnackbarHostState is used to display a notification when the network connection is restored
    val snackbarHostState = remember { SnackbarHostState() }

    // Tab selection state
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Search state
    var isSearchVisible by remember { mutableStateOf(false) }

    // Player position tracking (update from tracker)
    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }

    // YouTubePlayer instance + tracker
    var youTubePlayerInstance by remember { mutableStateOf<YouTubePlayer?>(null) }
    val tracker = remember { YouTubePlayerTracker() }

    // Load transcript data when screen opens
    LaunchedEffect(quizId) {
        viewModel.loadTranscriptForQuiz(quizId)
        val transcript = state.transcript
        if (transcript != null && state.segments.isEmpty() && transcript.content.isNotEmpty()) {
            viewModel.parseTranscriptContent(transcript.content, transcript.id)
        }
    }

    // Track current position and update ViewModel
    // (Every 500ms, we get tracker.currentSecond => update ViewModel, etc.)
    LaunchedEffect(Unit) {
        while (true) {
            // currentSecond is Float (unit: seconds)
            val curSec = tracker.currentSecond
            val durSec = tracker.videoDuration // Float

            // Update Compose state
            currentPosition = (curSec * 1000).toLong()
            totalDuration = (durSec * 1000).toLong()

            // Send to ViewModel
            viewModel.updateCurrentPosition(currentPosition)

            delay(500) // 500ms
        }
    }

    // Handle seek action => YouTubePlayer.seekTo(float seconds)
    LaunchedEffect(state.shouldSeek) {
        if (state.shouldSeek && youTubePlayerInstance != null) {
            val seekTargetSeconds = state.seekPositionMillis / 1000f
            youTubePlayerInstance?.seekTo(seekTargetSeconds)
            viewModel.resetSeekFlag()
        }
    }

    // Lifecycle: No need to release ExoPlayer,
    // but if you want to stop the player onDispose, you can
    DisposableEffect(Unit) {
        onDispose {
            // Optionally remove listeners, etc.
            // youTubePlayerInstance?.pause() => Not required
        }
    }

    // Show the notification when the network connection changes
    NetworkSnackbarManager.NetworkStatusSnackbar(
        snackbarHostState = snackbarHostState,
        networkStateListener = networkStateListener,
        showReconnectionMessage = true,
        showDisconnectionMessage = true
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Video player region
            // Extract videoId = 11 characters
            val videoId = remember(videoUrl) { extractVideoId(videoUrl) }

            // Removed GlobalNetworkAwareContent wrapper
            // Content is now directly inside the main Column
            Column(modifier = Modifier.fillMaxWidth()) {
                // Video player region
                if (videoId == null) {
                    // Show error
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.error_extracting_video_id, videoUrl))
                    }
                } else {
                    // YouTube Player View
                    AndroidView(
                        factory = { context ->
                            YouTubePlayerView(context).apply {
                                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                                    override fun onReady(youTubePlayer: YouTubePlayer) {
                                        // Save instance + attach tracker
                                        youTubePlayerInstance = youTubePlayer
                                        youTubePlayer.addListener(tracker)

                                        // Load video
                                        youTubePlayer.loadVideo(videoId, 0f)
                                    }
                                })
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    )
                }

                // Chapter Navigation Section with improved layout
                if (state.chapters.isNotEmpty() && totalDuration > 0) {
                    // Current chapter title with animation
                    state.currentChapter?.let { currentChapter ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = currentChapter.chapterTitle
                                    ?: "Chapter ${currentChapter.timestamp}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Enhanced Chapter Navigation Bar with Previous/Next buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Previous Chapter Button
                        IconButton(onClick = {
                            viewModel.getPreviousChapter()?.let { prevChapter ->
                                viewModel.jumpToChapter(prevChapter)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.NavigateBefore,
                                contentDescription = "Previous Chapter",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Chapter Navigation Bar
                        ChapterNavigationBar(
                            chapters = state.chapters,
                            currentTimeMillis = currentPosition,
                            totalDurationMillis = totalDuration,
                            onChapterClick = { chapter ->
                                viewModel.jumpToChapter(chapter)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )

                        // Next Chapter Button
                        IconButton(onClick = {
                            viewModel.getNextChapter()?.let { nextChapter ->
                                viewModel.jumpToChapter(nextChapter)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.NavigateNext,
                                contentDescription = "Next Chapter",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Chapter Navigation (chips) with horizontal scrolling
                    ChapterNavigationComponent(
                        chapters = state.chapters,
                        currentTimeMillis = currentPosition,
                        onChapterClick = { chapter: TranscriptSegment ->
                            viewModel.jumpToChapter(chapter)
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp)
                    )
                }
            }


            // Tab row + Search button
            Row(modifier = Modifier.fillMaxWidth()) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.weight(1f)
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text(stringResource(R.string.transcript)) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text(stringResource(R.string.chapters)) }
                    )
                }

                IconButton(onClick = {
                    isSearchVisible = !isSearchVisible
                    if (!isSearchVisible) {
                        viewModel.clearSearchQuery()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Toggle Search",
                        tint = if (isSearchVisible) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Search bar
            if (isSearchVisible) {
                val focusManager = LocalFocusManager.current

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text(stringResource(R.string.search_in_transcript)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearSearchQuery() }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                )

                if (filteredSegments.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.results_found, filteredSegments.size),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Content area
            if (searchQuery.isNotEmpty() && filteredSegments.isNotEmpty()) {
                // Show search results
                TranscriptList(
                    segments = filteredSegments,
                    currentTimeMillis = currentPosition,
                    onSegmentClick = { segment ->
                        viewModel.seekToSegment(segment)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                )
            } else if (searchQuery.isNotEmpty() && filteredSegments.isEmpty()) {
                // No search results
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_results_found, searchQuery),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                // Normal content (Transcript / Chapters)
                when (selectedTabIndex) {
                    0 -> {
                        // Only Transcript (excluding chapters)
                        TranscriptList(
                            segments = state.segments.filter { !it.isChapterStart },
                            currentTimeMillis = currentPosition,
                            onSegmentClick = { segment ->
                                viewModel.seekToSegment(segment)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                        )
                    }

                    1 -> {
                        // Chapters Only
                        TranscriptList(
                            segments = state.chapters,
                            currentTimeMillis = currentPosition,
                            onSegmentClick = { segment ->
                                viewModel.jumpToChapter(segment)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

fun extractVideoId(fullUrl: String): String? {
    val regex = Regex("(?:v=|/)([0-9A-Za-z_-]{11}).*")
    val match = regex.find(fullUrl)
    return match?.groups?.get(1)?.value
}
