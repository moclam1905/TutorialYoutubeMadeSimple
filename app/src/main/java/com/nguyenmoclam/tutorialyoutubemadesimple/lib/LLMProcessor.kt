package com.nguyenmoclam.tutorialyoutubemadesimple.lib

import com.nguyenmoclam.tutorialyoutubemadesimple.ApiService
import com.nguyenmoclam.tutorialyoutubemadesimple.MainActivity.Companion.OPENROUTER_API_KEY
import com.nguyenmoclam.tutorialyoutubemadesimple.Message
import com.nguyenmoclam.tutorialyoutubemadesimple.OpenRouterRequest
import com.nguyenmoclam.tutorialyoutubemadesimple.Question
import com.nguyenmoclam.tutorialyoutubemadesimple.Topic
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive


/**
 * Processes YouTube video content using Language Learning Models (LLM) to extract and simplify topics and questions.
 * This class handles the interaction with OpenRouter API to analyze video transcripts and generate child-friendly content.
 * 
 * The processing workflow consists of two main steps:
 * 1. Topic Extraction: Analyzes the video transcript to identify key topics and generate relevant questions
 * 2. Content Simplification: Transforms the extracted content into child-friendly format with ELI5 explanations
 *
 * The class uses OpenRouter's API with Gemini model for natural language processing tasks.
 */
