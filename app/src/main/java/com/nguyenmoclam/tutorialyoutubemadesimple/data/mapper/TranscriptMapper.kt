package com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper

import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.TranscriptEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Transcript

/**
 * Mapper class for converting between Transcript domain model and TranscriptEntity.
 */
object TranscriptMapper {
    /**
     * Convert a domain model to an entity
     */
    fun toEntity(transcript: Transcript): TranscriptEntity {
        return TranscriptEntity(
            transcriptId = transcript.id,
            quizId = transcript.quizId,
            content = transcript.content,
            language = transcript.language,
            lastUpdated = transcript.lastUpdated
        )
    }

    /**
     * Convert an entity to a domain model
     */
    fun toDomain(entity: TranscriptEntity): Transcript {
        return Transcript(
            id = entity.transcriptId,
            quizId = entity.quizId,
            content = entity.content,
            language = entity.language,
            lastUpdated = entity.lastUpdated
        )
    }
}