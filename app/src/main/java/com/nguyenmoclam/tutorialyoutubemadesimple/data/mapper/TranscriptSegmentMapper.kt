package com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper

import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.TranscriptSegmentEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.TranscriptSegment

/**
 * Mapper class for converting between TranscriptSegment domain model and TranscriptSegmentEntity.
 */
object TranscriptSegmentMapper {
    /**
     * Convert a TranscriptSegmentEntity to a TranscriptSegment domain model.
     */
    fun fromEntity(entity: TranscriptSegmentEntity): TranscriptSegment {
        return TranscriptSegment(
            id = entity.segmentId,
            transcriptId = entity.transcriptId,
            timestamp = entity.timestamp,
            timestampMillis = entity.timestampMillis,
            text = entity.text,
            isChapterStart = entity.isChapterStart,
            chapterTitle = entity.chapterTitle
        )
    }

    /**
     * Convert a TranscriptSegment domain model to a TranscriptSegmentEntity.
     */
    fun toEntity(model: TranscriptSegment): TranscriptSegmentEntity {
        return TranscriptSegmentEntity(
            segmentId = model.id,
            transcriptId = model.transcriptId,
            timestamp = model.timestamp,
            timestampMillis = model.timestampMillis,
            text = model.text,
            isChapterStart = model.isChapterStart,
            chapterTitle = model.chapterTitle,
            orderIndex = 0 // This will be set by the repository
        )
    }
}