class LLMProcessor {
    /**
     * Analyzes a video transcript to identify key topics and generate relevant questions.
     * This function serves as the first step in the content processing pipeline.
     * 
     * The function performs the following steps:
     * 1. Constructs a prompt for the LLM to analyze the transcript
     * 2. Sends the prompt to OpenRouter API using Gemini model
     * 3. Parses the JSON response into structured Topic objects
     * 
     * @param transcript The full text transcript of the YouTube video
     * @param title The title of the YouTube video
     * @return A list of [Topic] objects, each containing a title and up to 3 questions.
     *         Returns at most 5 topics to maintain focus on the most important content.
     * 
     * Example usage:
     * ```kotlin
     * val processor = LLMProcessor()
     * val topics = processor.extractTopicsAndQuestions(
     *     transcript = "Video transcript text...",
     *     title = "How to Build a Website"
     * )
     * ```
     */
    suspend fun extractTopicsAndQuestions(transcript: String, title: String): List<Topic> {
        // Construct a detailed prompt for the LLM to analyze the video content
        // The prompt includes specific instructions for JSON formatting and content structure
        val prompt = """
            You are an expert content analyzer. Given a YouTube video transcript, identify at most 5 most interesting topics discussed and generate at most 3 most thought-provoking questions for each topic.
            These questions don't need to be directly asked in the video. It's good to have clarification questions.

            VIDEO TITLE: $title

            TRANSCRIPT:
            $transcript

            IMPORTANT INSTRUCTIONS:
            1. You MUST format your response as a valid JSON object
            2. Each topic MUST have a title and questions array
            3. Each topic MUST have at most 3 questions
            4. Return at most 5 topics
            5. Questions should be clear and engaging
            6. DO NOT include any markdown code blocks or additional text
            7. Ensure all strings are properly escaped

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

        // Call the LLM API and parse the response
        val response = callLLM(prompt)
        return parseTopicsFromJson(response)
    }

    /**
     * Processes the extracted topics to create child-friendly content.
     * This function serves as the second step in the content processing pipeline.
     * 
     * The function performs the following steps:
     * 1. Combines all topics and questions into a single prompt
     * 2. Requests the LLM to simplify and explain the content
     * 3. Processes the response to update the original topics with simplified versions
     * 
     * Key features of the processing:
     * - Rephrases topics and questions in simpler language
     * - Generates ELI5 (Explain Like I'm 5) answers
     * - Maintains HTML formatting for better presentation
     * - Preserves the original content while adding simplified versions
     * 
     * @param topics The list of topics extracted from [extractTopicsAndQuestions]
     * @param transcript The original video transcript for context
     * @return A list of processed [Topic] objects with rephrased titles and questions, including simple answers
     *         Returns empty list if input topics is empty
     * 
     * Example usage:
     * ```kotlin
     * val simplifiedTopics = processor.processContent(
     *     topics = extractedTopics,
     *     transcript = videoTranscript
     * )
     * ```
     */
    suspend fun processContent(topics: List<Topic>, transcript: String): List<Topic> {
        if (topics.isEmpty()) return emptyList()

        // Combine all topics into a single prompt for batch processing
        // This approach is more efficient than processing topics individually
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

            For topic titles and questions:
            1. Keep them catchy and interesting, but short

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
     * Makes an API call to OpenRouter using the Gemini model.
     * This function handles the communication with the LLM service.
     * 
     * The function uses google/gemini-2.0-flash-thinking-exp:free model which is optimized for:
     * - Fast response times
     * - Structured output generation
     * - Natural language understanding
     * 
     * @param prompt The formatted prompt to send to the LLM
     * @return The raw response string from the LLM
     */
    private suspend fun callLLM(prompt: String): String {
        // Prepare the message in the format expected by OpenRouter API
        val messages = listOf(
            Message(role = "user", content = prompt)
        )
        val request = OpenRouterRequest(
            model = "google/gemini-2.0-flash-thinking-exp:free",
            messages = messages
        )
        val authHeader = "Bearer $OPENROUTER_API_KEY"
        
        // Make the API call and extract the response content
        val response = ApiService.openRouterApi.createCompletion(authHeader, request)
        return response.choices.firstOrNull()?.message?.content ?: ""
    }

    /**
     * Parses the JSON response from the initial topic extraction into Topic objects.
     * This function handles the complex task of converting raw JSON into structured data.
     * 
     * Key features:
     * - Handles both raw JSON and JSON within code blocks
     * - Enforces topic and question limits
     * - Provides null safety through careful parsing
     * - Maintains data integrity during transformation
     * 
     * @param jsonResponse The JSON string response from the LLM
     * @return A list of [Topic] objects, limited to 5 topics with 3 questions each
     * @throws Exception if JSON parsing fails or required fields are missing
     */
    private fun parseTopicsFromJson(jsonResponse: String): List<Topic> {
        // Clean the JSON response by removing any markdown code block markers
        val cleanJson = if (jsonResponse.contains("```")) {
            jsonResponse.substringAfter("```json").substringBefore("```").trim()
        } else {
            jsonResponse.trim()
        }

        // Parse the JSON string into a structured object
        val json = Json.parseToJsonElement(cleanJson).jsonObject
        val topicsArray = json["topics"]?.jsonArray ?: return emptyList()

        // Enforce the maximum limit of 5 topics
        val limitedTopicsArray = if (topicsArray.size > 5) topicsArray.take(5) else topicsArray

        // Transform each JSON topic element into a Topic object
        return limitedTopicsArray.mapNotNull { topicElement ->
            val topicObj = topicElement.jsonObject
            
            // Extract required fields with null safety
            val title = topicObj["title"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val questionsArray = topicObj["questions"]?.jsonArray ?: return@mapNotNull null

            // Enforce the maximum limit of 3 questions per topic
            val limitedQuestionsArray =
                if (questionsArray.size > 3) questionsArray.take(3) else questionsArray

            // Transform question elements into Question objects
            val questions = limitedQuestionsArray.map { questionElement ->
                questionElement.jsonPrimitive.content.let { Question(it) }
            }

            Topic(title = title, questions = questions)
        }
    }

    /**
     * Processes the JSON response from the content simplification into updated Topic objects.
     * This function handles the complex task of matching and updating original topics with their simplified versions.
     * 
     * The processing involves:
     * 1. Cleaning and parsing the JSON response
     * 2. Creating a lookup map for efficient topic matching
     * 3. Processing each original topic with its simplified version
     * 4. Maintaining original content when processing fails
     * 
     * @param originalTopics The original list of topics to be processed
     * @param jsonResponse The JSON string response from the LLM containing simplified content
     * @return A list of [Topic] objects with updated content (rephrased titles, questions, and answers)
     *         If processing fails for any topic, returns the original topic unchanged
     */
    private fun parseBatchProcessedContent(
        originalTopics: List<Topic>,
        jsonResponse: String
    ): List<Topic> {
        // Clean the JSON response by removing any markdown code block markers
        val cleanJson = if (jsonResponse.contains("```")) {
            jsonResponse.substringAfter("```json").substringBefore("```").trim()
        } else {
            jsonResponse.trim()
        }

        // Parse the JSON string into a structured object
        val json = Json.parseToJsonElement(cleanJson).jsonObject
        val topicsArray = json["topics"]?.jsonArray ?: return originalTopics

        // Create a map for efficient lookup of processed topics by their original title
        val processedTopicsMap = topicsArray.associate { topicElement ->
            val topicObj = topicElement.jsonObject
            val originalTitle = topicObj["original_title"]?.jsonPrimitive?.content ?: ""
            val rephrasedTitle =
                topicObj["rephrased_title"]?.jsonPrimitive?.content ?: originalTitle
            val questionsArray = topicObj["questions"]?.jsonArray

            originalTitle to Pair(rephrasedTitle, questionsArray)
        }

        // Process each original topic with its corresponding simplified version
        return originalTopics.map { originalTopic ->
            // Look up the processed version of the topic
            val (rephrasedTitle, questionsArray) = processedTopicsMap[originalTopic.title]
                ?: return@map originalTopic // Keep original if no processed version found

            // Process each question in the topic
            val processedQuestions = originalTopic.questions.map { originalQuestion ->
                // Find the processed version of the question
                val processedQuestion = questionsArray
                    ?.find { it.jsonObject["original"]?.jsonPrimitive?.content == originalQuestion.original }
                    ?.jsonObject

                // Create a new Question object with processed content
                Question(
                    original = originalQuestion.original,
                    rephrased = processedQuestion?.get("rephrased")?.jsonPrimitive?.content
                        ?: originalQuestion.original,
                    answer = processedQuestion?.get("answer")?.jsonPrimitive?.content ?: ""
                )
            }

            // Create a new Topic object with processed content
            originalTopic.copy(
                rephrased_title = rephrasedTitle,
                questions = processedQuestions
            )
        }
    }
}