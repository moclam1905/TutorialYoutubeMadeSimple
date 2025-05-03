package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.QuizStats
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.LocalNetworkUtils
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.highlightText // Import highlightText

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
    searchQuery: String = ""
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp) // CHANGED padding to 16.dp
            .clickable { onQuizClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Use theme color
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            QuizItemHeader(
                title = quiz.title,
                searchQuery = searchQuery,
                onDeleteQuiz = onDeleteQuiz
            )

            QuizItemThumbnail(
                thumbnailUrl = quiz.thumbnailUrl,
                localPath = quiz.localThumbnailPath,
                contentDescription = quiz.title
            ) // Handles spacing internally if needed

            QuizItemDescription(
                description = quiz.description,
                searchQuery = searchQuery
            )

            Spacer(modifier = Modifier.height(12.dp))

            QuizItemInfoRow(
                questionCount = quiz.questionCount,
                daysSinceLastUpdate = daysSinceLastUpdate
            )

            Spacer(modifier = Modifier.height(12.dp))

            ViewStatsButton(onClick = onToggleStats)

            QuizItemStatsSection(
                isExpanded = isStatsExpanded,
                quizStats = quizStats
            )
        }
    }
}

@Composable
fun QuizItemHeader(
    title: String,
    searchQuery: String,
    onDeleteQuiz: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = highlightText(title, searchQuery), // highlightText is in HomeScreenComponents
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface // Use theme color
        )
        IconButton(
            onClick = onDeleteQuiz,
            modifier = Modifier.size(24.dp) // Consistent size
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.delete_quiz), // Use string resource
                tint = MaterialTheme.colorScheme.onSurfaceVariant, // Use theme color
                modifier = Modifier.size(18.dp) // Slightly larger icon
            )
        }
    }
}

@Composable
fun QuizItemThumbnail(
    thumbnailUrl: String,
    localPath: String?,
    contentDescription: String
) {
    val networkUtils = LocalNetworkUtils.current
    // Add thumbnail only if URL is present
    if (thumbnailUrl.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        NetworkAwareImageLoader( // Assumes NetworkAwareImageLoader is accessible
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp)), // Clip the image corners
            imageUrl = thumbnailUrl,
            localPath = localPath,
            contentDescription = contentDescription,
            networkUtils = networkUtils,
            contentScale = ContentScale.Crop,
            onRetryClick = {} // Provide a lambda, even if empty
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun QuizItemDescription(
    description: String,
    searchQuery: String
) {
    Text(
        text = highlightText(description, searchQuery), // highlightText is in HomeScreenComponents
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant, // Use theme color
        modifier = Modifier.padding(top = 4.dp),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun QuizItemInfoRow(
    questionCount: Int,
    daysSinceLastUpdate: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Add Icon for question count
        Icon(
            imageVector = Icons.Default.HelpOutline,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.total_questions, questionCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.weight(1f)) // Push update info to the end
        // Add Icon for update date
        Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.last_update, daysSinceLastUpdate),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant // Use theme color
        )
    }
}

@Composable
fun ViewStatsButton(onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { // Wrap in Row to align end
        FilledTonalButton( // CHANGED to FilledTonalButton
            onClick = onClick,
            colors = ButtonDefaults.filledTonalButtonColors() // Use default tonal colors
        ) {
            Text(
                text = stringResource(R.string.view_stats),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun QuizItemStatsSection(
    isExpanded: Boolean,
    quizStats: QuizStats?
) {
    val visibleState = remember { MutableTransitionState(false) }.apply {
        targetState = isExpanded
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize() // ADDED animateContentSize
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
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant // Use theme color
            )

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant) // Use theme color
            Spacer(modifier = Modifier.height(8.dp))

            if (quizStats != null) {
                // Use ListItem instead of QuizStatRow
                ListItem(
                    headlineContent = { Text(stringResource(R.string.average_score)) },
                    trailingContent = { Text(String.format("%.1f%%", quizStats.completionScore * 100), fontWeight = FontWeight.Bold) },
                    modifier = Modifier.padding(vertical = 4.dp) // Adjust padding if needed
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.average_completion_time)) },
                    trailingContent = { Text(stringResource(R.string.time_elapsed_seconds, quizStats.timeElapsedSeconds), fontWeight = FontWeight.Bold) },
                    modifier = Modifier.padding(vertical = 4.dp) // Adjust padding if needed
                )
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
