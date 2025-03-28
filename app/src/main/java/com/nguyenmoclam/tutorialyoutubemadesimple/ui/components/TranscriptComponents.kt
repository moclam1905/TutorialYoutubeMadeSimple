package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.TranscriptSegment
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.TimeUtils

/**
 * A component that displays a list of transcript segments.
 */
@Composable
fun TranscriptList(
    segments: List<TranscriptSegment>,
    currentTimeMillis: Long,
    onSegmentClick: (TranscriptSegment) -> Unit,
    modifier: Modifier = Modifier
) {
    if (segments.isEmpty()) {
        Box(
            modifier = modifier.padding(16.dp),
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

    val lazyListState = rememberLazyListState()

    // Find the index of the current segment
    val currentIndex by remember(currentTimeMillis, segments) {
        derivedStateOf {
            val timestamps = segments.map { it.timestampMillis }
            TimeUtils.findClosestSegmentIndex(currentTimeMillis, timestamps)
        }
    }

    // Auto-scroll to the current segment with some offset to show context
    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0 && currentIndex < segments.size) {
            // Calculate scroll position with offset to show some context
            val scrollIndex = (currentIndex - 2).coerceAtLeast(0)
            lazyListState.animateScrollToItem(scrollIndex)
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = modifier
    ) {
        items(segments.size) { index ->
            val segment = segments[index]
            val isActive = segment.timestampMillis <= currentTimeMillis &&
                    (index == segments.size - 1 ||
                            segments[index + 1].timestampMillis > currentTimeMillis)

            TranscriptItem(
                segment = segment,
                isActive = isActive,
                onSegmentClick = onSegmentClick
            )
        }
    }
}

/**
 * A component that displays a single transcript segment.
 */
@Composable
fun TranscriptItem(
    segment: TranscriptSegment,
    isActive: Boolean,
    onSegmentClick: (TranscriptSegment) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "Background Color Animation"
    )

    val textColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = 300),
        label = "Text Color Animation"
    )

    val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onSegmentClick(segment) }
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        // If this segment is a chapter start, show the chapter title
        if (segment.isChapterStart && segment.chapterTitle != null) {
            ChapterHeader(title = segment.chapterTitle)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timestamp with rounded background if active
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = segment.timestamp,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            // Transcript text
            Text(
                text = segment.text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = fontWeight,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
        }

        if (segment.isChapterStart) {
            Divider(
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        }
    }
}

/**
 * A component that displays a chapter header.
 */
@Composable
fun ChapterHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Bookmark,
            contentDescription = "Chapter",
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
