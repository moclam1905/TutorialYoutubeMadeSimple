package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.YouTubeApi
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.HtmlGenerator
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.LLMProcessor
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.Section
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.YouTubeTranscriptLight
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class QuizCreationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val youTubeApiService: YouTubeApi
) : ViewModel() {

    data class QuizState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val quizSummary: String = "",
        val quizQuestionsJson: String = ""
    )

    var state by mutableStateOf(QuizState())
        private set

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleError(throwable)
    }

    private fun handleError(throwable: Throwable) {
        state = state.copy(
            isLoading = false,
            errorMessage = when (throwable) {
                is YouTubeTranscriptLight.TranscriptError -> handleTranscriptError(throwable)
                else -> throwable.message ?: context.getString(R.string.error_generic)
            }
        )
    }

    private fun handleTranscriptError(error: YouTubeTranscriptLight.TranscriptError): String {
        return when (error) {
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
    }

    fun createQuiz(
        videoUrlOrId: String,
        youtubeApiKey: String,
        generateSummary: Boolean,
        generateQuestions: Boolean,
        selectedLanguage: String,
        questionType: String,
        numberOfQuestions: Int
    ) {
        state = QuizState(isLoading = true)

        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val videoId = extractVideoId(videoUrlOrId)
                ?: throw IllegalArgumentException(context.getString(R.string.error_invalid_url))

            val videoResponse = youTubeApiService.getVideoInfo(videoId, youtubeApiKey)
            val snippet = videoResponse.items.firstOrNull()?.snippet
            val fetchedTitle = snippet?.title ?: context.getString(R.string.default_no_title)
            val fetchedThumb = snippet?.thumbnails?.run {
                maxres?.url ?: high?.url ?: medium?.url ?: default?.url
            } ?: ""

            val transcripts = YouTubeTranscriptLight.create().getTranscript(videoId)
            val transcriptContent = transcripts.joinToString(" ") { it.text }

            val newState = if (generateSummary && generateQuestions) {
                supervisorScope {
                    val summaryDeferred = async { createSummary(fetchedTitle, fetchedThumb, transcriptContent) }
                    val questionsDeferred = async { createQuestions(transcriptContent, selectedLanguage, questionType, numberOfQuestions) }
                    
                    try {
                        state.copy(
                            isLoading = false,
                            quizSummary = summaryDeferred.await(),
                            quizQuestionsJson = questionsDeferred.await()
                        )
                    } catch (e: Exception) {
                        throw e
                    }
                }
            } else if (generateSummary) {
                state.copy(
                    isLoading = false,
                    quizSummary = createSummary(fetchedTitle, fetchedThumb, transcriptContent)
                )
            } else if (generateQuestions) {
                state.copy(
                    isLoading = false,
                    quizQuestionsJson = createQuestions(transcriptContent, selectedLanguage, questionType, numberOfQuestions)
                )
            } else {
                state.copy(isLoading = false)
            }

            state = newState
        }
    }

    private suspend fun createSummary(
        fetchedTitle: String,
        fetchedThumb: String,
        transcriptContent: String
    ): String = withContext(Dispatchers.IO) {
        val processor = LLMProcessor()
        val topics = processor.extractTopicsAndQuestions(transcriptContent, fetchedTitle)
            .takeIf { it.isNotEmpty() }
            ?: throw IllegalStateException(context.getString(R.string.error_no_summary))

        val processedTopics = processor.processContent(topics, transcriptContent)
        val sections = processedTopics.map { topic ->
            Section(
                title = topic.rephrased_title.ifEmpty { topic.title },
                bullets = topic.questions.map { q ->
                    Pair(q.rephrased.ifEmpty { q.original }, q.answer)
                }
            )
        }

        HtmlGenerator.generate(
            title = fetchedTitle,
            imageUrl = fetchedThumb,
            sections = sections
        )
    }

    private suspend fun createQuestions(
        transcriptContent: String,
        selectedLanguage: String,
        questionType: String,
        numberOfQuestions: Int
    ): String = withContext(Dispatchers.IO) {
        val processor = LLMProcessor()
        val keyPoints = processor.extractKeyPoints(transcriptContent, selectedLanguage)
        processor.generateQuestionsFromKeyPoints(
            keyPoints = keyPoints,
            language = selectedLanguage,
            questionType = questionType,
            numberOfQuestions = numberOfQuestions
        )
    }

    private fun extractVideoId(input: String): String? {
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
