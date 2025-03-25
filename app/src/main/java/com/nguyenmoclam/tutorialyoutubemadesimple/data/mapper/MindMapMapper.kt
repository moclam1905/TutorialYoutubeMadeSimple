package com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper

import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.MindMapEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.MindMap

object MindMapMapper {
    fun toEntity(mindMap: MindMap): MindMapEntity {
        return MindMapEntity(
            id = mindMap.id,
            quizId = mindMap.quizId,
            keyPoints = mindMap.keyPoints,
            mermaidCode = mindMap.mermaidCode,
            lastUpdated = mindMap.lastUpdated
        )
    }

    fun toDomain(entity: MindMapEntity): MindMap {
        return MindMap(
            id = entity.id,
            quizId = entity.quizId,
            keyPoints = entity.keyPoints,
            mermaidCode = entity.mermaidCode,
            lastUpdated = entity.lastUpdated
        )
    }
}