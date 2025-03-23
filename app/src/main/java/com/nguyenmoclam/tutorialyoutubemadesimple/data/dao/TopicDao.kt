package com.nguyenmoclam.tutorialyoutubemadesimple.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.TopicEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the topics table.
 */
@Dao
interface TopicDao {
    /**
     * Get all topics for a specific quiz.
     */
    @Query("SELECT * FROM topics WHERE quizId = :quizId")
    fun getTopicsForQuiz(quizId: Long): Flow<List<TopicEntity>>
    
    /**
     * Get a topic by its ID.
     */
    @Query("SELECT * FROM topics WHERE topicId = :topicId")
    suspend fun getTopicById(topicId: Long): TopicEntity?
    
    /**
     * Get all topics.
     */
    @Query("SELECT * FROM topics")
    fun getAllTopics(): Flow<List<TopicEntity>>
    
    /**
     * Insert a topic into the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: TopicEntity): Long
    
    /**
     * Insert multiple topics into the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicEntity>): List<Long>
    
    /**
     * Update a topic in the database.
     */
    @Update
    suspend fun updateTopic(topic: TopicEntity)
    
    /**
     * Delete a topic from the database.
     */
    @Delete
    suspend fun deleteTopic(topic: TopicEntity)
    
    /**
     * Delete all topics for a specific quiz.
     */
    @Query("DELETE FROM topics WHERE quizId = :quizId")
    suspend fun deleteTopicsForQuiz(quizId: Long)
}