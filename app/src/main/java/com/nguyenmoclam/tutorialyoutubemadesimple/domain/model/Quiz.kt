package com.nguyenmoclam.tutorialyoutubemadesimple.domain.model

data class Quiz(
    val id: Long = 0,
    val title: String,
    val description: String,
    val videoUrl: String,
    val language: String,
    val questionType: String,
    val questionCount: Int,
    val summaryEnabled: Boolean,
    val questionsEnabled: Boolean,
    val lastUpdated: Long
)