package com.nguyenmoclam.tutorialyoutubemadesimple.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.TranscriptSegmentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the transcript_segments table.
 */
@Dao
interface TranscriptSegmentDao {
    @Query("SELECT * FROM transcript_segments WHERE transcriptId = :transcriptId ORDER BY orderIndex ASC")
    fun getSegmentsForTranscript(transcriptId: Long): Flow<List<TranscriptSegmentEntity>>

    @Query("SELECT * FROM transcript_segments WHERE transcriptId = :transcriptId AND isChapterStart = 1 ORDER BY orderIndex ASC")
    fun getChaptersForTranscript(transcriptId: Long): Flow<List<TranscriptSegmentEntity>>
    
    @Query("SELECT * FROM transcript_segments WHERE transcriptId = :transcriptId AND timestampMillis <= :currentTimeMillis ORDER BY timestampMillis DESC LIMIT 1")
    suspend fun getCurrentSegment(transcriptId: Long, currentTimeMillis: Long): TranscriptSegmentEntity?
    
    @Query("SELECT * FROM transcript_segments WHERE segmentId = :segmentId")
    suspend fun getSegmentById(segmentId: Long): TranscriptSegmentEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegment(segment: TranscriptSegmentEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegments(segments: List<TranscriptSegmentEntity>): List<Long>
    
    @Update
    suspend fun updateSegment(segment: TranscriptSegmentEntity)
    
    @Delete
    suspend fun deleteSegment(segment: TranscriptSegmentEntity)
    
    @Query("DELETE FROM transcript_segments WHERE transcriptId = :transcriptId")
    suspend fun deleteSegmentsForTranscript(transcriptId: Long)
}