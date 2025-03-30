package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Summary
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.question.CreateQuizQuestionsUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.CreateQuizUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.ExtractVideoIdUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.FetchVideoMetadataUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.GenerateQuestionsUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.GenerateQuizSummaryUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.ParseQuestionsUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz.ProcessYouTubeTranscriptUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.summary.CreateQuizSummaryUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.topic.SaveTopicsUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.transcript.SaveTranscriptUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Transcript
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.TranscriptSegment
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.keypoint.SaveKeyPointsUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.transcript.ParseTranscriptContentUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.transcript.SaveTranscriptWithSegmentsUseCase
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

enum class ProcessingCreateStep(val messageRes: Int) {
    FETCH_METADATA(R.string.step_fetch_metadata),
    FETCH_TRANSCRIPT(R.string.step_process_transcript),
    GENERATE_SUMMARY_AND_QUESTIONS(R.string.step_create_quiz), // Combined state for parallel processing
    SAVE_TO_DATABASE(R.string.error_generic),
    NONE(0);

    fun getMessage(context: Context): String {
        return if (messageRes != 0) context.getString(messageRes) else ""
    }

    fun getProgressPercentage(): Float {
        return when (this) {
            NONE -> 0f
            FETCH_METADATA -> 10f
            FETCH_TRANSCRIPT -> 30f
            GENERATE_SUMMARY_AND_QUESTIONS -> 70f
            SAVE_TO_DATABASE -> 95f
        }
    }
}

