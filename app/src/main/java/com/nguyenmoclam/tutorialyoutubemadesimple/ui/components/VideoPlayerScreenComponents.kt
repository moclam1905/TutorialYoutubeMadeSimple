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
            .fillMaxWidth(),
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

        // Navigation Bar with Previous/Next buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp) // Padding for buttons
        ) {
            IconButton(onClick = onPreviousChapter) {
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
                    .weight(1f)
                    .padding(horizontal = 8.dp) // Space between buttons and bar
            )
            IconButton(onClick = onNextChapter) {
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
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.weight(1f)
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
        IconButton(onClick = onToggleSearch) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.toggle_search),
                tint = if (isSearchVisible) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant // Use a less prominent color when inactive
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
    Box(modifier = modifier.padding(horizontal = 16.dp)) { // Apply horizontal padding once here
        when {
            // Search active and has results
            searchQuery.isNotEmpty() && filteredSegments.isNotEmpty() -> {
                TranscriptList( // Assumes TranscriptList is in components package
                    segments = filteredSegments,
                    currentTimeMillis = currentTimeMillis,
                    onSegmentClick = onSegmentClick, // Search results use segment click logic
                    modifier = Modifier.fillMaxSize() // Fill the Box
                )
            }
            // Search active but no results
            searchQuery.isNotEmpty() && filteredSegments.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(), // Fill the Box
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_results_found, searchQuery),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            // No search, show content based on tab
            else -> {
                when (selectedTabIndex) {
                    0 -> { // Transcript Tab
                        TranscriptList( // Assumes TranscriptList is in components package
                            // Filter out chapter markers from the main transcript view
                            segments = allSegments.filter { !it.isChapterStart },
                            currentTimeMillis = currentTimeMillis,
                            onSegmentClick = onSegmentClick,
                            modifier = Modifier.fillMaxSize() // Fill the Box
                        )
                    }

                    1 -> { // Chapters Tab
                        TranscriptList( // Assumes TranscriptList is in components package
                            segments = chapters,
                            currentTimeMillis = currentTimeMillis,
                            onSegmentClick = onChapterClick, // Chapters use chapter click logic
                            modifier = Modifier.fillMaxSize() // Fill the Box
                        )
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
