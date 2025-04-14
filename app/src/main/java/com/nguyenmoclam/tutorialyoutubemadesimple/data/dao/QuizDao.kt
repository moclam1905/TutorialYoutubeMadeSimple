package com.nguyenmoclam.tutorialyoutubemadesimple.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.QuizEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the quizzes table.
 */
@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes ORDER BY lastUpdated DESC")
    fun getAllQuizzes(): Flow<List<QuizEntity>>
    
    @Query("SELECT COUNT(*) FROM quizzes")
    suspend fun getQuizCount(): Int

    @Query("SELECT * FROM quizzes WHERE quizId = :quizId")
    suspend fun getQuizById(quizId: Long): QuizEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity): Long

    @Update
    suspend fun updateQuiz(quiz: QuizEntity)

    @Delete
    suspend fun deleteQuiz(quiz: QuizEntity)

    @Query("DELETE FROM quizzes WHERE quizId = :quizId")
    suspend fun deleteQuizById(quizId: Long)

    @Query("UPDATE quizzes SET localThumbnailPath = :localPath WHERE quizId = :quizId")
    suspend fun updateLocalThumbnailPath(quizId: Long, localPath: String)

    /**
     * Updates the title and description of a specific quiz.
     */
    @Query("UPDATE quizzes SET title = :title, description = :description, lastUpdated = :lastUpdated WHERE quizId = :quizId")
    suspend fun updateQuizTitleDescription(quizId: Long, title: String, description: String, lastUpdated: Long)

    /**
     * Updates the reminder interval for a specific quiz.
     */
    @Query("UPDATE quizzes SET reminderInterval = :reminderInterval, lastUpdated = :lastUpdated WHERE quizId = :quizId")
    suspend fun updateQuizReminderInterval(quizId: Long, reminderInterval: Long?, lastUpdated: Long)

    /**
     * Retrieves all quizzes that are associated with ANY of the provided tag IDs.
     * Uses a JOIN with the QuizTagCrossRef table.
     * Results are ordered by the last updated timestamp in descending order.
     *
     * @param tagIds A set of tag IDs to filter quizzes by.
     * @return A Flow emitting a list of QuizEntity objects matching the criteria.
     */
    @Query("""
        SELECT DISTINCT q.* FROM quizzes q
        INNER JOIN quiz_tag_cross_ref qtr ON q.quizId = qtr.quizId
        WHERE qtr.tagId IN (:tagIds)
        ORDER BY q.lastUpdated DESC
    """)
    fun getQuizzesWithAnyOfTags(tagIds: Set<Long>): Flow<List<QuizEntity>>
}
