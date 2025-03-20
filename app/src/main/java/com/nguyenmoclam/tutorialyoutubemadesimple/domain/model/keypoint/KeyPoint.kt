package com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.keypoint

/**
 * Domain model representing a key point extracted from a video transcript.
 * Key points are important facts or concepts identified in the video content.
 */
data class KeyPoint(
    val id: Long = 0,
    val quizId: Long,
    val content: String
)