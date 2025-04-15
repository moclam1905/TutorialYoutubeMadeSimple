package com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.tag

/**
 * Domain model representing a Tag.
 */
data class Tag(
    val id: Long = 0, // Use 0 as default for new tags before insertion
    val name: String
)
