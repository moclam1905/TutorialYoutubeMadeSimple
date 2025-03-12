package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmoclam.tutorialyoutubemadesimple.ApiService
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.YouTubeApi
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.HtmlGenerator
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.LLMProcessor
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.Section
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.YouTubeTranscriptLight
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

enum class ProcessingStep(val messageRes: Int) {
    FETCH_METADATA(R.string.step_fetch_metadata),
    PROCESS_TRANSCRIPT(R.string.step_process_transcript),
    EXTRACT_TOPICS(R.string.step_extract_topics),
    GENERATE_HTML(R.string.step_generate_html),
    NONE(0);

    fun getMessage(context: Context): String {
        return if (messageRes != 0) context.getString(messageRes) else ""
    }
}

/**
 * ViewModel responsible for managing YouTube video summarization workflow.
 * Handles video information retrieval, transcript processing, and summary generation.
 *
 * Key responsibilities:
 * - Extracts video ID from YouTube URLs
 * - Fetches video metadata using YouTube API
 * - Retrieves and processes video transcripts
 * - Generates summarized content in HTML format
 * - Manages loading states and error handling
 * - Provides file sharing functionality
 */
@HiltViewModel
class SummaryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val youTubeApiService: YouTubeApi
) : ViewModel() {

    /** Generated HTML summary content of the video */
    var summaryText by mutableStateOf("")
        private set

    /** Current processing step during summarization */
    var currentStep by mutableStateOf(ProcessingStep.NONE)
        private set

    /** Indicates whether a summarization process is currently in progress */
    var isLoading by mutableStateOf(false)
        private set

    /** Stores error messages when operations fail, null when no errors */
    var errorMessage by mutableStateOf<String?>(null)
        private set

    /**
     * Extracts YouTube video ID from various URL formats or direct ID input.
     *
     * Supports the following formats:
     * - Standard watch URLs: https://www.youtube.com/watch?v=VIDEO_ID
     * - Short URLs: https://youtu.be/VIDEO_ID
     * - Direct video ID input
     *
     * @param input The YouTube URL or video ID string
     * @return The extracted video ID or null if input is invalid
     */
    private fun extractVideoId(input: String): String? {
        val url = input.trim()
        return try {
            val regexWatch = Regex("v=([^&]+)")
            val regexShort = Regex("youtu\\.be/([^?]+)")
            when {
                url.contains("watch?v=") -> regexWatch.find(url)?.groupValues?.get(1)
                url.contains("youtu.be/") -> regexShort.find(url)?.groupValues?.get(1)
                else -> {
                    // If string does not contain "youtube", it could be a direct ID
                    if (url.isNotEmpty() && !url.contains(" ")) url else null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Initiates the video summarization process.
     *
     * This function performs the following steps:
     * 1. Extracts video ID from the input URL/ID
     * 2. Fetches video metadata (title, thumbnail) using YouTube API
     * 3. Retrieves video transcript
     * 4. Processes transcript content using LLM to extract topics and questions
     * 5. Generates formatted HTML summary
     *
     * The process runs asynchronously in viewModelScope, updating UI states accordingly.
     *
     * @param videoUrlOrId YouTube video URL or ID to summarize
     * @param youtubeApiKey API key for accessing YouTube Data API
     *
     * State Updates:
     * - [isLoading]: true during processing, false when complete
     * - [summaryText]: contains generated HTML summary when successful
     * - [errorMessage]: contains error description if process fails
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun startSummarization(videoUrlOrId: String, youtubeApiKey: String) {
        val videoId = extractVideoId(videoUrlOrId)
        if (videoId == null) {
            // URL/ID invalid
            errorMessage = context.getString(R.string.error_invalid_url)
            return
        }
        // Reset states before starting a new process
        errorMessage = null
        summaryText = ""
        isLoading = true
        currentStep = ProcessingStep.FETCH_METADATA

        viewModelScope.launch {
            try {
                // Processing on IO thread
                var fetchedTitle: String
                var fetchedThumb: String
                var transcriptContent: String
                var summaryResult = ""
                // Execute network and processing operations on IO thread to avoid blocking the UI
                withContext(Dispatchers.IO) {

                    // Step 1: Fetch video metadata from YouTube API
                    // This includes title and available thumbnail URLs at different resolutions
                    val videoResponse = youTubeApiService.getVideoInfo(videoId, youtubeApiKey)
                    val snippet = videoResponse.items.firstOrNull()?.snippet
                    fetchedTitle = snippet?.title ?: context.getString(R.string.default_no_title)
                    // Attempts to get thumbnails in descending order of quality:
                    // maxres (1280x720) -> high (480x360) -> medium (320x180) -> default (120x90)
                    fetchedThumb = snippet?.thumbnails?.maxres?.url
                        ?: snippet?.thumbnails?.high?.url
                                ?: snippet?.thumbnails?.medium?.url
                                ?: snippet?.thumbnails?.default?.url
                                ?: ""

                    // Step 2: Retrieve and process video transcript
                    currentStep = ProcessingStep.PROCESS_TRANSCRIPT
                    // Combines all transcript segments into a single continuous text
                    val transcripts = YouTubeTranscriptLight.create().getTranscript(videoId)
                    transcriptContent = transcripts.joinToString(" ") { it.text }

                    // Step 3: Extract and process topics using LLM
                    currentStep = ProcessingStep.EXTRACT_TOPICS
                    // First pass: Extract main topics and generate questions
                    val topics =
                        LLMProcessor().extractTopicsAndQuestions(transcriptContent, fetchedTitle)
                    if (topics.isEmpty()) {
                        throw Exception(context.getString(R.string.error_no_summary))
                    }
                    // Second pass: Process extracted topics to generate comprehensive answers
                    val processedTopics = LLMProcessor().processContent(topics, transcriptContent)

                    // Step 4: Generate HTML content
                    currentStep = ProcessingStep.GENERATE_HTML
                    // Generate sections with questions and answers
                    val sections = processedTopics.map { topic ->
                        Section(
                            title = topic.rephrased_title.ifEmpty { topic.title },
                            bullets = topic.questions.map { question ->
                                Pair(
                                    question.rephrased.ifEmpty { question.original },
                                    question.answer
                                )
                            }
                        )
                    }

                    // Generate HTML content
                    summaryResult = HtmlGenerator.generate(
                        title = fetchedTitle,
                        imageUrl = fetchedThumb,
                        sections = sections
                    )
                }
                // Update UI on the Main thread after the operation
                summaryText = summaryResult
            } catch (e: YouTubeTranscriptLight.TranscriptError) {
                // Handle specific transcript error cases
                errorMessage = when (e) {
                    is YouTubeTranscriptLight.TranscriptError.VideoNotFound ->
                        context.getString(R.string.error_video_not_found)

                    is YouTubeTranscriptLight.TranscriptError.TranscriptsDisabled ->
                        context.getString(R.string.error_transcripts_disabled)

                    is YouTubeTranscriptLight.TranscriptError.NoTranscriptAvailable ->
                        context.getString(R.string.error_no_transcript)

                    is YouTubeTranscriptLight.TranscriptError.NetworkError ->
                        context.getString(R.string.error_network)

                    is YouTubeTranscriptLight.TranscriptError.LanguageNotFound ->
                        context.getString(R.string.error_language_not_found)
                }
            } catch (e: Exception) {
                // Handle other general errors
                errorMessage = e.message ?: context.getString(R.string.error_generic)
            } finally {
                isLoading = false
                currentStep = ProcessingStep.NONE
            }
        }
    }

    /**
     * Exports the generated summary as an HTML file and initiates the share dialog.
     *
     * Process:
     * 1. Creates a temporary HTML file in the app's cache directory
     * 2. Writes the summary content to the file
     * 3. Generates a content URI using FileProvider for secure file sharing
     * 4. Launches the system share dialog
     *
     * @param context Android Context required for file operations and sharing
     *
     * Error Handling:
     * - Displays a Toast message if file creation or sharing fails
     * - Logs stack trace for debugging purposes
     */
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
    val questionCount: Int
        get() = when (questionCountMode) {
            "auto" -> when (questionLevel) {
                "low" -> 5
                "medium" -> 10
                "high" -> 15
                else -> 10 // Default to medium if unknown
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
