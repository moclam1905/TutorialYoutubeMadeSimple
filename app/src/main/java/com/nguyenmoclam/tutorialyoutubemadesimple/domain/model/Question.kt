package com.nguyenmoclam.tutorialyoutubemadesimple.domain.model

data class Question(
    val id: Long = 0,
    val quizId: Long,
    val text: String,
    val options: List<String>,
    val correctAnswer: String
)