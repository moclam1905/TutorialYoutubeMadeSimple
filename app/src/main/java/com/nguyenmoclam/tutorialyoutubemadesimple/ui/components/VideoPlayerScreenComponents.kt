package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.TranscriptSegment
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.ChapterNavigation as ChapterNavigationChips
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight

@Composable
fun VideoPlayerComponent(
    videoId: String?,
    videoUrl: String,
    onPlayerReady: (YouTubePlayer) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .height(240.dp) // Maintain original height
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant), // Add background color
        contentAlignment = Alignment.Center
    ) {
        if (videoId == null) {
            Text(stringResource(R.string.error_extracting_video_id, videoUrl))
        } else {
            AndroidView(
                factory = {
                    YouTubePlayerView(context).apply {
                        addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                onPlayerReady(youTubePlayer)
                                // Load video when player is ready
                                youTubePlayer.loadVideo(videoId, 0f)
                            }
                        })
                    }
                },
                modifier = Modifier.fillMaxSize() // Fill the Box
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Added OptIn here
@Composable
fun ChapterNavigationSection(
    chapters: List<TranscriptSegment>,
    currentChapter: TranscriptSegment?,
    currentTimeMillis: Long,
    totalDurationMillis: Long,
    onChapterClick: (TranscriptSegment) -> Unit,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Current chapter title
        currentChapter?.let { chapter ->
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
                    text = chapter.chapterTitle
                        ?: "Chapter @ ${chapter.timestamp}", // Fallback text
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Integrated Navigation Bar with Previous/Next buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth() // Use fillMaxWidth here
                .padding(horizontal = 8.dp, vertical = 4.dp) // Adjust padding
        ) {
            IconButton(onClick = onPreviousChapter, modifier = Modifier.size(40.dp)) { // Slightly smaller touch target if needed
                Icon(
                    imageVector = Icons.Default.NavigateBefore,
                    contentDescription = stringResource(R.string.previous_chapter),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            ChapterNavigationBar( // The progress bar style navigation from components package
                chapters = chapters,
                currentTimeMillis = currentTimeMillis,
                totalDurationMillis = totalDurationMillis,
                onChapterClick = onChapterClick,
                modifier = Modifier
                    .weight(1f) // Takes the available space between buttons
                    .padding(horizontal = 8.dp) // Keep padding around the bar
            )
            IconButton(onClick = onNextChapter, modifier = Modifier.size(40.dp)) { // Slightly smaller touch target if needed
                Icon(
                    imageVector = Icons.Default.NavigateNext,
                    contentDescription = stringResource(R.string.next_chapter),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Chapter Chips (Scrollable) - Uses alias ChapterNavigationChips
        ChapterNavigationChips(
            chapters = chapters,
            currentTimeMillis = currentTimeMillis,
            onChapterClick = onChapterClick,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
        )
    }
}

@Composable
fun TabAndSearchComponent(
    selectedTabIndex: Int,
    isSearchVisible: Boolean,
    onTabSelected: (Int) -> Unit,
    onToggleSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier
                .weight(1f)
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { onTabSelected(0) },
                text = { Text(stringResource(R.string.transcript)) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { onTabSelected(1) },
                text = { Text(stringResource(R.string.chapters)) }
            )
        }
        IconButton(
            onClick = onToggleSearch
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.toggle_search),
                tint = if (isSearchVisible) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SearchBarComponent(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onSearchAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth() // Ensure it fills width for consistent padding application
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text(stringResource(R.string.search_in_transcript)) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null) // Decorative
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = onClearQuery) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = stringResource(R.string.clear_search)
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearchAction() })
    )
}

@Composable
fun ContentArea(
    selectedTabIndex: Int,
    searchQuery: String,
    filteredSegments: List<TranscriptSegment>,
    allSegments: List<TranscriptSegment>,
    chapters: List<TranscriptSegment>,
    currentTimeMillis: Long,
    onSegmentClick: (TranscriptSegment) -> Unit,
    onChapterClick: (TranscriptSegment) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.padding(top = 8.dp)) { // Add some top padding if needed, keep horizontal padding within AnimatedContent
        AnimatedContent(
            targetState = Triple(selectedTabIndex, searchQuery.isNotEmpty(), filteredSegments.isEmpty()),
            transitionSpec = {
                // Define transitions: Fade in/out, maybe slight slide
                if (initialState.first != targetState.first) {
                    // Tab change transition
                    slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut()
                } else {
                    // Search state change transition (usually faster)
                    fadeIn(animationSpec = androidx.compose.animation.core.tween(200)) togetherWith
                            fadeOut(animationSpec = androidx.compose.animation.core.tween(200))
                }.using(
                    // Ensure size changes smoothly
                    SizeTransform(clip = false)
                )
            },
            label = "ContentAreaAnimation" // Add label for debugging
        ) { targetState ->
            val (currentTab, isSearching, isFilterEmpty) = targetState
            val currentModifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp) // Apply horizontal padding inside AnimatedContent

            when {
                // Search active and has results
                isSearching && !isFilterEmpty -> {
                    TranscriptList(
                        segments = filteredSegments,
                        currentTimeMillis = currentTimeMillis,
                        onSegmentClick = onSegmentClick,
                        modifier = currentModifier
                    )
                }
                // Search active but no results
                isSearching && isFilterEmpty -> {
                    Box(
                        modifier = currentModifier,
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { // Center content vertically
                            Icon(
                                imageVector = Icons.Default.SentimentVeryDissatisfied,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp).padding(bottom = 8.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.no_results_found, searchQuery),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant // Softer color
                            )
                        }
                    }
                }
                // No search, show content based on tab
                else -> {
                    when (currentTab) {
                        0 -> { // Transcript Tab
                            TranscriptList(
                                segments = allSegments.filter { !it.isChapterStart },
                                currentTimeMillis = currentTimeMillis,
                                onSegmentClick = onSegmentClick,
                                modifier = currentModifier
                            )
                        }
                        1 -> { // Chapters Tab
                            TranscriptList(
                                segments = chapters,
                                currentTimeMillis = currentTimeMillis,
                                onSegmentClick = onChapterClick,
                                modifier = currentModifier
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Utility Function ---

fun extractVideoId(fullUrl: String): String? {
    // Regex to find YouTube video ID from various URL formats
    val patterns = listOf(
        Regex("""(?:https?://)?(?:www\.)?youtube\.com/watch\?v=([a-zA-Z0-9_-]{11})"""),
        Regex("""(?:https?://)?(?:www\.)?youtu\.be/([a-zA-Z0-9_-]{11})"""),
        Regex("""(?:https?://)?(?:www\.)?youtube\.com/embed/([a-zA-Z0-9_-]{11})"""),
        Regex("""(?:https?://)?(?:www\.)?youtube\.com/v/([a-zA-Z0-9_-]{11})""")
    )
    for (pattern in patterns) {
        val match = pattern.find(fullUrl)
        if (match != null && match.groupValues.size > 1) {
            return match.groupValues[1] // The video ID is in the first capture group
        }
    }
    // Fallback for simpler cases or if embedded directly
    val simpleMatch = Regex("(?:v=|/|embed/)([a-zA-Z0-9_-]{11})").find(fullUrl)
    return simpleMatch?.groups?.get(1)?.value
}
