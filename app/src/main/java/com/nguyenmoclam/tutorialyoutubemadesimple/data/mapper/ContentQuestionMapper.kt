package com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper

import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.ContentQuestionEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.content.Question

/**
 * Mapper class for converting between Question domain model and ContentQuestionEntity.
 */
object ContentQuestionMapper {
    /**
     * Convert a domain model to an entity
     */
    fun toEntity(domain: Question, topicId: Long): ContentQuestionEntity {
        return ContentQuestionEntity(
            topicId = topicId,
            original = domain.original,
            rephrased = domain.rephrased,
            answer = domain.answer
        )
    }

    /**
     * Convert an entity to a domain model
     */
    fun toDomain(entity: ContentQuestionEntity): Question {
        return Question(
            original = entity.original,
            rephrased = entity.rephrased,
            answer = entity.answer
        )
    }
}