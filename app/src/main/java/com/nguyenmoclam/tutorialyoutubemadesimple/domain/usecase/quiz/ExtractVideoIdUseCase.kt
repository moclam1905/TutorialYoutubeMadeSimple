package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz

import javax.inject.Inject

/**
 * Use case for extracting a YouTube video ID from a URL or direct ID input.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class ExtractVideoIdUseCase @Inject constructor() {
    /**
     * Execute the use case to extract a video ID from a URL or direct ID input.
     *
     * @param input The YouTube URL or video ID
     * @return The extracted video ID, or null if the input is invalid
     */
    operator fun invoke(input: String): String? {
        val url = input.trim()
        return try {
            val regexWatch = Regex("v=([^&]+)")
            val regexShort = Regex("youtu\\.be/([^?]+)")
            when {
                url.contains("watch?v=") -> regexWatch.find(url)?.groupValues?.get(1)
                url.contains("youtu.be/") -> regexShort.find(url)?.groupValues?.get(1)
                else -> url.takeIf { it.isNotEmpty() && !it.contains(" ") }
            }
        } catch (e: Exception) {
            null
        }
    }
}