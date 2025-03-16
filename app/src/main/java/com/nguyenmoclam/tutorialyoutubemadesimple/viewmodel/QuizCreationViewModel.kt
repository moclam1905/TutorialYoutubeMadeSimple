package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.YouTubeApi
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Question
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Summary
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.HtmlGenerator
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.LLMProcessor
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.Section
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.YouTubeTranscriptLight
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
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
        return when(this) {
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
    private val youTubeApiService: YouTubeApi,
    private val quizRepository: QuizRepository
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
        state = QuizState(isLoading = true, currentStep = ProcessingCreateStep.FETCH_METADATA)

        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val videoId = extractVideoId(videoUrlOrId)
                ?: throw IllegalArgumentException(context.getString(R.string.error_invalid_url))

            val videoResponse = youTubeApiService.getVideoInfo(videoId, youtubeApiKey)
            val snippet = videoResponse.items.firstOrNull()?.snippet
            val fetchedTitle = snippet?.title ?: context.getString(R.string.default_no_title)
            val fetchedThumb = snippet?.thumbnails?.run {
                maxres?.url ?: high?.url ?: medium?.url ?: default?.url
            } ?: ""
            val fetchedDescription = snippet?.description ?: "No description available"

            state = state.copy(currentStep = ProcessingCreateStep.FETCH_TRANSCRIPT)
            val transcripts = YouTubeTranscriptLight.create().getTranscript(videoId)
            val transcriptContent = transcripts.joinToString(" ") { it.text }

            // Create quiz domain model
            val quiz = Quiz(
                title = fetchedTitle,
                description = fetchedDescription,
                videoUrl = videoUrlOrId,
                language = selectedLanguage,
                questionType = questionType,
                questionCount = numberOfQuestions,
                summaryEnabled = generateSummary,
                questionsEnabled = generateQuestions,
                lastUpdated = System.currentTimeMillis()
            )

            // Insert quiz and get its ID
            val quizId = quizRepository.insertQuiz(quiz)

            val newState = if (generateSummary && generateQuestions) {
                supervisorScope {
                    state = state.copy(currentStep = ProcessingCreateStep.GENERATE_SUMMARY_AND_QUESTIONS)
                    
                    val summaryDeferred = async {
                        createSummary(fetchedTitle, fetchedThumb, transcriptContent)
                    }
                    
                    val questionsDeferred = async {
                        createQuestions(transcriptContent, selectedLanguage, questionType, numberOfQuestions)
                    }
                    
                    try {
                        val summary = summaryDeferred.await()
                        val questions = questionsDeferred.await()
                        
                        state = state.copy(currentStep = ProcessingCreateStep.SAVE_TO_DATABASE)
                        // Save summary to database using domain model
                        quizRepository.insertSummary(Summary(
                            quizId = quizId,
                            content = summary
                        ))

                        // Parse and save questions using domain model
                        val questionEntities = parseAndCreateQuestions(questions, quizId)
                        quizRepository.insertQuestions(questionEntities)

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
                state = state.copy(currentStep = ProcessingCreateStep.GENERATE_SUMMARY_AND_QUESTIONS)
                val summary = createSummary(fetchedTitle, fetchedThumb, transcriptContent)
                
                state = state.copy(currentStep = ProcessingCreateStep.SAVE_TO_DATABASE)
                // Save summary to database using domain model
                quizRepository.insertSummary(Summary(
                    quizId = quizId,
                    content = summary
                ))

                state.copy(
                    isLoading = false,
                    currentStep = ProcessingCreateStep.NONE,
                    quizSummary = summary
                )
            } else if (generateQuestions) {
                state = state.copy(currentStep = ProcessingCreateStep.GENERATE_SUMMARY_AND_QUESTIONS)
                val questions = createQuestions(transcriptContent, selectedLanguage, questionType, numberOfQuestions)
                
                state = state.copy(currentStep = ProcessingCreateStep.SAVE_TO_DATABASE)
                // Parse and save questions using domain model
                val questionEntities = parseAndCreateQuestions(questions, quizId)
                quizRepository.insertQuestions(questionEntities)

                state.copy(
                    isLoading = false,
                    currentStep = ProcessingCreateStep.NONE,
                    quizIdInserted = quizId
                )
            } else {
                state.copy(isLoading = false, currentStep = ProcessingCreateStep.NONE)
            }

            state = newState
        }
    }

    private fun parseAndCreateQuestions(questionsJson: String, quizId: Long): List<Question> {
        val processor = LLMProcessor()
        val (multipleChoiceQuestions, trueFalseQuestions) = processor.parseQuizQuestions(questionsJson)
        
        val questions = mutableListOf<Question>()
        
        // Convert multiple choice questions to domain model
        questions.addAll(multipleChoiceQuestions.map { mcq ->
            Question(
                quizId = quizId,
                text = mcq.question,
                options = mcq.options.values.toList(),
                correctAnswer = mcq.correctAnswers.joinToString(",")
            )
        })
        
        // Convert true/false questions to domain model
        questions.addAll(trueFalseQuestions.map { tfq ->
            Question(
                quizId = quizId,
                text = tfq.statement,
                options = listOf("True", "False"),
                correctAnswer = if (tfq.isTrue) "True" else "False"
            )
        })
        
        return questions
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
