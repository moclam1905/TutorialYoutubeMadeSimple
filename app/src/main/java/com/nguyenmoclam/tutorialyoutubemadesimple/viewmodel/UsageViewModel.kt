package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.CreditStatus
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.TokenUsage
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.TokenUsageSummary
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.OpenRouterRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * ViewModel for managing token usage data and credit monitoring.
 * Provides UI state and actions for displaying usage statistics.
 */
@HiltViewModel
class UsageViewModel @Inject constructor(
    private val usageRepository: UsageRepository,
    private val openRouterRepository: OpenRouterRepository
) : ViewModel() {
    
    // State for credits information
    private val _creditStatusState = MutableStateFlow<CreditStatusState>(CreditStatusState.Loading)
    val creditStatusState: StateFlow<CreditStatusState> = _creditStatusState.asStateFlow()
    
    // State for token usage summary
    private val _timeRange = MutableStateFlow(TimeRange.ALL_TIME)
    val timeRange: StateFlow<TimeRange> = _timeRange.asStateFlow()
    
    // Token usage summary based on selected time range
    val tokenUsageSummary: StateFlow<TokenUsageSummaryState> = _timeRange
        .flatMapLatest { range ->
            when (range) {
                TimeRange.LAST_7_DAYS -> usageRepository.getTokenUsageSummaryForLastDays(7)
                TimeRange.LAST_30_DAYS -> usageRepository.getTokenUsageSummaryForLastDays(30)
                TimeRange.ALL_TIME -> usageRepository.getTokenUsageSummary()
            }
        }
        .map { summary ->
            if (summary.totalTokens > 0) {
                TokenUsageSummaryState.Success(summary)
            } else {
                TokenUsageSummaryState.Empty
            }
        }
        .catch { e ->
            emit(TokenUsageSummaryState.Error(e.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TokenUsageSummaryState.Loading
        )
    
    // Token usage details based on selected time range
    val tokenUsageDetails: StateFlow<TokenUsageDetailsState> = _timeRange
        .flatMapLatest { range ->
            when (range) {
                TimeRange.LAST_7_DAYS -> usageRepository.getTokenUsageForLastDays(7)
                TimeRange.LAST_30_DAYS -> usageRepository.getTokenUsageForLastDays(30)
                TimeRange.ALL_TIME -> usageRepository.getAllTokenUsage()
            }
        }
        .map { usageList ->
            if (usageList.isNotEmpty()) {
                TokenUsageDetailsState.Success(usageList)
            } else {
                TokenUsageDetailsState.Empty
            }
        }
        .catch { e ->
            emit(TokenUsageDetailsState.Error(e.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TokenUsageDetailsState.Loading
        )
    
    init {
        refreshCreditStatus()
    }
    
    /**
     * Refreshes the credit status from the API.
     */
    fun refreshCreditStatus() {
        viewModelScope.launch {
            _creditStatusState.value = CreditStatusState.Loading
            
            try {
                val creditStatus = usageRepository.getCreditStatus(forceRefresh = true)
                
                if (creditStatus != null) {
                    _creditStatusState.value = CreditStatusState.Success(creditStatus)
                } else {
                    _creditStatusState.value = CreditStatusState.Error("Failed to fetch credit status")
                }
            } catch (e: Exception) {
                _creditStatusState.value = CreditStatusState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Records token usage for a model interaction.
     * 
     * @param usage The token usage to record.
     */
    fun recordTokenUsage(usage: TokenUsage) {
        viewModelScope.launch {
            usageRepository.recordTokenUsage(usage)
        }
    }
    
    /**
     * Records token usage after a completion response.
     * 
     * @param modelId The ID of the model used.
     * @param modelName The human-readable name of the model.
     * @param promptTokens The number of tokens in the prompt.
     * @param completionTokens The number of tokens in the completion.
     * @param promptPrice The price per 1000 prompt tokens.
     * @param completionPrice The price per 1000 completion tokens.
     */
    fun recordCompletion(
        modelId: String,
        modelName: String,
        promptTokens: Int,
        completionTokens: Int,
        promptPrice: Double,
        completionPrice: Double
    ) {
        viewModelScope.launch {
            val tokenUsage = usageRepository.createTokenUsage(
                modelId = modelId,
                modelName = modelName,
                promptTokens = promptTokens,
                completionTokens = completionTokens,
                promptPrice = promptPrice,
                completionPrice = completionPrice
            )
            
            usageRepository.recordTokenUsage(tokenUsage)
            
            // Refresh credit status in background
            refreshCreditStatus()
        }
    }
    
    /**
     * Changes the time range for usage statistics.
     * 
     * @param range The new time range.
     */
    fun setTimeRange(range: TimeRange) {
        _timeRange.value = range
    }
    
    /**
     * Time ranges for filtering usage data.
     */
    enum class TimeRange {
        LAST_7_DAYS,
        LAST_30_DAYS,
        ALL_TIME
    }
    
    /**
     * States for credit status UI.
     */
    sealed class CreditStatusState {
        data object Loading : CreditStatusState()
        data class Success(val creditStatus: CreditStatus) : CreditStatusState()
        data class Error(val message: String) : CreditStatusState()
    }
    
    /**
     * States for token usage summary UI.
     */
    sealed class TokenUsageSummaryState {
        data object Loading : TokenUsageSummaryState()
        data class Success(val summary: TokenUsageSummary) : TokenUsageSummaryState()
        data object Empty : TokenUsageSummaryState()
        data class Error(val message: String) : TokenUsageSummaryState()
    }
    
    /**
     * States for token usage details UI.
     */
    sealed class TokenUsageDetailsState {
        data object Loading : TokenUsageDetailsState()
        data class Success(val usageList: List<TokenUsage>) : TokenUsageDetailsState()
        data object Empty : TokenUsageDetailsState()
        data class Error(val message: String) : TokenUsageDetailsState()
    }
} 