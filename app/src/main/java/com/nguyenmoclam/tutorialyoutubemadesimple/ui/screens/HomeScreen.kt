package com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.QuizStats
import com.nguyenmoclam.tutorialyoutubemadesimple.navigation.AppScreens
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.NetworkAwareImageLoader
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.LocalNetworkUtils
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val state by viewModel.state.collectAsState()
    // Trigger refresh when screen becomes active
    LaunchedEffect(Unit) {
        viewModel.refreshQuizzes()
    }

    // Add scroll state tracking
    val lazyListState = rememberLazyListState()
    val isScrollingUp = remember {
        derivedStateOf {
            if (lazyListState.firstVisibleItemIndex > 0) {
                // When scrolled past the first item, check scroll direction
                lazyListState.firstVisibleItemScrollOffset == 0 ||
                        lazyListState.isScrollInProgress.not()
            } else {
                // Always show navigation when at the top
                true
            }
        }
    }

    // Share scroll state with MainActivity
    LaunchedEffect(isScrollingUp.value) {
        // Update the BottomNavigationVisibilityState singleton
        BottomNavigationVisibilityState.isVisible.value = isScrollingUp.value
    }

    // Delete Confirmation Dialog
    state.showDeleteConfirmDialog?.let { quizId ->
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteQuizDialog() },
            title = { Text(stringResource(R.string.delete_quiz)) },
            text = { Text(stringResource(R.string.delete_quiz_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteQuiz(quizId) }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.hideDeleteQuizDialog() }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.learning_hub)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 8.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {

            Text(
                text = stringResource(R.string.explore_challenges),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { query -> viewModel.updateSearchQuery(query) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text(stringResource(R.string.search_challenges)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Tabs
            var selectedTabIndex by remember { mutableIntStateOf(0) }
            val tabs = listOf(
                stringResource(R.string.all),
                stringResource(R.string.popular),
                stringResource(R.string.new_filter),
                stringResource(R.string.trending)
            )

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)) {
                tabs.forEachIndexed { index, title ->
                    FilterTab(
                        title = title,
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                // Show loading indicator when data is being loaded
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (state.networkRestricted) {
                // Show network restriction message
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.network_restricted),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.content_loading_restricted),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else if (state.quizzes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.no_quizzes_available),
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.quizzes) { quiz ->
                        LearningChallengeItem(
                            quiz = quiz,
                            isStatsExpanded = state.expandedStatsMap[quiz.id] == true,
                            quizStats = state.quizStatsCache[quiz.id],
                            onToggleStats = { viewModel.toggleStatsExpanded(quiz.id) },
                            onDeleteQuiz = { viewModel.showDeleteQuizDialog(quiz.id) },
                            daysSinceLastUpdate = viewModel.getDaysSinceLastUpdate(quiz.lastUpdated),
                            onQuizClick = {
                                navController.navigate(
                                    AppScreens.QuizDetail.withArgs(
                                        quiz.id.toString()
                                    )
                                )
                            },
                            searchQuery = state.searchQuery // Pass searchQuery from state
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }


}

@Composable
fun FilterTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun LearningChallengeItem(
    quiz: Quiz,
    isStatsExpanded: Boolean,
    quizStats: QuizStats?,
    onToggleStats: () -> Unit,
    onDeleteQuiz: () -> Unit,
    daysSinceLastUpdate: Int,
    onQuizClick: () -> Unit,
    searchQuery: String = "" // Add searchQuery parameter with default empty value
) {
    val networkUtils = LocalNetworkUtils.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable { onQuizClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title row with delete icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Use highlightText function for title
                Text(
                    text = highlightText(quiz.title, searchQuery),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(
                    onClick = { onDeleteQuiz() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Quiz",
                        tint = Color.Gray.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Add thumbnail if use of NetworkAwareImageLoader is appropriate
            if (quiz.thumbnailUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                NetworkAwareImageLoader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    imageUrl = quiz.thumbnailUrl, // Network URL
                    localPath = quiz.localThumbnailPath, // Local file path
                    contentDescription = quiz.title,
                    networkUtils = networkUtils,
                    contentScale = ContentScale.Crop,
                    onRetryClick = {}
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Description with highlighted search query
            Text(
                text = highlightText(quiz.description, searchQuery),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Question count and last update info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.total_questions, quiz.questionCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.last_update, daysSinceLastUpdate),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats button
            Button(
                onClick = onToggleStats,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.view_stats),
                    fontWeight = FontWeight.Medium
                )
            }

            // Animated stats section
            val visibleState = remember { MutableTransitionState(false) }
            visibleState.targetState = isStatsExpanded

            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(
                    animationSpec = tween(
                        300
                    )
                ),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(
                    animationSpec = tween(
                        300
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.quiz_statistics),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    if (quizStats != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.average_score),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = String.format("%.1f%%", quizStats.completionScore * 100),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.average_completion_time),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = stringResource(
                                    R.string.time_elapsed_seconds,
                                    quizStats.timeElapsedSeconds
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.no_quiz_attempts),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Highlights search query in text by creating an AnnotatedString with different style for matching parts
 */
@Composable
fun highlightText(text: String, searchQuery: String): AnnotatedString {
    if (searchQuery.isBlank()) {
        return AnnotatedString(text)
    }

    val highlightColor = MaterialTheme.colorScheme.primary

    return buildAnnotatedString {
        val lowercaseText = text.lowercase()
        val lowercaseQuery = searchQuery.lowercase()

        var startIndex = 0
        var matchIndex = lowercaseText.indexOf(lowercaseQuery, startIndex)

        while (matchIndex >= 0) {
            // Add text before match
            append(text.substring(startIndex, matchIndex))

            // Add highlighted match
            val endIndex = matchIndex + searchQuery.length
            withStyle(
                SpanStyle(
                    color = highlightColor,
                    fontWeight = FontWeight.Bold,
                    background = highlightColor.copy(alpha = 0.2f)
                )
            ) {
                append(text.substring(matchIndex, endIndex))
            }

            // Move to next match
            startIndex = endIndex
            matchIndex = lowercaseText.indexOf(lowercaseQuery, startIndex)
        }

        // Add remaining text
        if (startIndex < text.length) {
            append(text.substring(startIndex))
        }
    }
}