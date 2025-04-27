package com.nguyenmoclam.tutorialyoutubemadesimple.lib

import com.nguyenmoclam.tutorialyoutubemadesimple.OpenRouterApi
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.Result
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.LLMConfig
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.Message
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.OpenRouterRequest
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.OpenRouterResponse
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.UsageRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.content.Question
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.content.Topic
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.MultipleChoiceQuestion
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.TrueFalseQuestion
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.SecurePreferences
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

/**
 * Processes YouTube video content using Language Learning Models (LLM) to extract and simplify topics and questions.
 * This class handles the interaction with OpenRouter API to analyze video transcripts and generate child-friendly content.
 *
 * The processing workflow consists of two main steps:
 * 1. Topic Extraction: Analyzes the video transcript to identify key topics and generate relevant questions
 * 2. Content Simplification: Transforms the extracted content into child-friendly format with ELI5 explanations
 *
 * The class uses OpenRouter's API with configurable model selection for natural language processing tasks.
 */
class LLMProcessor @Inject constructor(
    private val networkUtils: NetworkUtils,
    private val openRouterApi: OpenRouterApi,
    private val securePreferences: SecurePreferences,
    private val usageRepository: UsageRepository,
    initialConfig: LLMConfig? = null
) {
    /**
     * The current LLM configuration being used for API calls.
     * Defaults to the predefined default configuration.
     */
    private var currentConfig: LLMConfig = initialConfig ?: LLMConfig.DEFAULT
    
    /**
     * Error callback for detailed error reporting.
     * Can be set to receive detailed error information.
     */
    private var errorCallback: ((LLMError) -> Unit)? = null
    
    /**
     * Model unavailability tracking to prevent repeated failures.
     * Maps model IDs to timestamp when they were marked unavailable.
     */
    private val unavailableModels = mutableMapOf<String, Long>()
    
    /**
     * The cooldown period in milliseconds before retrying an unavailable model.
     * Default is 5 minutes.
     */
    private val MODEL_UNAVAILABILITY_COOLDOWN = 5 * 60 * 1000L
    
    /**
     * Sets a new configuration for the LLM processor.
     * 
     * @param config The new configuration to use for future API calls.
     */
    fun setConfig(config: LLMConfig) {
        currentConfig = config
    }
    
    /**
     * Returns the current LLM configuration.
     * 
     * @return The current configuration being used.
     */
    fun getConfig(): LLMConfig {
        return currentConfig
    }
    
    /**
     * Resets the configuration to the default values.
     */
    fun resetConfig() {
        currentConfig = LLMConfig.DEFAULT
    }
    
    /**
     * Sets a callback for receiving detailed error reports.
     * 
     * @param callback The function to call when errors occur.
     */
    fun setErrorCallback(callback: (LLMError) -> Unit) {
        errorCallback = callback
    }
    
    /**
     * Checks if a model is currently marked as unavailable.
     * 
     * @param modelId The ID of the model to check.
     * @return True if the model is unavailable, false otherwise.
     */
    private fun isModelUnavailable(modelId: String): Boolean {
        val timestamp = unavailableModels[modelId] ?: return false
        val now = System.currentTimeMillis()
        
        // If the cooldown period has passed, remove the model from unavailable list
        if (now - timestamp > MODEL_UNAVAILABILITY_COOLDOWN) {
            unavailableModels.remove(modelId)
            return false
        }
        
        return true
    }
    
    /**
     * Marks a model as unavailable for the cooldown period.
     * 
     * @param modelId The ID of the model to mark as unavailable.
     */
    private fun markModelUnavailable(modelId: String) {
        unavailableModels[modelId] = System.currentTimeMillis()
    }

    /**
     * Analyzes a video transcript to identify key topics and generate relevant questions.
     * This function serves as the first step in the content processing pipeline.
     *
     * @param transcript The full text transcript of the YouTube video
     * @param title The title of the YouTube video
     * @return A list of [Topic] objects, each containing a title and up to 3 questions.
     *         Returns at most 5 topics to maintain focus on the most important content.
     */
    suspend fun extractTopicsAndQuestions(
        transcript: String,
        title: String,
        language: String = "English"
    ): List<Topic> {
        val prompt = """
            You are an expert content analyzer. Given a YouTube video transcript, identify at most 5 most interesting topics discussed and generate at most 3 most thought-provoking questions for each topic.
            These questions don't need to be directly asked in the video. It's good to have clarification questions.

            VIDEO TITLE: $title

            TRANSCRIPT:
            $transcript

            LANGUAGE:
            $language

            IMPORTANT INSTRUCTIONS:
            1. You MUST format your response as a valid JSON object
            2. Each topic MUST have a title and questions array
            3. Each topic MUST have at most 3 questions
            4. Return at most 5 topics
            5. Questions should be clear and engaging
            6. DO NOT include any markdown code blocks or additional text
            7. Ensure all strings are properly escaped
            8. All topics and questions MUST be in the specified language: $language

            Expected JSON format:
            {
                "topics": [
                    {
                        "title": "Clear and Concise Topic Title",
                        "questions": [
                            "First thought-provoking question about this topic?",
                            "Second interesting question about this topic?",
                            "Third clarifying question about this topic?"
                        ]
                    }
                ]
            }

            The response MUST be a valid JSON object following exactly this structure.
        """.trimIndent()

        val response = callLLM(prompt)
        return parseTopicsFromJson(response)
    }

    /**
     * Processes the extracted topics to create child-friendly content.
     * This function serves as the second step in the content processing pipeline.
     *
     * @param topics The list of topics extracted from [extractTopicsAndQuestions]
     * @param transcript The original video transcript for context
     * @return A list of processed [Topic] objects with rephrased titles and questions, including simple answers
     *         Returns empty list if input topics is empty
     */
    suspend fun processContent(
        topics: List<Topic>,
        transcript: String,
        language: String = "English"
    ): List<Topic> {
        if (topics.isEmpty()) return emptyList()

        val prompt = """
            You are a content simplifier for children. Given multiple topics and questions from a YouTube video,
            rephrase each topic title and its questions to be clearer, and provide simple ELI5 (Explain Like I'm 5) answers.

            TOPICS AND QUESTIONS:
            ${
            topics.joinToString("\n\n") { topic ->
                """TOPIC: ${topic.title}
                QUESTIONS:
                ${topic.questions.joinToString("\n") { "- ${it.original}" }}""".trimIndent()
            }
        }

            TRANSCRIPT EXCERPT:
            $transcript

            LANGUAGE:
            $language

            For topic titles and questions:
            1. Keep them catchy and interesting, but short
            2. All content MUST be in the specified language: $language

            For your answers:
            1. Format them using HTML with <b> and <i> tags for highlighting.
            2. Prefer lists with <ol> and <li> tags. Ideally, <li> followed by <b> for the key points.
            3. Quote important keywords but explain them in easy-to-understand language (e.g., "<b>Quantum computing</b> is like having a super-fast magical calculator")
            4. Keep answers interesting but short

            IMPORTANT INSTRUCTIONS:
            1. You MUST format your response as a valid JSON object
            2. Each topic MUST have original_title, rephrased_title, and questions array
            3. Each question MUST have original, rephrased, and answer fields
            4. DO NOT include any markdown code blocks or additional text
            5. Ensure all strings are properly escaped
            6. The response MUST be a valid JSON object following exactly this structure

            Expected JSON format:
            ```json
            {
                "topics": [
                    {
                        "original_title": "Original Topic Title",
                        "rephrased_title": "Interesting topic title in 10 words",
                        "questions": [
                            {
                                "original": "Original question from input",
                                "rephrased": "Clearer, child-friendly version of the question",
                                "answer": "Simple, engaging answer with HTML formatting using <b>, <i>, <ol>, and <li> tags"
                            }
                        ]
                    }
                ]
            }
            ```
        """.trimIndent()

        val response = callLLM(prompt)
        return parseBatchProcessedContent(topics, response)
    }

    /**
     * Processes a prompt through the configured LLM to get a response.
     * Handles retries, error classification, and model fallback.
     * 
     * @param prompt The prompt to send to the LLM.
     * @param config Optional configuration override for this specific call.
     * @return The LLM's response as a string.
     */
    private suspend fun callLLM(
        prompt: String,
        config: LLMConfig? = null
    ): String {
        // Check if content should not be loaded based on data saver settings
        if (!networkUtils.shouldLoadContent(highQuality = true)) {
            throw IllegalStateException("Network restricted by data saver settings")
        }
        
        // Use the provided config or fall back to the current config
        var usedConfig = config ?: currentConfig
        
        // Check if the selected model is unavailable and needs a fallback
        if (isModelUnavailable(usedConfig.modelId)) {
            val message = "Model ${usedConfig.modelId} is currently unavailable, falling back to default model"
            errorCallback?.invoke(LLMError.ModelUnavailable(usedConfig.modelId, message))
            usedConfig = LLMConfig.DEFAULT
        }
        
        // Determine which API key to use
        val apiKey = if (usedConfig.apiKey.isNotBlank()) {
            usedConfig.apiKey
        } else {
            // Get API key from secure storage
            securePreferences.getOpenRouterApiKey()
        }
        
        val messages = listOf(
            Message(role = "user", content = prompt)
        )
        
        val request = OpenRouterRequest(
            model = usedConfig.modelId,
            messages = messages,
            max_tokens = usedConfig.maxTokens,
            temperature = usedConfig.temperature,
            top_p = usedConfig.topP
        )
        
        val authHeader = "Bearer $apiKey"
        
        // Implement retry with exponential backoff
        val maxRetries = 3
        var retryCount = 0
        var lastError: Throwable? = null
        
        while (retryCount <= maxRetries) {
            try {
                // Use withConnectionTimeout to apply user's timeout setting
                val result = networkUtils.withConnectionTimeout {
                    openRouterApi.createCompletion(authHeader, request)
                }
                
                // Handle the result - using the custom Result class
                val apiResponse = when (result) {
                    is Result.Success -> {
                        // result.value is Result<OpenRouterResponse>
                        when (val innerResult = result.value) {
                            is Result.Success -> innerResult.value
                            is Result.Failure -> throw innerResult.error
                        }
                    }
                    is Result.Failure -> throw result.error
                }
                
                // Reset unavailability status if the call succeeded
                if (unavailableModels.containsKey(usedConfig.modelId)) {
                    unavailableModels.remove(usedConfig.modelId)
                }
                
                // Record token usage for successful responses
                try {
                    val usageInfo = apiResponse.usage
                    // Create TokenUsage object first
                    val tokenUsage = usageRepository.createTokenUsage(
                        modelId = usedConfig.modelId,
                        modelName = "", // Placeholder: Fetch actual model name if needed
                        promptTokens = usageInfo?.promptTokens ?: 0,
                        completionTokens = usageInfo?.completionTokens ?: 0,
                        promptPrice = 0.0, // Placeholder: Fetch actual prompt price if needed
                        completionPrice = 0.0 // Placeholder: Fetch actual completion price if needed
                    )
                    // Then record it
                    usageRepository.recordTokenUsage(tokenUsage)
                } catch (e: Exception) {
                    // Log the error but don't fail the main operation
                    errorCallback?.invoke(LLMError.TrackingError("Failed to record token usage: ${e.message}"))
                }
                
                // Now apiResponse is OpenRouterResponse
                return apiResponse.choices.firstOrNull()?.message?.content ?: ""
            } catch (e: Exception) {
                lastError = e
                val errorType = classifyError(e)
                
                when (errorType) {
                    ErrorType.MODEL_UNAVAILABLE -> {
                        // Mark model as unavailable and try again with default model
                        markModelUnavailable(usedConfig.modelId)
                        val fallbackError = LLMError.ModelUnavailable(
                            usedConfig.modelId,
                            "Model unavailable: ${e.message}. Falling back to default model."
                        )
                        errorCallback?.invoke(fallbackError)
                        
                        // Only fallback if we're not already using the default model
                        if (usedConfig.modelId != LLMConfig.DEFAULT.modelId) {
                            usedConfig = LLMConfig.DEFAULT
                            continue  // Skip the retry delay and retry immediately with fallback
                        }
                    }
                    ErrorType.TEMPORARY -> {
                        // Decide whether to retry or break BEFORE incrementing count
                        if (retryCount < maxRetries) {
                            retryCount++ // Increment only if we are actually retrying
                            // Log the retry attempt count correctly
                            val retryError = LLMError.TemporaryError(
                                "Temporary error: ${lastError?.message}. Retrying ($retryCount/$maxRetries)"
                            )
                            errorCallback?.invoke(retryError)
                            
                            // Exponential backoff using the updated retryCount
                            val delayTime = 500L * (1 shl (retryCount - 1))
                            kotlinx.coroutines.delay(delayTime)
                        } else {
                            // Permanent error OR max retries reached
                            if (errorType == ErrorType.PERMANENT) {
                                // Log permanent error if it hasn't been thrown yet
                                val permanentError = LLMError.PermanentError(
                                    "Permanent error: ${lastError?.message}. API call failed."
                                )
                                errorCallback?.invoke(permanentError)
                                throw lastError // Throw the original error for permanent issues
                            }
                            // If it wasn't permanent, it means retries are exhausted
                            break // Exit the loop
                        }
                    }
                    ErrorType.PERMANENT -> {
                        // Permanent error, no point in retrying
                        val permanentError = LLMError.PermanentError(
                            "Permanent error: ${e.message}. API call failed."
                        )
                        errorCallback?.invoke(permanentError)
                        throw e
                    }
                }
            }
        }
        
        // If we've reached here, all retries failed
        val exhaustedError = LLMError.RetriesExhausted(
            "Failed after $maxRetries retries: ${lastError?.message}"
        )
        errorCallback?.invoke(exhaustedError)
        throw lastError ?: IllegalStateException("API call failed after retries")
    }
    
    /**
     * Classifies an error to determine if it's temporary, permanent, or model-specific.
     * 
     * @param error The error to classify.
     * @return The ErrorType classification.
     */
    private fun classifyError(error: Throwable): ErrorType {
        return when {
            // Model unavailable errors
            error.message?.contains("model_not_found") == true -> ErrorType.MODEL_UNAVAILABLE
            error.message?.contains("invalid model") == true -> ErrorType.MODEL_UNAVAILABLE
            error.message?.contains("model capacity") == true -> ErrorType.MODEL_UNAVAILABLE
            error.message?.contains("not available") == true -> ErrorType.MODEL_UNAVAILABLE
            
            // Temporary errors
            error is java.net.SocketTimeoutException -> ErrorType.TEMPORARY
            error is java.io.IOException -> ErrorType.TEMPORARY
            error.message?.contains("timeout") == true -> ErrorType.TEMPORARY
            error.message?.contains("429") == true -> ErrorType.TEMPORARY  // Too many requests
            error.message?.contains("503") == true -> ErrorType.TEMPORARY  // Service unavailable
            error.message?.contains("502") == true -> ErrorType.TEMPORARY  // Bad gateway
            
            // Permanent errors
            error.message?.contains("401") == true -> ErrorType.PERMANENT  // Unauthorized
            error.message?.contains("403") == true -> ErrorType.PERMANENT  // Forbidden
            error.message?.contains("400") == true -> ErrorType.PERMANENT  // Bad request
            error.message?.contains("404") == true -> ErrorType.PERMANENT  // Not found
            
            // Default to temporary for unknown errors
            else -> ErrorType.TEMPORARY
        }
    }
    
    /**
     * Error types for classifying API errors.
     */
    private enum class ErrorType {
        TEMPORARY,      // Can be retried
        PERMANENT,      // Should not be retried
        MODEL_UNAVAILABLE // Model-specific error, should fallback
    }

    /**
     * Parses the JSON response from the initial topic extraction into Topic objects using kotlinx.serialization.
     *
     * @param jsonResponse The JSON string response from the LLM
     * @return A list of [Topic] objects, limited to 5 topics with 3 questions each
     */
    private fun parseTopicsFromJson(jsonResponse: String): List<Topic> {
        val cleanJson = if (jsonResponse.contains("```")) {
            jsonResponse.substringAfter("```json").substringBefore("```").trim()
        } else {
            jsonResponse.trim()
        }

        return try {
            val json = Json.parseToJsonElement(cleanJson).jsonObject
            val topicsArray = json["topics"]?.jsonArray ?: return emptyList()

            val limitedTopicsArray = if (topicsArray.size > 5) topicsArray.take(5) else topicsArray

            limitedTopicsArray.mapNotNull { topicElement ->
                val topicObj = topicElement.jsonObject
                val title = topicObj["title"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val questionsArray = topicObj["questions"]?.jsonArray ?: return@mapNotNull null

                val limitedQuestionsArray =
                    if (questionsArray.size > 3) questionsArray.take(3) else questionsArray

                val questions = limitedQuestionsArray.map { questionElement ->
                    questionElement.jsonPrimitive.content.let { Question(it) }
                }
                Topic(title = title, questions = questions)
            }
        } catch (e: Exception) {
            println("parseTopicsFromJson error: ${e.message}")
            emptyList()
        }
    }

    /**
     * Parses the JSON response from the content simplification into updated Topic objects using kotlinx.serialization.
     *
     * @param originalTopics The original list of topics
     * @param jsonResponse The JSON string response from the LLM containing simplified content
     */
    private fun parseBatchProcessedContent(
        originalTopics: List<Topic>,
        jsonResponse: String
    ): List<Topic> {
        val cleanJson = if (jsonResponse.contains("```")) {
            jsonResponse.substringAfter("```json").substringBefore("```").trim()
        } else {
            jsonResponse.trim()
        }

        return try {
            val json = Json.parseToJsonElement(cleanJson).jsonObject
            val topicsArray = json["topics"]?.jsonArray ?: return originalTopics

            val processedTopicsMap = topicsArray.associate { topicElement ->
                val topicObj = topicElement.jsonObject
                val originalTitle = topicObj["original_title"]?.jsonPrimitive?.content ?: ""
                val rephrasedTitle =
                    topicObj["rephrased_title"]?.jsonPrimitive?.content ?: originalTitle
                val questionsArray = topicObj["questions"]?.jsonArray

                originalTitle to Pair(rephrasedTitle, questionsArray)
            }

            originalTopics.map { originalTopic ->
                val (rephrasedTitle, questionsArray) = processedTopicsMap[originalTopic.title]
                    ?: return@map originalTopic

                val processedQuestions = originalTopic.questions.map { originalQuestion ->
                    val processedQuestion = questionsArray
                        ?.find {
                            it.jsonObject["original"]?.jsonPrimitive?.content == originalQuestion.original
                        }
                        ?.jsonObject

                    Question(
                        original = originalQuestion.original,
                        rephrased = processedQuestion?.get("rephrased")?.jsonPrimitive?.content
                            ?: originalQuestion.original,
                        answer = processedQuestion?.get("answer")?.jsonPrimitive?.content ?: ""
                    )
                }

                originalTopic.copy(
                    rephrased_title = rephrasedTitle,
                    questions = processedQuestions
                )
            }
        } catch (e: Exception) {
            println("parseBatchProcessedContent error: ${e.message}")
            originalTopics
        }
    }

    private fun parseKeyPointsFromJson(jsonStr: String): List<String> {
        return try {
            val json = Json.parseToJsonElement(jsonStr).jsonObject
            json["key_points"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
        } catch (e: Exception) {
            println("parseKeyPointsFromJson error: ${e.message}")
            emptyList()
        }
    }

    suspend fun extractKeyPoints(transcript: String, language: String): List<String> {
        val prompt = """
            You are an expert content analyzer. Given a YouTube video transcript, identify the key points or important facts discussed in the video. These key points will be used to generate questions later.

            TRANSCRIPT:
            $transcript

            LANGUAGE:
            $language

            INSTRUCTIONS:
            1. Identify at most 10 key points or important facts.
            2. Each key point should be concise and clear.
            3. Format the response as a valid JSON object with the following structure:
               {
                 "key_points": [
                   "Key point 1",
                   "Key point 2"
                 ]
               }
            Ensure the response is a valid JSON object following the specified structure.
        """.trimIndent()

        val response = callLLM(prompt)
        val cleanJson = if (response.contains("```")) {
            response.substringAfter("```json").substringBefore("```").trim()
        } else {
            response.trim()
        }

        return parseKeyPointsFromJson(cleanJson)
    }

    suspend fun generateQuestionsFromKeyPoints(
        keyPoints: List<String>,
        language: String,
        questionType: String,
        numberOfQuestions: Int
    ): String {
        val keyPointsText = keyPoints.joinToString("\n")
        val prompt = """
            You are an expert in creating educational questions. Given a list of key points from a YouTube video transcript, generate questions based on these key points. The questions should be in the specified language, of the specified type, and limited to the specified number.

            KEY POINTS:
            $keyPointsText

            LANGUAGE:
            $language

            QUESTION TYPE:
            $questionType

            NUMBER OF QUESTIONS:
            $numberOfQuestions

            INSTRUCTIONS:
            1. Generate exactly $numberOfQuestions questions based on the provided key points.
            2. All questions and answers must be in $language.
            3. For multiple-choice questions:
                - Provide 4 options labeled as A, B, C, D.
                - Indicate the correct answer(s) using the labels (e.g., "A" or ["A", "C"]).
                - For single-answer questions, there should be exactly one correct answer.
                - For multiple-answer questions, there can be more than one correct answer.
            4. For True/False questions:
               - Provide a statement and indicate whether it is true or false.
            5. Ensure the questions are directly related to the key points.
            6. Format the response as a valid JSON object with the following structure:

            For multiple-choice questions:
            {
              "questions": [
                {
                  "question": "Question text",
                  "options": {
                    "A": "Option 1",
                    "B": "Option 2",
                    "C": "Option 3",
                    "D": "Option 4"
                  },
                  "correct_answers": ["A"] // or ["A", "C"] for multiple answers
                }
              ]
            }

            For True/False questions:
            {
              "questions": [
                {
                  "statement": "Statement text",
                  "is_true": true/false
                }
              ]
            }

            Ensure the response is a valid JSON object following the specified structure.
        """.trimIndent()

        return callLLM(prompt)
    }

    fun parseQuizQuestions(jsonResponse: String): Pair<List<MultipleChoiceQuestion>, List<TrueFalseQuestion>> {
        val cleanJson = if (jsonResponse.contains("```")) {
            jsonResponse.substringAfter("```json").substringBefore("```").trim()
        } else {
            jsonResponse.trim()
        }

        return try {
            parseQuestionsFromJson(cleanJson)
        } catch (e: Exception) {
            println("parseQuizQuestions error: ${e.message}")
            Pair(emptyList(), emptyList())
        }
    }

    private fun parseQuestionsFromJson(jsonStr: String): Pair<List<MultipleChoiceQuestion>, List<TrueFalseQuestion>> {
        val json = Json.parseToJsonElement(jsonStr).jsonObject
        val questionsArray = json["questions"]?.jsonArray ?: return Pair(emptyList(), emptyList())

        val multipleChoiceQuestions = mutableListOf<MultipleChoiceQuestion>()
        val trueFalseQuestions = mutableListOf<TrueFalseQuestion>()

        questionsArray.forEach { questionElement ->
            val questionObj = questionElement.jsonObject

            when {
                // Multiple-choice
                questionObj.containsKey("options") -> {
                    val question = questionObj["question"]?.jsonPrimitive?.content ?: return@forEach
                    val optionsObj = questionObj["options"]?.jsonObject ?: return@forEach
                    val correctAnswersArray =
                        questionObj["correct_answers"]?.jsonArray ?: return@forEach

                    val options = optionsObj.entries.associate {
                        it.key to it.value.jsonPrimitive.content
                    }
                    val correctAnswers = correctAnswersArray.map { it.jsonPrimitive.content }

                    multipleChoiceQuestions.add(
                        MultipleChoiceQuestion(
                            question = question,
                            options = options,
                            correctAnswers = correctAnswers
                        )
                    )
                }
                // True/False
                questionObj.containsKey("statement") -> {
                    val statement =
                        questionObj["statement"]?.jsonPrimitive?.content ?: return@forEach
                    val isTrue = questionObj["is_true"]?.jsonPrimitive?.content?.toBoolean()
                        ?: return@forEach

                    trueFalseQuestions.add(
                        TrueFalseQuestion(
                            statement = statement,
                            isTrue = isTrue
                        )
                    )
                }
            }
        }

        return Pair(multipleChoiceQuestions, trueFalseQuestions)
    }

    /**
     * Analyzes a video transcript and extracts the main key points for mind map generation.
     * @param transcript The full text transcript of the YouTube video.
     * @param title The title of the YouTube video.
     * @return A list of key point strings (at most 5) representing the core ideas of the video.
     */
    suspend fun extractKeyPointsForMindMap(
        transcript: String,
        title: String,
        language: String = "English"
    ): List<String> {
        val prompt = """
        You are an expert content analyzer specializing in educational content. Given a YouTube video transcript, identify the main key points or core concepts discussed in the video. These key points will be used to create a mind map for educational purposes.

        VIDEO TITLE: $title

        TRANSCRIPT:
        $transcript

        LANGUAGE:
        $language

        IMPORTANT INSTRUCTIONS:
        1. You MUST format your response as a valid JSON object.
        2. Include an array field "key_points" containing the key points.
        3. Analyze the content depth and complexity to determine the appropriate number of key points:
           - For short or simple content: Extract 3-5 key points
           - For medium-length content: Extract 5-7 key points
           - For long or complex content: Extract 7-10 key points
        4. Each key point should be a clear, concise sentence (10-15 words maximum).
        5. Ensure key points are factually accurate based on the transcript content.
        6. Key points should be distinct from each other and cover different aspects of the content.
        7. Arrange key points in a logical order that follows the content's natural progression.
        8. DO NOT include any markdown code blocks or additional text.
        9. Ensure all strings are properly escaped.
        10. All key points MUST be in the specified language: $language

        Expected JSON format:
        {
            "key_points": [
                "First key point or main idea...",
                "Second key point...",
                "Third key point...",
                "And so on based on content complexity..."
            ]
        }

        The response MUST be a valid JSON object following exactly this structure.
    """.trimIndent()

        val response = callLLM(prompt)
        return parseKeyPointsFromJsonToListString(response)
    }

    /**
     * Generates Mermaid mind map diagram code from a list of key points.
     * @param keyPoints The list of key point strings extracted from the transcript.
     * @param title The title of the YouTube video (used as the central node).
     * @return A string containing the Mermaid mindmap code representing the title and its key points.
     */
    suspend fun generateMermaidMindMapCode(
        keyPoints: List<String>,
        title: String,
        language: String = "English"
    ): String {
        if (keyPoints.isEmpty()) return ""

        // Adjust number of sub-points based on number of key points
        val subPointsPerKeyPoint = when {
            keyPoints.size <= 5 -> "2-3"
            keyPoints.size <= 7 -> "2"
            else -> "1-2"
        }

        val prompt = """
        You are an expert at generating educational mind maps. Based on the given video title and key points, produce a well-structured Mermaid mind map diagram that clearly visualizes the relationships between concepts.

        VIDEO TITLE: $title

        KEY POINTS:
        ${keyPoints.joinToString("\n") { "- $it" }}

        LANGUAGE:
        $language

        IMPORTANT INSTRUCTIONS:
        1. Use the video title as the central root node of the mind map.
        2. Each key point should be a top-level node branching from the title.
        3. For each key point, add $subPointsPerKeyPoint relevant sub-points that elaborate on that key point (derived from the key point itself).
        4. Use appropriate icons where relevant (e.g., 💡 for ideas, 🔑 for key concepts, 📊 for data points).
        5. Keep the mind map simple and standard - DO NOT use any styling, class definitions, or node IDs.
        6. Ensure the mind map is well-balanced with similar depth across branches.
        7. Output the diagram in Mermaid syntax inside a markdown code block labeled 'mermaid'.
        8. DO NOT include any explanation or text outside the Mermaid code block.
        9. All text in the mind map MUST be in the specified language: $language

        Expected Mermaid mindmap format:
        ```mermaid
        mindmap
            root(($title))
              1[First key point]
                1.1[Sub-point 1.1]
                1.2[Sub-point 1.2]
              2[Second key point]
                2.1[Sub-point 2.1]
                2.2[Sub-point 2.2]
              ... and so on for all key points
        ```

        The response MUST contain only the Mermaid mind map code in the format above without any styling, class definitions, or node IDs.
    """.trimIndent()

        val response = callLLM(prompt)
        // Extract the Mermaid code block content from the LLM response
        val code = if (response.contains("```")) {
            if (response.contains("```mermaid")) {
                response.substringAfter("```mermaid").substringBefore("```").trim()
            } else {
                response.substringAfter("```").substringBefore("```").trim()
            }
        } else {
            response.trim()
        }
        return code
    }

    /**
     * Fixes Mermaid mind map code that has syntax errors.
     * @param originalCode The original Mermaid code with errors.
     * @param errorMessage The error message describing the syntax issue.
     * @param language The language of the content.
     * @return A string containing the corrected Mermaid mindmap code.
     */
    suspend fun fixMindMapCode(
        originalCode: String,
        errorMessage: String,
        language: String = "English"
    ): String {
        val prompt = """
        You are an expert at fixing Mermaid syntax errors in mind maps. I have a Mermaid mind map diagram with syntax errors that needs to be fixed.

        ORIGINAL MERMAID CODE WITH ERRORS:
        ```mermaid
        ${originalCode.trim()}
        ```

        ERROR MESSAGE:
        $errorMessage

        LANGUAGE:
        $language

        IMPORTANT INSTRUCTIONS:
        1. Carefully analyze the error message and identify the syntax issues in the code.
        2. Fix ONLY the syntax errors while preserving the content and structure of the mind map.
        3. Ensure the fixed code follows proper Mermaid mindmap syntax (including correct indentation and one node per line).
        4. If a node contains special characters (like &, (), emoji, or internal quotes), enclose the entire node text in double quotes.
        5. If the node text contains parentheses (), which causes a syntax error, replace them with single quotes ' ' (e.g., (example) → 'example').
        6. Keep all text in the specified language: $language.
        7. Do not add or remove nodes, nor rename any existing text. Only fix syntax issues.
        8. Return ONLY the fixed Mermaid code without any markdown formatting, explanations, or comments.

        The response should contain only the corrected Mermaid mindmap code.
        """.trimIndent()

        val response = callLLM(prompt)
        // Extract the Mermaid code from the LLM response
        val fixedCode = if (response.contains("```")) {
            if (response.contains("```mermaid")) {
                response.substringAfter("```mermaid").substringBefore("```").trim()
            } else {
                response.substringAfter("```").substringBefore("```").trim()
            }
        } else {
            response.trim()
        }
        return fixedCode
    }

    /**
     * Represents errors that can occur during LLM operations.
     */
    sealed class LLMError {
        /**
         * Error when a model is unavailable and fallback is used.
         */
        data class ModelUnavailable(val modelId: String, val message: String) : LLMError()
        
        /**
         * Error for temporary issues that were retried.
         */
        data class TemporaryError(val message: String) : LLMError()
        
        /**
         * Error for permanent issues that cannot be resolved with retries.
         */
        data class PermanentError(val message: String) : LLMError()
        
        /**
         * Error when all retries are exhausted.
         */
        data class RetriesExhausted(val message: String) : LLMError()
        
        /**
         * Error when tracking token usage fails.
         */
        data class TrackingError(val message: String) : LLMError()
    }
    
    /**
     * Parses the JSON response from the key point extraction prompt into a list of key point strings.
     * @param jsonResponse The raw JSON string response from the LLM.
     * @return List of key point texts (max 5) or an empty list if parsing fails or no key points found.
     */
    private fun parseKeyPointsFromJsonToListString(jsonResponse: String): List<String> {
        // Remove any markdown formatting (e.g., code fences) if present
        val cleanJson = if (jsonResponse.contains("```")) {
            jsonResponse.substringAfter("```json").substringBefore("```").trim()
        } else {
            jsonResponse.trim()
        }

        return try {
            val json = Json.parseToJsonElement(cleanJson).jsonObject
            val pointsArray = json["key_points"]?.jsonArray ?: return emptyList()
            // Limit to at most 5 key points
            val limitedPointsArray = if (pointsArray.size > 5) pointsArray.take(5) else pointsArray
            limitedPointsArray.mapNotNull { pointElement ->
                pointElement.jsonPrimitive.content.trim().takeIf { it.isNotEmpty() }
            }
        } catch (e: Exception) {
            println("parseKeyPointsFromJson error: ${e.message}")
            emptyList()
        }
    }
}
