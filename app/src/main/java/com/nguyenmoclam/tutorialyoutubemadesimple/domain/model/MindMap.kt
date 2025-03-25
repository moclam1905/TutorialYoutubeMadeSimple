package com.nguyenmoclam.tutorialyoutubemadesimple.domain.model

data class MindMap(
    val id: Long = 0,
    val quizId: Long,
    val keyPoints: List<String>,
    val mermaidCode: String,
    val lastUpdated: Long = System.currentTimeMillis()
)
