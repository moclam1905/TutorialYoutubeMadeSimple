package com.nguyenmoclam.tutorialyoutubemadesimple.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.TokenUsageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for TokenUsageEntity.
 * Provides methods for inserting and querying token usage records.
 */
@Dao
interface TokenUsageDao {
    /**
     * Inserts a token usage record into the database.
     * 
     * @param tokenUsage The token usage record to insert.
     * @return The ID of the inserted record.
     */
    @Insert
    suspend fun insertTokenUsage(tokenUsage: TokenUsageEntity): Long
    
    /**
     * Gets all token usage records.
     * 
     * @return A Flow of all token usage records.
     */
    @Query("SELECT * FROM token_usage ORDER BY timestamp DESC")
    fun getAllTokenUsage(): Flow<List<TokenUsageEntity>>
    
    /**
     * Gets token usage records for a specific model.
     * 
     * @param modelId The ID of the model to filter by.
     * @return A Flow of token usage records for the specified model.
     */
    @Query("SELECT * FROM token_usage WHERE modelId = :modelId ORDER BY timestamp DESC")
    fun getTokenUsageForModel(modelId: String): Flow<List<TokenUsageEntity>>
    
    /**
     * Gets token usage records for a specific time period.
     * 
     * @param startTime The start time of the period (in milliseconds).
     * @param endTime The end time of the period (in milliseconds).
     * @return A Flow of token usage records for the specified time period.
     */
    @Query("SELECT * FROM token_usage WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getTokenUsageForPeriod(startTime: Long, endTime: Long): Flow<List<TokenUsageEntity>>
    
    /**
     * Gets the total usage statistics.
     * 
     * @return A list of aggregated usage statistics by model.
     */
    @Query("SELECT modelId, modelName, SUM(promptTokens) as promptTokens, " +
           "SUM(completionTokens) as completionTokens, SUM(totalTokens) as totalTokens, " +
           "SUM(estimatedCost) as estimatedCost, MAX(timestamp) as timestamp " +
           "FROM token_usage GROUP BY modelId")
    fun getTokenUsageSummaryByModel(): Flow<List<TokenUsageSummary>>
    
    /**
     * Gets the total usage statistics for a specific time period.
     * 
     * @param startTime The start time of the period (in milliseconds).
     * @param endTime The end time of the period (in milliseconds).
     * @return A list of aggregated usage statistics by model for the specified time period.
     */
    @Query("SELECT modelId, modelName, SUM(promptTokens) as promptTokens, " +
           "SUM(completionTokens) as completionTokens, SUM(totalTokens) as totalTokens, " +
           "SUM(estimatedCost) as estimatedCost, MAX(timestamp) as timestamp " +
           "FROM token_usage WHERE timestamp BETWEEN :startTime AND :endTime " +
           "GROUP BY modelId")
    fun getTokenUsageSummaryByModelForPeriod(startTime: Long, endTime: Long): Flow<List<TokenUsageSummary>>
    
    /**
     * Gets the total usage values.
     * 
     * @return The total prompt tokens, completion tokens, total tokens, and cost.
     */
    @Query("SELECT SUM(promptTokens) as promptTokens, " +
           "SUM(completionTokens) as completionTokens, " +
           "SUM(totalTokens) as totalTokens, " +
           "SUM(estimatedCost) as estimatedCost " +
           "FROM token_usage")
    suspend fun getTotalUsageStats(): TokenUsageStats?
    
    /**
     * Gets the total usage values for a specific time period.
     * 
     * @param startTime The start time of the period (in milliseconds).
     * @param endTime The end time of the period (in milliseconds).
     * @return The total prompt tokens, completion tokens, total tokens, and cost for the period.
     */
    @Query("SELECT SUM(promptTokens) as promptTokens, " +
           "SUM(completionTokens) as completionTokens, " +
           "SUM(totalTokens) as totalTokens, " +
           "SUM(estimatedCost) as estimatedCost " +
           "FROM token_usage WHERE timestamp BETWEEN :startTime AND :endTime")
    suspend fun getTotalUsageStatsForPeriod(startTime: Long, endTime: Long): TokenUsageStats?
    
    /**
     * Data class for holding aggregated usage statistics returned by summary queries.
     */
    data class TokenUsageSummary(
        val modelId: String,
        val modelName: String?,
        val promptTokens: Int,
        val completionTokens: Int,
        val totalTokens: Int,
        val estimatedCost: Double,
        val timestamp: Long
    )
    
    /**
     * Data class for holding aggregated usage statistics.
     */
    data class TokenUsageStats(
        val promptTokens: Int,
        val completionTokens: Int,
        val totalTokens: Int,
        val estimatedCost: Double
    )
} 