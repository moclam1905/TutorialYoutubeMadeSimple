package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.TranscriptSegment
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.TimeUtils

/**
 * A component that displays chapter navigation with enhanced markers on a progress bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterNavigationBar(
    chapters: List<TranscriptSegment>,
    currentTimeMillis: Long,
    totalDurationMillis: Long,
    onChapterClick: (TranscriptSegment) -> Unit,
    modifier: Modifier = Modifier
) {
    if (chapters.isEmpty() || totalDurationMillis <= 0) return

    var boxWidth by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(vertical = 8.dp)
            .onGloballyPositioned { coordinates ->
                boxWidth = coordinates.size.width
            }
    ) {
        // Background progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .align(Alignment.Center)
        )

        // Current progress with animation
        val progress = (currentTimeMillis.toFloat() / totalDurationMillis).coerceIn(0f, 1f)
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(durationMillis = 300),
            label = stringResource(R.string.progress)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
                .align(Alignment.CenterStart)
        )

        // Chapter markers
        chapters.forEachIndexed { index, chapter ->
            val chapterPosition =
                (chapter.timestampMillis.toFloat() / totalDurationMillis).coerceIn(0f, 1f)
            val isCurrentChapter = chapter.timestampMillis <= currentTimeMillis &&
                    (index == chapters.size - 1 || chapters[index + 1].timestampMillis > currentTimeMillis)

            // Calculate position based on chapter timestamp and actual container width
            // Ensure boxWidth is not zero to avoid calculation errors
            val xOffset = if (boxWidth > 0) (chapterPosition * boxWidth).toInt().dp else 0.dp

            // Create tooltip with chapter title
            val tooltipText = chapter.chapterTitle ?: stringResource(
                R.string.chapter_timestamp,
                chapter.timestamp
            )
            val tooltipState = rememberTooltipState(isPersistent = false)

            // Animate marker size when it becomes the current chapter
            val markerSize by animateDpAsState(
                targetValue = if (isCurrentChapter) 14.dp else 10.dp,
                animationSpec = tween(durationMillis = 300),
                label = "markerSize"
            )

            // Animate marker scale for pulse effect when current
            val markerScale by animateFloatAsState(
                targetValue = if (isCurrentChapter) 1.1f else 1f,
                animationSpec = tween(durationMillis = 500),
                label = "markerScale"
            )

            // Only render marker if boxWidth is valid to avoid layout issues
            if (boxWidth > 0) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.extraSmall,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(
                                text = tooltipText,
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    state = tooltipState,
                    modifier = Modifier
                        .offset(x = xOffset - (markerSize / 2))
                        .align(Alignment.Center)
                ) {
                    Box(
                        modifier = Modifier
                            .size(markerSize)
                            .scale(markerScale)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(
                                if (isCurrentChapter) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onChapterClick(chapter)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCurrentChapter) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = tooltipText,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * A component that displays chapter navigation with horizontal scrolling chips.
 */
@Composable
fun ChapterNavigation(
    chapters: List<TranscriptSegment>,
    currentTimeMillis: Long,
    onChapterClick: (TranscriptSegment) -> Unit,
    modifier: Modifier = Modifier
) {
    if (chapters.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_data),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    // Find the current chapter
    val currentChapterIndex by remember(currentTimeMillis, chapters) {
        derivedStateOf {
            val timestamps = chapters.map { it.timestampMillis }
            TimeUtils.findClosestSegmentIndex(currentTimeMillis, timestamps)
        }
    }

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(chapters) { chapter ->
            ChapterChip(
                chapter = chapter,
                isActive = chapters.indexOf(chapter) == currentChapterIndex,
                onClick = { onChapterClick(chapter) }
            )
        }
    }
}

/**
 * A component that displays a chapter chip with improved visual feedback.
 */
@Composable
fun ChapterChip(
    chapter: TranscriptSegment,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title =
        chapter.chapterTitle ?: stringResource(R.string.chapter_timestamp, chapter.timestamp)

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chapter timestamp with icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isActive)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = chapter.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isActive)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}