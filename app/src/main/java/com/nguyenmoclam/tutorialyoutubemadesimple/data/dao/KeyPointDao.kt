package com.nguyenmoclam.tutorialyoutubemadesimple.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.KeyPointEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the key_points table.
 */
@Dao
interface KeyPointDao {
    /**
     * Get all key points for a specific quiz.
     */
    @Query("SELECT * FROM key_points WHERE quizId = :quizId")
    fun getKeyPointsForQuiz(quizId: Long): Flow<List<KeyPointEntity>>
    
    /**
     * Get a key point by its ID.
     */
    @Query("SELECT * FROM key_points WHERE keyPointId = :keyPointId")
    suspend fun getKeyPointById(keyPointId: Long): KeyPointEntity?
    
    /**
     * Get all key points.
     */
    @Query("SELECT * FROM key_points")
    fun getAllKeyPoints(): Flow<List<KeyPointEntity>>
    
    /**
     * Insert a key point into the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeyPoint(keyPoint: KeyPointEntity): Long
    
    /**
     * Insert multiple key points into the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeyPoints(keyPoints: List<KeyPointEntity>): List<Long>
    
    /**
     * Update a key point in the database.
     */
    @Update
    suspend fun updateKeyPoint(keyPoint: KeyPointEntity)
    
    /**
     * Delete a key point from the database.
     */
    @Delete
    suspend fun deleteKeyPoint(keyPoint: KeyPointEntity)
    
    /**
     * Delete all key points for a specific quiz.
     */
    @Query("DELETE FROM key_points WHERE quizId = :quizId")
    suspend fun deleteKeyPointsForQuiz(quizId: Long)
}