package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import android.content.Context
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
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.keypoint.SaveKeyPointsUseCase
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
    private val saveTopicsUseCase: SaveTopicsUseCase,
    private val saveKeyPointsUseCase: SaveKeyPointsUseCase
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
                questionCount = numberOfQuestions,
                summaryEnabled = generateSummary,
                questionsEnabled = generateQuestions,
                lastUpdated = System.currentTimeMillis()
            )

            // Insert quiz and get its ID using the use case
            val quizId = createQuizUseCase(quiz)

            // Save transcript to database
            saveTranscriptUseCase(
                Transcript(
                    quizId = quizId,
                    content = transcriptContent,
                    language = selectedLanguage
                )
            )

            val newState = if (generateSummary && generateQuestions) {
                supervisorScope {
                    state =
                        state.copy(currentStep = ProcessingCreateStep.GENERATE_SUMMARY_AND_QUESTIONS)

                    val summaryDeferred = async {
                        val result = generateQuizSummaryUseCase(
                            fetchedTitle,
                            fetchedThumb,
                            transcriptContent
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
                    generateQuizSummaryUseCase(fetchedTitle, fetchedThumb, transcriptContent)

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
