package com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.content

data class Question(
    val original: String,
    var rephrased: String = "",
    var answer: String = ""
)