package com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper

import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.SummaryEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Summary

object SummaryMapper {
    fun toEntity(domain: Summary): SummaryEntity {
        return SummaryEntity(
            summaryId = domain.id,
            quizId = domain.quizId,
            content = domain.content
        )
    }

    fun toDomain(entity: SummaryEntity): Summary {
        return Summary(
            id = entity.summaryId,
            quizId = entity.quizId,
            content = entity.content
        )
    }
}