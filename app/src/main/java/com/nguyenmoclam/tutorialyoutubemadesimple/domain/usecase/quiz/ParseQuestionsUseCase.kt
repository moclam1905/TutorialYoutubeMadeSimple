package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz

import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Question
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.LLMProcessor
import javax.inject.Inject

/**
 * Use case for parsing questions from JSON to domain model objects.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class ParseQuestionsUseCase @Inject constructor(
    private val llmProcessor: LLMProcessor
) {
    /**
     * Execute the use case to parse questions from JSON.
     *
     * @param questionsJson The JSON string containing the questions
     * @param quizId The ID of the quiz to associate the questions with
     * @return List of Question domain model objects
     */
    operator fun invoke(questionsJson: String, quizId: Long): List<Question> {
        val (multipleChoiceQuestions, trueFalseQuestions) = llmProcessor.parseQuizQuestions(questionsJson)
        
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
}