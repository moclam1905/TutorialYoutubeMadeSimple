package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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

enum class SearchState { Collapsed, Expanded }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun TabAndSearchComponent(
    selectedTabIndex: Int,
    searchQuery: String,
    onTabSelected: (Int) -> Unit,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onSearchAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    var state by rememberSaveable { mutableStateOf(SearchState.Collapsed) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    // A single transition controls everything
    val transition = updateTransition(targetState = state, label = "SearchTransition")

    val horizontalPadding by transition.animateDp(
        label = "HorizontalPadding",
        transitionSpec = { tween(durationMillis = 300) }
    ) { s -> if (s == SearchState.Expanded) 0.dp else 8.dp }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            // Animate child size changes (TabRow ↔ TextField)
            .animateContentSize(tween(300)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = horizontalPadding)
                .height(56.dp), // Keep row height fixed
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left part: Tabs or TextField
            AnimatedContent(
                targetState = state,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    // Slide + fade in opposite directions for the two states
                    (slideInHorizontally { it / 2 } + fadeIn() with
                            slideOutHorizontally { -it / 2 } + fadeOut())
                        .using(SizeTransform(clip = false))
                },
                label = "Tabs<->TextField"
            ) { target ->
                if (target == SearchState.Collapsed) {
                    TabRow(selectedTabIndex = selectedTabIndex) {
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
                } else {
                    // When TextField appears → auto focus
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                        keyboard?.show()
                    }
                    TextField(
                        value = searchQuery,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = {
                            Text(stringResource(R.string.search_in_transcript))
                        },
                        leadingIcon = {
                            IconButton(onClick = {
                                state = SearchState.Collapsed
                                keyboard?.hide()
                                onQueryChange("")
                                onClearQuery()
                            }) {
                                Icon(Icons.Default.NavigateBefore,
                                    contentDescription = stringResource(R.string.toggle_search))
                            }
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = onClearQuery) {
                                    Icon(Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.clear_search))
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            onSearchAction()
                            keyboard?.hide()
                        }),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                }
            }

            // Right part: Search button (keep fixed 56.dp width to prevent layout jumps)
            transition.AnimatedVisibility(
                visible = { it == SearchState.Collapsed },
                enter = fadeIn() + scaleIn(tween(200)),
                exit  = fadeOut() + scaleOut(tween(200))
            ) {
                // Box keeps 56.dp width even when IconButton disappears
                Box(Modifier.width(56.dp), contentAlignment = Alignment.Center) {
                    IconButton(onClick = { state = SearchState.Expanded }) {
                        Icon(Icons.Default.Search,
                            contentDescription = stringResource(R.string.toggle_search))
                    }
                }
            }
        }
    }
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
                if (initialState.first != targetState.first) {
                    slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut()
                } else {
                    fadeIn(animationSpec = androidx.compose.animation.core.tween(200)) togetherWith
                            fadeOut(animationSpec = androidx.compose.animation.core.tween(200))
                }.using(
                    SizeTransform(clip = false)
                )
            },
            label = "ContentAreaAnimation"
        ) { targetState ->
            val (currentTab, isSearching, isFilterEmpty) = targetState
            val currentModifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)

            when {
                isSearching && !isFilterEmpty -> {
                    TranscriptList(
                        segments = filteredSegments,
                        currentTimeMillis = currentTimeMillis,
                        onSegmentClick = onSegmentClick,
                        modifier = currentModifier
                    )
                }
                isSearching && isFilterEmpty -> {
                    Box(
                        modifier = currentModifier,
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SentimentVeryDissatisfied,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp).padding(bottom = 8.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.no_results_found, searchQuery),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    when (currentTab) {
                        0 -> {
                            TranscriptList(
                                segments = allSegments.filter { !it.isChapterStart },
                                currentTimeMillis = currentTimeMillis,
                                onSegmentClick = onSegmentClick,
                                modifier = currentModifier
                            )
                        }
                        1 -> {
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
    val patterns = listOf(
        Regex("""(?:https?://)?(?:www\.)?youtube\.com/watch\?v=([a-zA-Z0-9_-]{11})"""),
        Regex("""(?:https?://)?(?:www\.)?youtu\.be/([a-zA-Z0-9_-]{11})"""),
        Regex("""(?:https?://)?(?:www\.)?youtube\.com/embed/([a-zA-Z0-9_-]{11})"""),
        Regex("""(?:https?://)?(?:www\.)?youtube\.com/v/([a-zA-Z0-9_-]{11})""")
    )
    for (pattern in patterns) {
        val match = pattern.find(fullUrl)
        if (match != null && match.groupValues.size > 1) {
            return match.groupValues[1]
        }
    }
    val simpleMatch = Regex("(?:v=|/|embed/)([a-zA-Z0-9_-]{11})").find(fullUrl)
    return simpleMatch?.groups?.get(1)?.value
}
