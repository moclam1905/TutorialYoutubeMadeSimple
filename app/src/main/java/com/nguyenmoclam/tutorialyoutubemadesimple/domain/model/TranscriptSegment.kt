package com.nguyenmoclam.tutorialyoutubemadesimple.domain.model

/**
 * Domain model representing a segment of a video transcript with timestamp.
 */
data class TranscriptSegment(
    val id: Long = 0,
    val transcriptId: Long,
    val timestamp: String, // Format: "mm:ss"
    val timestampMillis: Long, // Timestamp in milliseconds for easier comparison
    val text: String,
    val isChapterStart: Boolean = false,
    val chapterTitle: String? = null
)