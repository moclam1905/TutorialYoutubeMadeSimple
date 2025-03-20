package com.nguyenmoclam.tutorialyoutubemadesimple.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity class representing a content question extracted from a video in the database.
 * Has a many-to-one relationship with TopicEntity.
 */
@Entity(
    tableName = "content_questions",
    foreignKeys = [
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["topicId"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("topicId")]
)
data class ContentQuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val questionId: Long = 0,
    
    val topicId: Long,
    val original: String,
    val rephrased: String,
    val answer: String
)