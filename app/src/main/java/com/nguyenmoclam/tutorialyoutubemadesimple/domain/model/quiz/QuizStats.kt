package com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz

/**
 * Domain model representing quiz statistics.
 *
 * @property completionScore The percentage of quiz completion (0.0 to 1.0)
 * @property timeElapsedSeconds The time elapsed in seconds since the quiz was last updated
 */
data class QuizStats(
    val completionScore: Float,
    val timeElapsedSeconds: Int
)