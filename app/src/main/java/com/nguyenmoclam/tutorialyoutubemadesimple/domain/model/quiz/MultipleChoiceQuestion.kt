package com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz

data class MultipleChoiceQuestion(
    val question: String,
    val options: Map<String, String>,
    val correctAnswers: List<String>
)