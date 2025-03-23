package com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper

import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.TopicEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.content.Topic

/**
 * Mapper class for converting between Topic domain model and TopicEntity.
 */
object TopicMapper {
    /**
     * Convert a domain model to an entity
     */
    fun toEntity(domain: Topic, quizId: Long): TopicEntity {
        return TopicEntity(
            quizId = quizId,
            title = domain.title,
            rephrasedTitle = domain.rephrased_title
        )
    }

    /**
     * Convert an entity to a domain model
     * Note: This doesn't include the questions, which need to be loaded separately
     */
    fun toDomain(entity: TopicEntity, questions: List<com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.content.Question> = emptyList()): Topic {
        return Topic(
            title = entity.title,
            rephrased_title = entity.rephrasedTitle,
            questions = questions
        )
    }
}