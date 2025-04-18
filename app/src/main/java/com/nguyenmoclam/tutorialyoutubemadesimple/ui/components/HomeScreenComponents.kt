package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.QuizStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar() {
    TopAppBar(
        title = { Text(stringResource(R.string.learning_hub)) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun ScreenTitle(titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    placeholderRes: Int,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text(
                stringResource(placeholderRes),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }, // Adjust placeholder color if needed
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search_challenges),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        shape = RoundedCornerShape(8.dp),
        singleLine = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = Color.Transparent, // Make background transparent
            focusedBorderColor = Color.Transparent, // Remove border when focused
            unfocusedBorderColor = Color.Transparent // Remove border when unfocused
        )
    )
}

@Composable
fun FilterTabs(
    selectedTabIndex: Int,
    onTabSelected: (Int, String) -> Unit
) {
    val tabs = listOf(
        R.string.all to "All",
        R.string.summary_tab to "Summary",
        R.string.questions_tab to "Question",
        R.string.mindmap_tab to "Mindmap"
        // Add more filters here if needed, mapping string resource to a key
    )

    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        edgePadding = 0.dp,
        divider = {},
        indicator = {}
    ) {
        tabs.forEachIndexed { index, (titleRes, titleKey) ->
            FilterTab(
                title = stringResource(titleRes),
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index, titleKey) }
            )
        }
    }
}

/**
 * Composable for displaying sub-filter chips (e.g., for Question status).
 */
@Composable
fun SubFilterChips(
    selectedSubFilterIndex: Int,
    onSubFilterSelected: (Int, String) -> Unit // Pass index and key
) {
    val subFilters = listOf(
        R.string.all_questions to "All",
        R.string.in_progress_question to "InProgress",
        R.string.completed to "Completed"
    )

    ScrollableTabRow(
        selectedTabIndex = selectedSubFilterIndex,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
        edgePadding = 0.dp,
        divider = {},
        indicator = {}
    ) {
        subFilters.forEachIndexed { index, (titleRes, titleKey) ->
            FilterTab(
                title = stringResource(titleRes),
                selected = selectedSubFilterIndex == index,
                onClick = { onSubFilterSelected(index, titleKey) }
            )
        }
    }
}


@Composable
fun FilterTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val textColor =
        if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = textColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_quiz)) },
        text = { Text(stringResource(R.string.delete_quiz_confirmation)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

// --- Content Display Logic ---

@Composable
fun QuizListContent(
    isLoading: Boolean,
    isNetworkRestricted: Boolean,
    quizzes: List<Quiz>,
    lazyListState: LazyListState,
    expandedStatsMap: Map<Long, Boolean>,
    quizStatsCache: Map<Long, QuizStats?>,
    searchQuery: String,
    onToggleStats: (Long) -> Unit,
    onDeleteQuiz: (Long) -> Unit,
    getDaysSinceLastUpdate: (Long) -> Int,
    onQuizClick: (Long) -> Unit
) {
    when {
        isLoading -> LoadingIndicator()
        isNetworkRestricted -> NetworkRestrictedMessage()
        quizzes.isEmpty() -> EmptyStateMessage(messageRes = R.string.no_quizzes_available)
        else -> QuizList(
            quizzes = quizzes,
            lazyListState = lazyListState,
            expandedStatsMap = expandedStatsMap,
            quizStatsCache = quizStatsCache,
            searchQuery = searchQuery,
            onToggleStats = onToggleStats,
            onDeleteQuiz = onDeleteQuiz,
            getDaysSinceLastUpdate = getDaysSinceLastUpdate,
            onQuizClick = onQuizClick
        )
    }
}

@OptIn(ExperimentalFoundationApi::class) // Add OptIn annotation
@Composable
fun QuizList(
    quizzes: List<Quiz>,
    lazyListState: LazyListState,
    expandedStatsMap: Map<Long, Boolean>,
    quizStatsCache: Map<Long, QuizStats?>,
    searchQuery: String,
    onToggleStats: (Long) -> Unit,
    onDeleteQuiz: (Long) -> Unit,
    getDaysSinceLastUpdate: (Long) -> Int,
    onQuizClick: (Long) -> Unit
) {
    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(items = quizzes, key = { quiz -> quiz.id }) { quiz ->
            // Add animateItemPlacement modifier for list animations
            Box(
                modifier = Modifier.animateItemPlacement(
                    // Optional: Add animation spec like tween
                    tween(durationMillis = 300)
                )
            ) {
                // Pass lambdas directly capturing quiz.id (Long)
                LearningChallengeItem( // This will be imported from ChallengeItemComponents
                    quiz = quiz,
                    isStatsExpanded = expandedStatsMap[quiz.id] == true,
                    quizStats = quizStatsCache[quiz.id],
                    onToggleStats = { onToggleStats(quiz.id) },
                    onDeleteQuiz = { onDeleteQuiz(quiz.id) },
                    daysSinceLastUpdate = getDaysSinceLastUpdate(quiz.lastUpdated),
                    onQuizClick = { onQuizClick(quiz.id) },
                    searchQuery = searchQuery
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun NetworkRestrictedMessage() {
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
}

@Composable
fun EmptyStateMessage(messageRes: Int) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(messageRes),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}
