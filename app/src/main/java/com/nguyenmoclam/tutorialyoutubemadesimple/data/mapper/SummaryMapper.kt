package com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper

import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.SummaryEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Summary

object SummaryMapper {
    /**
     * Convert a domain model to an entity
     * @param domain The domain model to convert
     * @return The converted entity with updated lastSyncTimestamp
     */
    fun toEntity(domain: Summary): SummaryEntity {
        return SummaryEntity(
            summaryId = domain.id,
            quizId = domain.quizId,
            content = domain.content,
            lastSyncTimestamp = System.currentTimeMillis() // Update timestamp on conversion
        )
    }

    /**
     * Convert an entity to a domain model
     * @param entity The entity to convert
     * @return The converted domain model
     */
    fun toDomain(entity: SummaryEntity): Summary {
        return Summary(
            id = entity.summaryId,
            quizId = entity.quizId,
            content = entity.content,
            lastSyncTimestamp = entity.lastSyncTimestamp
        )
    }
}