package com.nguyenmoclam.tutorialyoutubemadesimple.data.repository

import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.TokenUsageDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.TokenUsageEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.CreditStatus
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.Result
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.TokenUsage
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.TokenUsageSummary
import com.nguyenmoclam.tutorialyoutubemadesimple.data.service.OpenRouterService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking

/**
 * Repository for tracking and managing usage data, including token usage
 * and credits monitoring. Provides methods for recording token usage,
 * retrieving usage statistics, and monitoring credit balance.
 */
@Singleton
class UsageRepository @Inject constructor(
    private val tokenUsageDao: TokenUsageDao,
    private val openRouterService: OpenRouterService
) {
    // Credit status cached value
    private var cachedCreditStatus: CreditStatus? = null
    private var lastCreditFetchTime = 0L
    
    // Cache expiration time (15 minutes)
    private val creditCacheExpirationTime = 15 * 60 * 1000L
    
    /**
     * Records token usage for a model interaction.
     * 
     * @param tokenUsage The token usage information to record.
     */
    suspend fun recordTokenUsage(tokenUsage: TokenUsage) = withContext(Dispatchers.IO) {
        val entity = TokenUsageEntity.fromTokenUsage(tokenUsage)
        tokenUsageDao.insertTokenUsage(entity)
    }
    
    /**
     * Gets all token usage records.
     * 
     * @return A Flow of all token usage records.
     */
    fun getAllTokenUsage(): Flow<List<TokenUsage>> {
        return tokenUsageDao.getAllTokenUsage()
            .map { list -> list.map { it.toTokenUsage() } }
            .flowOn(Dispatchers.IO)
    }
    
    /**
     * Gets token usage records for a specific model.
     * 
     * @param modelId The ID of the model to filter by.
     * @return A Flow of token usage records for the specified model.
     */
    fun getTokenUsageForModel(modelId: String): Flow<List<TokenUsage>> {
        return tokenUsageDao.getTokenUsageForModel(modelId)
            .map { list -> list.map { it.toTokenUsage() } }
            .flowOn(Dispatchers.IO)
    }
    
    /**
     * Gets token usage records for the last days.
     * 
     * @param days The number of days to look back.
     * @return A Flow of token usage records for the specified period.
     */
    fun getTokenUsageForLastDays(days: Int): Flow<List<TokenUsage>> {
        val endTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.time = Date(endTime)
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val startTime = calendar.timeInMillis
        
        return tokenUsageDao.getTokenUsageForPeriod(startTime, endTime)
            .map { list -> list.map { it.toTokenUsage() } }
            .flowOn(Dispatchers.IO)
    }
    
    /**
     * Gets a summary of token usage by model.
     * 
     * @return A Flow of TokenUsageSummary.
     */
    fun getTokenUsageSummary(): Flow<TokenUsageSummary> {
        return tokenUsageDao.getTokenUsageSummaryByModel()
            .map { modelEntities ->
                val usageByModel = modelEntities.associate { entity ->
                    entity.modelId to TokenUsageSummary.ModelUsage(
                        modelId = entity.modelId,
                        modelName = entity.modelName.toString(),
                        promptTokens = entity.promptTokens,
                        completionTokens = entity.completionTokens,
                        totalTokens = entity.totalTokens,
                        estimatedCost = entity.estimatedCost
                    )
                }
                
                val totalStats = runBlocking { tokenUsageDao.getTotalUsageStats() } 
                    ?: TokenUsageDao.TokenUsageStats(0, 0, 0, 0.0)
                
                TokenUsageSummary(
                    totalPromptTokens = totalStats.promptTokens,
                    totalCompletionTokens = totalStats.completionTokens,
                    totalTokens = totalStats.totalTokens,
                    totalCost = totalStats.estimatedCost,
                    usageByModel = usageByModel
                )
            }
            .flowOn(Dispatchers.IO)
    }
    
    /**
     * Gets a summary of token usage by model for the last days.
     * 
     * @param days The number of days to look back.
     * @return A Flow of TokenUsageSummary for the specified period.
     */
    fun getTokenUsageSummaryForLastDays(days: Int): Flow<TokenUsageSummary> {
        val endTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.time = Date(endTime)
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val startTime = calendar.timeInMillis
        
        return tokenUsageDao.getTokenUsageSummaryByModelForPeriod(startTime, endTime)
            .map { modelEntities ->
                val usageByModel = modelEntities.associate { entity ->
                    entity.modelId to TokenUsageSummary.ModelUsage(
                        modelId = entity.modelId,
                        modelName = entity.modelName.toString(),
                        promptTokens = entity.promptTokens,
                        completionTokens = entity.completionTokens,
                        totalTokens = entity.totalTokens,
                        estimatedCost = entity.estimatedCost
                    )
                }
                
                val totalStats = runBlocking { tokenUsageDao.getTotalUsageStatsForPeriod(startTime, endTime) } 
                    ?: TokenUsageDao.TokenUsageStats(0, 0, 0, 0.0)
                
                TokenUsageSummary(
                    totalPromptTokens = totalStats.promptTokens,
                    totalCompletionTokens = totalStats.completionTokens,
                    totalTokens = totalStats.totalTokens,
                    totalCost = totalStats.estimatedCost,
                    usageByModel = usageByModel
                )
            }
            .flowOn(Dispatchers.IO)
    }
    
    /**
     * Gets the user's credit status from OpenRouter.
     * Uses cached data if available and not expired.
     * 
     * @param forceRefresh If true, forces a refresh from the API.
     * @return The user's credit status.
     */
    suspend fun getCreditStatus(forceRefresh: Boolean = false): CreditStatus? = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()
        val cacheExpired = (currentTime - lastCreditFetchTime) > creditCacheExpirationTime
        
        if (!forceRefresh && !cacheExpired && cachedCreditStatus != null) {
            return@withContext cachedCreditStatus
        }
        
        val result = openRouterService.getCredits()
        
        if (result is Result.Success) {
            cachedCreditStatus = CreditStatus.fromCreditsResponse(result.value)
            lastCreditFetchTime = currentTime
            cachedCreditStatus
        } else {
            null
        }
    }
    
    /**
     * Calculates the estimated cost for a token usage.
     * 
     * @param modelId The ID of the model used.
     * @param promptTokens The number of prompt tokens.
     * @param completionTokens The number of completion tokens.
     * @param promptPrice The price per 1000 prompt tokens.
     * @param completionPrice The price per 1000 completion tokens.
     * @return The estimated cost.
     */
    fun calculateCost(
        modelId: String,
        promptTokens: Int,
        completionTokens: Int,
        promptPrice: Double,
        completionPrice: Double
    ): Double {
        val promptCost = (promptTokens.toDouble() / 1000.0) * promptPrice
        val completionCost = (completionTokens.toDouble() / 1000.0) * completionPrice
        return promptCost + completionCost
    }
    
    /**
     * Creates a TokenUsage record from response data.
     * 
     * @param modelId The ID of the model used.
     * @param modelName The name of the model used.
     * @param promptTokens The number of prompt tokens.
     * @param completionTokens The number of completion tokens.
     * @param promptPrice The price per 1000 prompt tokens.
     * @param completionPrice The price per 1000 completion tokens.
     * @return A TokenUsage record.
     */
    fun createTokenUsage(
        modelId: String,
        modelName: String,
        promptTokens: Int,
        completionTokens: Int,
        promptPrice: Double,
        completionPrice: Double
    ): TokenUsage {
        val totalTokens = promptTokens + completionTokens
        val estimatedCost = calculateCost(
            modelId = modelId,
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            promptPrice = promptPrice,
            completionPrice = completionPrice
        )
        
        return TokenUsage(
            modelId = modelId,
            modelName = modelName,
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            totalTokens = totalTokens,
            estimatedCost = estimatedCost
        )
    }
} 