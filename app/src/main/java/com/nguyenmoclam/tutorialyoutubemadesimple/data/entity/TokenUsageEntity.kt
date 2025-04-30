package com.nguyenmoclam.tutorialyoutubemadesimple.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.TokenUsage
import java.util.Date

/**
 * Database entity for storing token usage records.
 * Used to track usage history for reporting and monitoring.
 */
@Entity(tableName = "token_usage")
data class TokenUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val modelId: String,
    val modelName: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val estimatedCost: Double,
    val timestamp: Long
) {
    companion object {
        /**
         * Creates an entity from a TokenUsage model.
         */
        fun fromTokenUsage(tokenUsage: TokenUsage): TokenUsageEntity {
            return TokenUsageEntity(
                modelId = tokenUsage.modelId,
                modelName = tokenUsage.modelName,
                promptTokens = tokenUsage.promptTokens,
                completionTokens = tokenUsage.completionTokens,
                totalTokens = tokenUsage.totalTokens,
                estimatedCost = tokenUsage.estimatedCost,
                timestamp = tokenUsage.timestamp.time
            )
        }
    }

    /**
     * Converts this entity to a TokenUsage model.
     */
    fun toTokenUsage(): TokenUsage {
        return TokenUsage(
            modelId = modelId,
            modelName = modelName,
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            totalTokens = totalTokens,
            estimatedCost = estimatedCost,
            timestamp = Date(timestamp)
        )
    }
} 