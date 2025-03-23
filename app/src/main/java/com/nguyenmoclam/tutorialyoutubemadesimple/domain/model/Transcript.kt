package com.nguyenmoclam.tutorialyoutubemadesimple.domain.model

/**
 * Domain model representing a video transcript.
 */
data class Transcript(
    val id: Long = 0,
    val quizId: Long,
    val content: String,
    val language: String = "en",
    val lastUpdated: Long = System.currentTimeMillis()
)