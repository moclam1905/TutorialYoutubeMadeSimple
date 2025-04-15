package com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.tag

/**
 * Domain model representing a Tag along with the count of quizzes associated with it.
 */
data class TagWithCount(
    val tag: Tag,
    val quizCount: Int
)
