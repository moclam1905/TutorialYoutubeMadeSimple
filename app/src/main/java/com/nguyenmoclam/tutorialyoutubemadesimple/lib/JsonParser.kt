package com.nguyenmoclam.tutorialyoutubemadesimple.lib

import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.content.Question
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.content.Topic
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.MultipleChoiceQuestion
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.TrueFalseQuestion
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.LLMProcessor.LLMError
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Parses the JSON response from the key point extraction prompt into a list of key point strings.
 * @param jsonResponse The raw JSON string response from the LLM.
 * @return List of key point texts (max 5) or an empty list if parsing fails or no key points found.
 */
fun parseKeyPointsFromJsonToListString(
    jsonResponse: String,
    errorCallback: ((LLMError) -> Unit?)? = null
): List<String> {
    // Remove any markdown formatting (e.g., code fences) if present
    val cleanJson = if (jsonResponse.contains("```")) {
        jsonResponse.substringAfter("```json").substringBefore("```").trim()
    } else {
        jsonResponse.trim()
    }
    // --- LOGGING START ---
    println("LLMProcessor: Attempting to parse key points list from JSON: $cleanJson")
    // --- LOGGING END ---

    return try {
        val json = Json.parseToJsonElement(cleanJson).jsonObject
        // Check for "key_points" or "key_point" key
        val pointsArray =
            json["key_points"]?.jsonArray ?: json["key_point"]?.jsonArray ?: return emptyList()

        // Limit to at most 5 key points (consider if this limit is still desired or should be adjusted based on the prompt)
        val limitedPointsArray =
            if (pointsArray.size > 10) pointsArray.take(10) else pointsArray // Adjusted limit based on extractKeyPointsForMindMap prompt

        limitedPointsArray.mapNotNull { pointElement ->
            pointElement.jsonPrimitive.content.trim().takeIf { it.isNotEmpty() }
        }
    } catch (e: Exception) {
        println("LLMProcessor: parseKeyPointsFromJsonToListString error: ${e.message}. Failed JSON: $cleanJson") // <-- Add logging
        errorCallback?.invoke(LLMError.PermanentError("Failed to parse key points list JSON: ${e.message}"))
        emptyList()
    }
}

