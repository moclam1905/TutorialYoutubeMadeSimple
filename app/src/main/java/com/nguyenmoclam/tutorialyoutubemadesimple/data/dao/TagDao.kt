package com.nguyenmoclam.tutorialyoutubemadesimple.data.dao

import androidx.room.Dao
import androidx.room.Embedded // Import Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.QuizEntity // Add missing import
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.QuizTagCrossRef
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.TagEntity

import kotlinx.coroutines.flow.Flow

// Data class to hold TagEntity and its associated quiz count
data class TagWithCount(
    @Embedded val tag: TagEntity,
    val quizCount: Int
)

/**
 * Data Access Object for the Tag entity and its relationship with Quiz.
 */
@Dao
interface TagDao {

    /**
     * Inserts a new tag. If the tag name already exists, it ignores the insertion.
     * Returns the row ID of the newly inserted tag, or -1 if ignored.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    /**
     * Inserts an association between a quiz and a tag.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuizTagCrossRef(crossRef: QuizTagCrossRef)

    /**
     * Deletes an association between a quiz and a tag.
     */
    @Query("DELETE FROM quiz_tag_cross_ref WHERE quizId = :quizId AND tagId = :tagId")
    suspend fun deleteQuizTagCrossRef(quizId: Long, tagId: Long)

    /**
     * Gets all tags from the database.
     */
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    /**
     * Gets all tags along with the count of quizzes associated with each tag.
     * Uses a LEFT JOIN to include tags even if they have no associated quizzes.
     */
    @Query("""
        SELECT t.*, COUNT(qtr.quizId) as quizCount
        FROM tags t
        LEFT JOIN quiz_tag_cross_ref qtr ON t.tagId = qtr.tagId
        GROUP BY t.tagId
        ORDER BY t.name ASC
    """)
    fun getTagsWithQuizCount(): Flow<List<TagWithCount>>

    /**
     * Gets a tag by its name (case-insensitive).
     */
    @Query("SELECT * FROM tags WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getTagByName(name: String): TagEntity?

    /**
     * Gets all tags associated with a specific quiz.
     */
    @Transaction // Ensures atomicity when reading related data
    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN quiz_tag_cross_ref qtr ON t.tagId = qtr.tagId
        WHERE qtr.quizId = :quizId
        ORDER BY t.name ASC
    """)
    fun getTagsForQuiz(quizId: Long): Flow<List<TagEntity>>

    /**
     * Deletes all tag associations for a specific quiz.
     * Useful when updating the tags for a quiz.
     */
    @Query("DELETE FROM quiz_tag_cross_ref WHERE quizId = :quizId")
    suspend fun deleteTagsForQuiz(quizId: Long)
    
    /**
     * Gets all quizzes associated with a specific tag.
     */
    @Query("""
        SELECT q.* FROM quizzes q
        INNER JOIN quiz_tag_cross_ref qtr ON q.quizId = qtr.quizId
        WHERE qtr.tagId = :tagId
        ORDER BY q.lastUpdated DESC
    """)
    fun getQuizzesForTag(tagId: Long): Flow<List<QuizEntity>>
    
    /**
     * Gets all quizzes that have any of the specified tags.
     */
    @Query("""
        SELECT DISTINCT q.* FROM quizzes q
        INNER JOIN quiz_tag_cross_ref qtr ON q.quizId = qtr.quizId
        WHERE qtr.tagId IN (:tagIds)
        ORDER BY q.lastUpdated DESC
    """)
    fun getQuizzesWithAnyOfTags(tagIds: Set<Long>): Flow<List<QuizEntity>>

    /**
     * Transaction to update the tags for a specific quiz.
     * It first deletes existing associations and then inserts the new ones.
     */
    @Transaction
    suspend fun updateTagsForQuiz(quizId: Long, tags: List<TagEntity>) {
        // Delete existing associations
        deleteTagsForQuiz(quizId)
        // Loop through the new tags and insert them
        tags.forEach { tag ->
            // Ensure the tag exists or insert it
            var existingTag = getTagByName(tag.name)
            val tagIdToAssociate = if (existingTag != null) {
                existingTag.tagId
            } else {
                // Insert the new tag and get its ID
                insertTag(TagEntity(name = tag.name)) // Let Room auto-generate ID
                getTagByName(tag.name)?.tagId ?: -1L // Re-fetch to get the generated ID
            }

            if (tagIdToAssociate != -1L) {
                insertQuizTagCrossRef(QuizTagCrossRef(quizId = quizId, tagId = tagIdToAssociate))
            }
            // Handle potential error if tag insertion failed or couldn't be retrieved immediately?
            // For simplicity, we assume it works or the cross-ref insert will be skipped.
        }
    }
}
