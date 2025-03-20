package com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper

import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.KeyPointEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.keypoint.KeyPoint

/**
 * Mapper class for converting between KeyPoint domain model and KeyPointEntity data model.
 */
object KeyPointMapper {
    /**
     * Convert a KeyPointEntity to a KeyPoint domain model.
     *
     * @param entity The KeyPointEntity to convert
     * @return The corresponding KeyPoint domain model
     */
    fun toDomain(entity: KeyPointEntity): KeyPoint {
        return KeyPoint(
            id = entity.keyPointId,
            quizId = entity.quizId,
            content = entity.content
        )
    }

    /**
     * Convert a KeyPoint domain model to a KeyPointEntity.
     *
     * @param domain The KeyPoint domain model to convert
     * @param quizId The ID of the quiz this key point belongs to
     * @return The corresponding KeyPointEntity
     */
    fun toEntity(content: String, quizId: Long): KeyPointEntity {
        return KeyPointEntity(
            quizId = quizId,
            content = content
        )
    }
}