fun parseQuestionsFromJson(jsonStr: String): Pair<List<MultipleChoiceQuestion>, List<TrueFalseQuestion>> {
    // --- LOGGING START ---
    println("LLMProcessor: Attempting to parse quiz questions from JSON: $jsonStr")
    // --- LOGGING END ---
    val json = Json.parseToJsonElement(jsonStr).jsonObject
    // Check for "questions" or "question" key
    val questionsArray = json["questions"]?.jsonArray ?: json["question"]?.jsonArray ?: return Pair(
        emptyList(),
        emptyList()
    )

    val multipleChoiceQuestions = mutableListOf<MultipleChoiceQuestion>()
    val trueFalseQuestions = mutableListOf<TrueFalseQuestion>()

    questionsArray.forEach { questionElement ->
        val questionObj = questionElement.jsonObject

        when {
            // Multiple-choice
            questionObj.containsKey("options") -> {
                val question = questionObj["question"]?.jsonPrimitive?.content ?: return@forEach
                // Check for "options" or "option" key
                val optionsObj =
                    questionObj["options"]?.jsonObject ?: questionObj["option"]?.jsonObject
                    ?: return@forEach
                // Check for "correct_answers" (plural array), "correct_answer" (singular array), or "correct_answer" (single string)
                val correctAnswersArray = questionObj["correct_answers"]?.jsonArray
                    ?: questionObj["correct_answer"]?.jsonArray
                    ?: questionObj["correct_answer"]?.jsonPrimitive?.content?.let {
                        Json.parseToJsonElement(
                            "[\"$it\"]"
                        ).jsonArray
                    } // Handle single string answer
                    ?: return@forEach

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

fun parseKeyPointsFromJson(
    jsonStr: String,
    errorCallback: ((LLMError) -> Unit?)? = null
): List<String> {
    // --- LOGGING START ---
    println("LLMProcessor: Attempting to parse key points from JSON: $jsonStr")
    // --- LOGGING END ---
    return try {
        val json = Json.parseToJsonElement(jsonStr).jsonObject
        // Check for "key_points" or "key_point" key
        val keyPointsArray = json["key_points"]?.jsonArray ?: json["key_point"]?.jsonArray
        keyPointsArray?.map { it.jsonPrimitive.content } ?: emptyList()
    } catch (e: Exception) {
        println("LLMProcessor: parseKeyPointsFromJson error: ${e.message}. Failed JSON: $jsonStr") // <-- Add logging
        errorCallback?.invoke(LLMError.PermanentError("Failed to parse key points JSON: ${e.message}"))
        emptyList()
    }
}

/**
 * Parses the JSON response from the initial topic extraction into Topic objects using kotlinx.serialization.
 *
 * @param jsonResponse The JSON string response from the LLM
 * @return A list of [Topic] objects, limited to 5 topics with 3 questions each
 */
fun parseTopicsFromJson(
    jsonResponse: String,
    errorCallback: ((LLMError) -> Unit?)? = null
): List<Topic> {
    println("LLMProcessor: JSON: $jsonResponse") // <-- Add logging here
    val cleanJson = if (jsonResponse.contains("```")) {
        jsonResponse.substringAfter("```json").substringBefore("```").trim()
    } else {
        jsonResponse.trim()
    }

    // Log the cleaned JSON before parsing
    println("LLMProcessor: Attempting to parse topics from JSON: $cleanJson") // <-- Add logging here


    return try {
        val json = Json.parseToJsonElement(cleanJson).jsonObject
        val topicsArray =
            json["topics"]?.jsonArray ?: json["topic"]?.jsonArray ?: return emptyList()

        if (topicsArray.isEmpty()) {
            println("LLMProcessor: 'topics' or 'topic' array is empty.") // <-- Add logging here
        }

        val limitedTopicsArray = if (topicsArray.size > 5) topicsArray.take(5) else topicsArray

        limitedTopicsArray.mapNotNull { topicElement ->
            val topicObj = topicElement.jsonObject
            val title = topicObj["title"]?.jsonPrimitive?.content ?: run {
                println("LLMProcessor: Topic missing 'title'. Skipping.") // <-- Add logging here
                return@mapNotNull null
            }
            val questionsArray =
                topicObj["questions"]?.jsonArray ?: topicObj["question"]?.jsonArray ?: run {
                    println("LLMProcessor: Topic '$title' missing 'questions' or 'question' key. Skipping.") // Updated log message
                    return@mapNotNull null
                }

            val limitedQuestionsArray =
                if (questionsArray.size > 3) questionsArray.take(3) else questionsArray

            val questions = limitedQuestionsArray.map { questionElement ->
                questionElement.jsonPrimitive.content.let { Question(it) }
            }
            Topic(title = title, questions = questions)
        }.also {
            if (it.isEmpty() && !topicsArray.isEmpty()) {
                println("LLMProcessor: Parsing resulted in empty list despite non-empty JSON array.") // <-- Add logging here
            }
        }
    } catch (e: Exception) {
        println("LLMProcessor: parseTopicsFromJson error: ${e.message}. Failed JSON: $cleanJson") // <-- Add logging here
        errorCallback?.invoke(LLMError.PermanentError("Failed to parse topics JSON: ${e.message}")) // Optional: report error
        emptyList()
    }
}

fun parseMermaidMindMapCleanCode(responseContent: String): String {
    return if (responseContent.contains("```")) {
        if (responseContent.contains("```mermaid")) {
            responseContent.substringAfter("```mermaid").substringBefore("```").trim()
        } else {
            responseContent.substringAfter("```").substringBefore("```").trim()
        }
    } else {
        responseContent.trim()
    }
}

fun parseBatchProcessedContent(
    originalTopics: List<Topic>,
    jsonResponse: String,
    errorCallback: ((LLMError) -> Unit?)? = null
): List<Topic> {
    val cleanJson = if (jsonResponse.contains("```")) {
        jsonResponse.substringAfter("```json").substringBefore("```").trim()
    } else {
        jsonResponse.trim()
    }

    return try {
        val json = Json.parseToJsonElement(cleanJson).jsonObject
        val topicsArray =
            json["topics"]?.jsonArray ?: json["topic"]?.jsonArray ?: return originalTopics

        val processedTopicsMap = topicsArray.associate { topicElement ->
            val topicObj = topicElement.jsonObject
            val originalTitle = topicObj["original_title"]?.jsonPrimitive?.content ?: ""
            val rephrasedTitle =
                topicObj["rephrased_title"]?.jsonPrimitive?.content ?: originalTitle
            val questionsArray =
                topicObj["questions"]?.jsonArray ?: topicObj["question"]?.jsonArray

            originalTitle to Pair(rephrasedTitle, questionsArray)
        }

        originalTopics.map { originalTopic ->
            val (rephrasedTitle, questionsArray) = processedTopicsMap[originalTopic.title]
                ?: return@map originalTopic

            val processedQuestions = originalTopic.questions.map { originalQuestion ->
                val processedQuestionObject = questionsArray
                    ?.find {
                        it.jsonObject["original"]?.jsonPrimitive?.content == originalQuestion.original
                    }
                    ?.jsonObject

                Question(
                    original = originalQuestion.original,
                    rephrased = processedQuestionObject?.get("rephrased")?.jsonPrimitive?.content
                        ?: originalQuestion.original,
                    answer = processedQuestionObject?.get("answer")?.jsonPrimitive?.content
                        ?: ""
                )
            }

            originalTopic.copy(
                rephrased_title = rephrasedTitle,
                questions = processedQuestions
            )
        }
    } catch (e: Exception) {
        errorCallback?.invoke(LLMError.PermanentError("Failed to parse batch processed content JSON: ${e.message}"))
        originalTopics // Return original topics on failure
    }
}