package com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.content

data class Topic(
    val title: String,
    val rephrased_title: String = "",
    val questions: List<Question>
)