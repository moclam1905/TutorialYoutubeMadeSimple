package com.nguyenmoclam.tutorialyoutubemadesimple.domain.model

import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.tag.Tag // Add import for Tag

data class Quiz(
    val id: Long = 0,
    val title: String,
    val description: String,
    val videoUrl: String,
    val thumbnailUrl: String = "", // Store YouTube video thumbnail URL
    val language: String,
    val questionType: String,
    val questionCount: Int,
    val summaryEnabled: Boolean,
    val questionsEnabled: Boolean,
    val lastUpdated: Long,
    val localThumbnailPath: String? = null, // Path to locally stored thumbnail
    val reminderInterval: Long? = null, // Interval in milliseconds, null or 0 means disabled
    val tags: List<Tag> = emptyList() // List of associated tags
)
