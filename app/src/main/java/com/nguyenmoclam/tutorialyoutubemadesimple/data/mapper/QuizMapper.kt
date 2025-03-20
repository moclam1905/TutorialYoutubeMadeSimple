package com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper

import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.QuizEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz

object QuizMapper {
    fun toEntity(domain: Quiz): QuizEntity {
        return QuizEntity(
            quizId = domain.id,
            title = domain.title,
            description = domain.description,
            videoUrl = domain.videoUrl,
            thumbnailUrl = domain.thumbnailUrl,
            language = domain.language,
            questionType = domain.questionType,
            questionCount = domain.questionCount,
            summaryEnabled = domain.summaryEnabled,
            questionsEnabled = domain.questionsEnabled,
            lastUpdated = domain.lastUpdated
        )
    }

    fun toDomain(entity: QuizEntity): Quiz {
        return Quiz(
            id = entity.quizId,
            title = entity.title,
            description = entity.description,
            videoUrl = entity.videoUrl,
            thumbnailUrl = entity.thumbnailUrl,
            language = entity.language,
            questionType = entity.questionType,
            questionCount = entity.questionCount,
            summaryEnabled = entity.summaryEnabled,
            questionsEnabled = entity.questionsEnabled,
            lastUpdated = entity.lastUpdated
        )
    }
}