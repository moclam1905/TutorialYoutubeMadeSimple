package com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter

data class Message(val role: String, val content: String)

data class Choice(val message: Message)

data class OpenRouterResponse(val choices: List<Choice>)

data class OpenRouterRequest(val model: String, val messages: List<Message>)