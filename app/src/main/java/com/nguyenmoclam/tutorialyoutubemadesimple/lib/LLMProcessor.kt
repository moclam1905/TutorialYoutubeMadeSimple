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
 * Data class representing a multiple choice question
 * @property question The question text
 * @property options Map of option labels (A,B,C,D) to option text
 * @property correctAnswers List of correct answer labels
 */
data class MultipleChoiceQuestion(
    val question: String,
    val options: Map<String, String>,
    val correctAnswers: List<String>
)

/**
 * Data class representing a true/false question
 * @property statement The statement to evaluate
 * @property isTrue Whether the statement is true or false
 */
data class TrueFalseQuestion(
    val statement: String,
    val isTrue: Boolean
)

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
     * @param transcript The full text transcript of the YouTube video
     * @param title The title of the YouTube video
     * @return A list of [Topic] objects, each containing a title and up to 3 questions.
     *         Returns at most 5 topics to maintain focus on the most important content.
     */
    suspend fun extractTopicsAndQuestions(transcript: String, title: String): List<Topic> {
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
    suspend fun processContent(topics: List<Topic>, transcript: String): List<Topic> {
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
     */
    private suspend fun callLLM(prompt: String): String {
        val messages = listOf(
            Message(role = "user", content = prompt)
        )
        val request = OpenRouterRequest(
            model = "google/gemini-2.0-flash-thinking-exp:free",
            messages = messages
        )
        val authHeader = "Bearer $OPENROUTER_API_KEY"

        val response = ApiService.openRouterApi.createCompletion(authHeader, request)
        return response.choices.firstOrNull()?.message?.content ?: ""
    }

    /**
     * Parses the JSON response from the initial topic extraction into Topic objects using kotlinx.serialization.
     *
     * @param jsonResponse The JSON string response from the LLM
     * @return A list of [Topic] objects, limited to 5 topics with 3 questions each
     */
    private fun parseTopicsFromJson(jsonResponse: String): List<Topic> {
        // Loại bỏ code block (nếu có)
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

            // Tạo map để truy cập nhanh
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
            // Nếu lỗi, trả về danh sách gốc
            originalTopics
        }
    }

    /**
     * Chỉ dùng kotlinx.serialization để parse key points.
     */
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
        // Làm sạch nếu có dấu ```json
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

    /**
     * Chỉ dùng kotlinx.serialization để parse danh sách câu hỏi (MultipleChoice + TrueFalse).
     */
    fun parseQuizQuestions(jsonResponse: String): Pair<List<MultipleChoiceQuestion>, List<TrueFalseQuestion>> {
        // Làm sạch nếu có code block
        val cleanJson = if (jsonResponse.contains("```")) {
            jsonResponse.substringAfter("```json").substringBefore("```").trim()
        } else {
            jsonResponse.trim()
        }

        return try {
            parseWithKotlinxSerialization(cleanJson)
        } catch (e: Exception) {
            println("parseQuizQuestions error: ${e.message}")
            Pair(emptyList(), emptyList())
        }
    }

    /**
     * Hàm parse dùng duy nhất kotlinx.serialization.
     */
    private fun parseWithKotlinxSerialization(jsonStr: String): Pair<List<MultipleChoiceQuestion>, List<TrueFalseQuestion>> {
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
                    val correctAnswersArray = questionObj["correct_answers"]?.jsonArray ?: return@forEach

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
                    val statement = questionObj["statement"]?.jsonPrimitive?.content ?: return@forEach
                    val isTrue = questionObj["is_true"]?.jsonPrimitive?.content?.toBoolean() ?: return@forEach

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
}
