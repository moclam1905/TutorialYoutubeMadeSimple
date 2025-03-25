package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import java.io.File

class QuizViewModel : ViewModel() {

    /** Generated HTML summary content of the video */
    var summaryText by mutableStateOf("")
        private set

    /** Indicates whether a summarization process is currently in progress */
    var isLoading by mutableStateOf(false)
        private set

    /** Stores error messages when operations fail, null when no errors */
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun exportSummaryToHtml(context: Context) {
        try {
            // Write summary content to a temporary HTML file
            val fileName = "summary_${System.currentTimeMillis()}.html"
            val file = File(context.cacheDir, fileName)
            file.writeText(summaryText, Charsets.UTF_8)
            // Get URI using FileProvider
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            // Create a share intent
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/html"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            // Open the share dialog
            context.startActivity(
                android.content.Intent.createChooser(
                    shareIntent,
                    context.getString(R.string.share_dialog_title)
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                context.getString(R.string.error_export_file, e.message),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /** Quiz creation form state variables */
    var youtubeUrl by mutableStateOf("")
        private set

    var selectedLanguage by mutableStateOf("English")
        private set

    var questionType by mutableStateOf("multiple-choice")
        private set

    var questionCountMode by mutableStateOf("auto")
        private set

    var questionLevel by mutableStateOf("medium")
        private set

    var manualQuestionCount by mutableStateOf("5")
        private set

    var generateSummary by mutableStateOf(true)
        private set

    var generateQuestions by mutableStateOf(true)
        private set

    /** Get the actual question count based on mode and level */
    val numberOfQuestions: Int
        get() = when (questionCountMode) {
            "auto" -> when (questionLevel) {
                "low" -> (5..10).random() // Random between 5-10 questions
                "medium" -> (11..20).random() // Random between 11-20 questions
                "high" -> (21..30).random() // Random between 21-30 questions
                else -> (11..20).random() // Default to medium if unknown
            }

            "manual" -> manualQuestionCount.toIntOrNull() ?: 5
            else -> 5 // Default value
        }

    /** Update functions for quiz creation form state */
    fun updateYoutubeUrl(url: String) {
        youtubeUrl = url
    }

    fun updateSelectedLanguage(language: String) {
        selectedLanguage = language
    }

    fun updateQuestionType(type: String) {
        questionType = type
    }

    fun updateQuestionCountMode(mode: String) {
        questionCountMode = mode
    }

    fun updateQuestionLevel(level: String) {
        questionLevel = level
    }

    fun updateManualQuestionCount(count: String) {
        manualQuestionCount = count
    }

    fun updateGenerateSummary(generate: Boolean) {
        generateSummary = generate
    }

    fun updateGenerateQuestions(generate: Boolean) {
        generateQuestions = generate
    }

    /** Reset quiz creation form state */
    fun resetQuizFormState() {
        youtubeUrl = ""
        selectedLanguage = "English"
        questionType = "multiple-choice"
        questionCountMode = "auto"
        questionLevel = "medium"
        manualQuestionCount = "5"
        generateSummary = true
        generateQuestions = true
    }
}
