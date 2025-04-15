package com.nguyenmoclam.tutorialyoutubemadesimple.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Represents the associative entity for the many-to-many relationship
 * between QuizEntity and TagEntity.
 */
@Entity(
    tableName = "quiz_tag_cross_ref",
    primaryKeys = ["quizId", "tagId"], // Composite primary key
    foreignKeys = [
        ForeignKey(
            entity = QuizEntity::class,
            parentColumns = ["quizId"],
            childColumns = ["quizId"],
            onDelete = ForeignKey.CASCADE // If a quiz is deleted, remove its tag associations
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["tagId"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE // If a tag is deleted, remove its quiz associations
        )
    ],
    indices = [
        Index(value = ["quizId"]),
        Index(value = ["tagId"])
    ]
)
data class QuizTagCrossRef(
    val quizId: Long,
    val tagId: Long
)
