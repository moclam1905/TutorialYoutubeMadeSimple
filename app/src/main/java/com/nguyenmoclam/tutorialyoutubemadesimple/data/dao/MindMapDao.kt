package com.nguyenmoclam.tutorialyoutubemadesimple.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.MindMapEntity

@Dao
interface MindMapDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMindMap(mindMap: MindMapEntity)

    @Query("SELECT * FROM mindmaps WHERE quizId = :quizId")
    suspend fun getMindMapForQuiz(quizId: Long): MindMapEntity?
    
    @Query("DELETE FROM mindmaps WHERE quizId = :quizId")
    suspend fun deleteMindMapForQuiz(quizId: Long)
}
