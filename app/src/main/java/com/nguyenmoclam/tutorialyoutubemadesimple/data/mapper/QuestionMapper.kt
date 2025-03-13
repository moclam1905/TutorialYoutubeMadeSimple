package com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper

import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.QuestionEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Question

object QuestionMapper {
    fun toEntity(domain: Question): QuestionEntity {
        return QuestionEntity(
            questionId = domain.id,
            quizId = domain.quizId,
            questionText = domain.text,
            options = domain.options,
            correctAnswer = domain.correctAnswer
        )
    }

    fun toDomain(entity: QuestionEntity): Question {
        return Question(
            id = entity.questionId,
            quizId = entity.quizId,
            text = entity.questionText,
            options = entity.options,
            correctAnswer = entity.correctAnswer
        )
    }
}