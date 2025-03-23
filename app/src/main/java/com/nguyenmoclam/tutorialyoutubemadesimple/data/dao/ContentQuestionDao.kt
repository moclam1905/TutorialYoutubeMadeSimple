package com.nguyenmoclam.tutorialyoutubemadesimple.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.ContentQuestionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the content_questions table.
 */
@Dao
interface ContentQuestionDao {
    /**
     * Get all questions for a specific topic.
     */
    @Query("SELECT * FROM content_questions WHERE topicId = :topicId")
    fun getQuestionsForTopic(topicId: Long): Flow<List<ContentQuestionEntity>>
    
    /**
     * Get a question by its ID.
     */
    @Query("SELECT * FROM content_questions WHERE questionId = :questionId")
    suspend fun getQuestionById(questionId: Long): ContentQuestionEntity?
    
    /**
     * Get all content questions.
     */
    @Query("SELECT * FROM content_questions")
    fun getAllQuestions(): Flow<List<ContentQuestionEntity>>
    
    /**
     * Insert a question into the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: ContentQuestionEntity): Long
    
    /**
     * Insert multiple questions into the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<ContentQuestionEntity>): List<Long>
    
    /**
     * Update a question in the database.
     */
    @Update
    suspend fun updateQuestion(question: ContentQuestionEntity)
    
    /**
     * Delete a question from the database.
     */
    @Delete
    suspend fun deleteQuestion(question: ContentQuestionEntity)
    
    /**
     * Delete all questions for a specific topic.
     */
    @Query("DELETE FROM content_questions WHERE topicId = :topicId")
    suspend fun deleteQuestionsForTopic(topicId: Long)
    
    /**
     * Get all questions for a specific quiz by joining with topics table.
     */
    @Query("SELECT cq.* FROM content_questions cq INNER JOIN topics t ON cq.topicId = t.topicId WHERE t.quizId = :quizId")
    fun getQuestionsForQuiz(quizId: Long): Flow<List<ContentQuestionEntity>>
}