@HiltViewModel
class QuizCreationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val extractVideoIdUseCase: ExtractVideoIdUseCase,
    private val fetchVideoMetadataUseCase: FetchVideoMetadataUseCase,
    private val processYouTubeTranscriptUseCase: ProcessYouTubeTranscriptUseCase,
    private val createQuizUseCase: CreateQuizUseCase,
    private val generateQuizSummaryUseCase: GenerateQuizSummaryUseCase,
    private val generateQuestionsUseCase: GenerateQuestionsUseCase,
    private val parseQuestionsUseCase: ParseQuestionsUseCase,
    private val createQuizSummaryUseCase: CreateQuizSummaryUseCase,
    private val createQuizQuestionsUseCase: CreateQuizQuestionsUseCase,
    private val saveTranscriptUseCase: SaveTranscriptUseCase,
    private val saveTranscriptWithSegmentsUseCase: SaveTranscriptWithSegmentsUseCase,
    private val parseTranscriptContentUseCase: ParseTranscriptContentUseCase,
    private val saveTopicsUseCase: SaveTopicsUseCase,
    private val saveKeyPointsUseCase: SaveKeyPointsUseCase,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    data class QuizState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val quizSummary: String = "",
        val quizIdInserted: Long = -1,
        val currentStep: ProcessingCreateStep = ProcessingCreateStep.NONE
    )

    var state by mutableStateOf(QuizState())
        private set

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleError(throwable)
    }

    private fun handleError(throwable: Throwable) {
        state = state.copy(
            isLoading = false,
            currentStep = ProcessingCreateStep.NONE,
            errorMessage = throwable.message ?: context.getString(R.string.error_generic)
        )
    }

    private fun handleTranscriptError(errorType: String): String {
        return when (errorType) {
            "VideoNotFound" ->
                context.getString(R.string.error_video_not_found)

            "TranscriptsDisabled" ->
                context.getString(R.string.error_transcripts_disabled)

            "NoTranscriptAvailable" ->
                context.getString(R.string.error_no_transcript)

            "NetworkError" ->
                context.getString(R.string.error_network)

            "LanguageNotFound" ->
                context.getString(R.string.error_language_not_found)

            "Network restricted by data saver settings" ->
                context.getString(R.string.restricted_network_error)

            else -> context.getString(R.string.error_generic)
        }
    }

    fun createQuiz(
        videoUrlOrId: String,
        youtubeApiKey: String,
        generateSummary: Boolean,
        generateQuestions: Boolean,
        selectedLanguage: String,
        questionType: String,
        numberOfQuestions: Int,
        transcriptMode: String
    ) {
        // Check data saver settings before starting the quiz creation process
        if (!networkUtils.shouldLoadContent()) {
            state = QuizState(
                isLoading = false,
                errorMessage = context.getString(R.string.restricted_network_error)
            )
            return
        }

        state = QuizState(isLoading = true, currentStep = ProcessingCreateStep.FETCH_METADATA)

        viewModelScope.launch(coroutineExceptionHandler) {
            // Extract video ID using the use case
            val videoId = extractVideoIdUseCase(videoUrlOrId)
                ?: throw IllegalArgumentException(context.getString(R.string.error_invalid_url))

            // Fetch video metadata using the use case
            val metadata = fetchVideoMetadataUseCase(
                videoId = videoId,
                apiKey = youtubeApiKey,
                defaultTitle = context.getString(R.string.default_no_title)
            )

            if (metadata.error != null) {
                throw IllegalStateException(metadata.error)
            }

            val fetchedTitle = metadata.title
            val fetchedThumb = metadata.thumbnailUrl
            val fetchedDescription = metadata.description

            // Process transcript using the use case
            state = state.copy(currentStep = ProcessingCreateStep.FETCH_TRANSCRIPT)
            val transcriptResult = processYouTubeTranscriptUseCase(
                videoId = videoId,
                languages = listOf("en"), transcriptMode = transcriptMode
            )

            if (transcriptResult.error != null) {
                throw IllegalStateException(handleTranscriptError(transcriptResult.error))
            }

            val transcriptContent = transcriptResult.text

            // Create quiz domain model
            val quiz = Quiz(
                title = fetchedTitle,
                description = fetchedDescription,
                videoUrl = videoUrlOrId,
                thumbnailUrl = fetchedThumb,
                language = selectedLanguage,
                questionType = questionType,
                questionCount = if (generateQuestions) numberOfQuestions else 0,
                summaryEnabled = generateSummary,
                questionsEnabled = generateQuestions,
                lastUpdated = System.currentTimeMillis()
            )

            // Insert quiz and get its ID using the use case
            val quizId = createQuizUseCase(quiz)

            // Create transcript object
            val transcript = Transcript(
                quizId = quizId,
                content = transcriptContent,
                language = selectedLanguage
            )

            // Process transcript segments if available
            if (transcriptResult.segments.isNotEmpty()) {
                // Convert YouTube transcript segments to our domain model
                var segments = transcriptResult.segments.mapIndexed { index, segment ->
                    val timestamp =
                        TimeUtils.convertMillisToTimestamp((segment.start * 1000).toLong())
                    val timestampMillis = (segment.start * 1000).toLong()

                    TranscriptSegment(
                        transcriptId = 0, // Will be set by the repository
                        timestamp = timestamp,
                        timestampMillis = timestampMillis,
                        text = segment.text,
                        isChapterStart = false // Default value, can be updated later
                    )
                }

                // Process chapters if available
                if (transcriptResult.chapters.isNotEmpty()) {
                    Log.d(
                        "QuizCreationViewModel",
                        "Processing ${transcriptResult.chapters.size} chapters"
                    )
                    // Mark segments that correspond to chapter start times
                    transcriptResult.chapters.forEach { chapter ->
                        val chapterStartTimeMs = (chapter.startSeconds * 1000).toLong()
                        // Find the segment closest to the chapter start time
                        val closestSegmentIndex = segments.indexOfFirst { segment ->
                            segment.timestampMillis >= chapterStartTimeMs
                        }.takeIf { it >= 0 } ?: 0

                        if (closestSegmentIndex < segments.size) {
                            // Mark this segment as a chapter start and add the chapter title
                            // Create a new mutable list to allow modification
                            val mutableSegments = segments.toMutableList()
                            mutableSegments[closestSegmentIndex] =
                                segments[closestSegmentIndex].copy(
                                    isChapterStart = true,
                                    chapterTitle = chapter.title
                                )
                            // Update the original list with the modified list
                            segments = mutableSegments
                            Log.d(
                                "QuizCreationViewModel",
                                "Added chapter: ${chapter.title} at ${segments[closestSegmentIndex].timestamp}"
                            )
                        }
                    }
                }

                // Save transcript with segments
                val transcriptId = saveTranscriptWithSegmentsUseCase(transcript, segments)

                // Only parse the content for chapter detection if no chapters were found from YouTube
                transcript.id = transcriptId

                // If we already have chapters from YouTube, skip additional parsing
                if (transcriptResult.chapters.isEmpty()) {
                    Log.d(
                        "QuizCreationViewModel",
                        "No chapters from YouTube, attempting to detect from content"
                    )
                    // For large transcripts, use the Flow-based implementation
                    val parsedSegments = if (transcriptContent.length > 10000) {
                        // Collect all segments from the flow
                        val segments = mutableListOf<TranscriptSegment>()
                        parseTranscriptContentUseCase.invokeAsFlow(transcriptContent, transcriptId)
                            .collect { chunks -> segments.clear(); segments.addAll(chunks) }
                        segments
                    } else {
                        // For smaller transcripts, use the direct method
                        parseTranscriptContentUseCase(transcriptContent, transcriptId)
                    }

                    // If parsing produced segments with chapters, update the transcript
                    if (parsedSegments.isNotEmpty() && parsedSegments.any { it.isChapterStart }) {
                        Log.d(
                            "QuizCreationViewModel",
                            "Detected ${parsedSegments.count { it.isChapterStart }} chapters from content"
                        )
                        saveTranscriptWithSegmentsUseCase(transcript, parsedSegments)
                    }
                } else {
                    Log.d(
                        "QuizCreationViewModel",
                        "Using ${transcriptResult.chapters.size} chapters from YouTube"
                    )
                }
            } else {
                // If no segments available, save transcript and try to parse content
                val transcriptId = saveTranscriptUseCase(transcript)

                // Try to parse transcript content into segments with chapter detection
                // For large transcripts, use the Flow-based implementation
                val parsedSegments = if (transcriptContent.length > 10000) {
                    // Collect all segments from the flow
                    val segments = mutableListOf<TranscriptSegment>()
                    parseTranscriptContentUseCase.invokeAsFlow(transcriptContent, transcriptId)
                        .collect { chunks -> segments.clear(); segments.addAll(chunks) }
                    segments
                } else {
                    // For smaller transcripts, use the direct method
                    parseTranscriptContentUseCase(transcriptContent, transcriptId)
                }

                // If segments were successfully parsed, update the transcript
                if (parsedSegments.isNotEmpty()) {
                    // Update transcript with the parsed segments
                    // The saveTranscriptWithSegmentsUseCase will handle chapter detection and processing
                    transcript.id = transcriptId
                    saveTranscriptWithSegmentsUseCase(transcript, parsedSegments)
                }
            }

            val newState = if (generateSummary && generateQuestions) {
                supervisorScope {
                    state =
                        state.copy(currentStep = ProcessingCreateStep.GENERATE_SUMMARY_AND_QUESTIONS)

                    val summaryDeferred = async {
                        val result = generateQuizSummaryUseCase(
                            fetchedTitle,
                            fetchedThumb,
                            transcriptContent,
                            selectedLanguage
                        )
                        if (result.error != null) {
                            throw IllegalStateException(result.error)
                        }
                        result.content
                    }

                    val questionsDeferred = async {
                        val result = generateQuestionsUseCase(
                            transcriptContent,
                            selectedLanguage,
                            questionType,
                            numberOfQuestions
                        )
                        if (result.error != null) {
                            throw IllegalStateException(result.error)
                        }
                        // Save key points extracted during question generation
                        val keyPoints = generateQuestionsUseCase.getLastExtractedKeyPoints()
                        saveKeyPointsUseCase(keyPoints, quizId)
                        result.content
                    }

                    try {
                        val summary = summaryDeferred.await()
                        val questionsJson = questionsDeferred.await()

                        state = state.copy(currentStep = ProcessingCreateStep.SAVE_TO_DATABASE)
                        // Save summary to database using domain model and use case
                        createQuizSummaryUseCase(
                            Summary(
                                quizId = quizId,
                                content = summary
                            )
                        )

                        // Save topics and content questions from the LLM processing
                        // This data is used to generate the summary but was not previously saved
                        val topics = generateQuizSummaryUseCase.getLastProcessedTopics()
                        saveTopicsUseCase(topics, quizId)

                        // Parse and save questions using domain model and use cases
                        val questions = parseQuestionsUseCase(questionsJson, quizId)
                        createQuizQuestionsUseCase(questions)

                        state.copy(
                            isLoading = false,
                            currentStep = ProcessingCreateStep.NONE,
                            quizSummary = summary,
                            quizIdInserted = quizId
                        )
                    } catch (e: Exception) {
                        throw e
                    }
                }
            } else if (generateSummary) {
                state =
                    state.copy(currentStep = ProcessingCreateStep.GENERATE_SUMMARY_AND_QUESTIONS)
                val summaryResult =
                    generateQuizSummaryUseCase(
                        fetchedTitle,
                        fetchedThumb,
                        transcriptContent,
                        selectedLanguage
                    )

                if (summaryResult.error != null) {
                    throw IllegalStateException(summaryResult.error)
                }

                val summary = summaryResult.content

                state = state.copy(currentStep = ProcessingCreateStep.SAVE_TO_DATABASE)
                // Save summary to database using domain model and use case
                createQuizSummaryUseCase(
                    Summary(
                        quizId = quizId,
                        content = summary
                    )
                )

                state.copy(
                    isLoading = false,
                    currentStep = ProcessingCreateStep.NONE,
                    quizSummary = summary,
                    quizIdInserted = quizId
                )
            } else if (generateQuestions) {
                state =
                    state.copy(currentStep = ProcessingCreateStep.GENERATE_SUMMARY_AND_QUESTIONS)
                val questionsResult = generateQuestionsUseCase(
                    transcriptContent,
                    selectedLanguage,
                    questionType,
                    numberOfQuestions
                )

                if (questionsResult.error != null) {
                    throw IllegalStateException(questionsResult.error)
                }

                val questionsJson = questionsResult.content

                state = state.copy(currentStep = ProcessingCreateStep.SAVE_TO_DATABASE)
                // Save key points extracted during question generation
                val keyPoints = generateQuestionsUseCase.getLastExtractedKeyPoints()
                saveKeyPointsUseCase(keyPoints, quizId)

                // Parse and save questions using domain model and use cases
                val questions = parseQuestionsUseCase(questionsJson, quizId)
                createQuizQuestionsUseCase(questions)

                state.copy(
                    isLoading = false,
                    currentStep = ProcessingCreateStep.NONE,
                    quizIdInserted = quizId
                )
            } else {
                // Neither summary nor questions - just save the basic quiz info
                state.copy(
                    isLoading = false,
                    currentStep = ProcessingCreateStep.NONE,
                    quizIdInserted = quizId
                )
            }

            state = newState
        }
    }
}
