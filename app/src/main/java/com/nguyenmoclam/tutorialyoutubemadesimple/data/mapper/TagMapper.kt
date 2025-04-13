package com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper

import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.TagEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.tag.Tag

object TagMapper {

    fun toDomain(entity: TagEntity): Tag {
        return Tag(
            id = entity.tagId,
            name = entity.name
        )
    }

    fun toEntity(domain: Tag): TagEntity {
        // When converting to entity, we might not have the ID if it's a new tag.
        // Room will handle auto-generation if tagId is 0.
        return TagEntity(
            tagId = domain.id,
            name = domain.name
        )
    }

    fun listToDomain(entities: List<TagEntity>): List<Tag> {
        return entities.map { toDomain(it) }
    }

    fun listToEntity(domains: List<Tag>): List<TagEntity> {
        return domains.map { toEntity(it) }
    }
}
