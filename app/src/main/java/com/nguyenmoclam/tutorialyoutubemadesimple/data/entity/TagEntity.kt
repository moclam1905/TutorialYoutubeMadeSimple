package com.nguyenmoclam.tutorialyoutubemadesimple.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity class representing a tag in the database.
 * Tags can be associated with multiple quizzes.
 */
@Entity(
    tableName = "tags",
    indices = [Index(value = ["name"], unique = true)] // Ensure tag names are unique
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val tagId: Long = 0,

    val name: String // The name of the tag
)
