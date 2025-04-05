package com.nguyenmoclam.tutorialyoutubemadesimple.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Highlights search query in text by creating an AnnotatedString with different style for matching parts
 */
@Composable
fun highlightText(text: String, searchQuery: String): AnnotatedString {
    if (searchQuery.isBlank() || text.isBlank()) { // Added check for blank text
        return AnnotatedString(text)
    }

    val highlightColor = MaterialTheme.colorScheme.primary

    return buildAnnotatedString {
        val lowercaseText = text.lowercase()
        val lowercaseQuery = searchQuery.lowercase()

        var startIndex = 0
        var matchIndex = lowercaseText.indexOf(lowercaseQuery, startIndex)

        while (matchIndex >= 0) {
            // Add text before match
            if (matchIndex > startIndex) { // Avoid appending empty string if match is at start
                append(text.substring(startIndex, matchIndex))
            }

            // Add highlighted match
            val endIndex = matchIndex + searchQuery.length
            withStyle(
                SpanStyle(
                    color = highlightColor,
                    fontWeight = FontWeight.Bold,
                    background = highlightColor.copy(alpha = 0.2f)
                )
            ) {
                // Ensure endIndex does not exceed text length
                append(text.substring(matchIndex, endIndex.coerceAtMost(text.length)))
            }

            // Move to next match
            startIndex = endIndex
            if (startIndex >= text.length) break // Exit loop if startIndex is out of bounds
            matchIndex = lowercaseText.indexOf(lowercaseQuery, startIndex)
        }

        // Add remaining text
        if (startIndex < text.length) {
            append(text.substring(startIndex))
        }
    }
}
