package com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.TokenUsageSummary
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.UsageViewModel
import java.text.NumberFormat
import java.util.*

/**
 * DetailedModelUsageScreen displays comprehensive usage statistics for all AI models.
 * It provides a breakdown of token usage and costs across models.
 */
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedModelUsageScreen(
    navController: NavController,
    usageViewModel: UsageViewModel
) {
    val tokenUsageSummaryState by usageViewModel.tokenUsageSummary.collectAsStateWithLifecycle()
    val selectedTimeRange by usageViewModel.timeRange.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detailed_model_usage)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { usageViewModel.refreshTokenUsage() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.refresh)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header with summary information
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    when (tokenUsageSummaryState) {
                        is UsageViewModel.TokenUsageSummaryState.Success -> {
                            val summary = (tokenUsageSummaryState as UsageViewModel.TokenUsageSummaryState.Success).summary
                            
                            Text(
                                text = stringResource(R.string.usage_summary_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Time range display
                            Text(
                                text = when (selectedTimeRange) {
                                    UsageViewModel.TimeRange.LAST_7_DAYS -> stringResource(R.string.last_7_days_stats)
                                    UsageViewModel.TimeRange.LAST_30_DAYS -> stringResource(R.string.last_30_days_stats)
                                    UsageViewModel.TimeRange.ALL_TIME -> stringResource(R.string.all_time_stats)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Usage summary grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Total tokens
                                SummaryItem(
                                    label = stringResource(R.string.total_tokens),
                                    value = NumberFormat.getNumberInstance().format(summary.totalTokens),
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Total cost
                                SummaryItem(
                                    label = stringResource(R.string.total_cost),
                                    value = "$${String.format("%.4f", summary.totalCost)}",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Prompt tokens
                                SummaryItem(
                                    label = stringResource(R.string.prompt_tokens),
                                    value = NumberFormat.getNumberInstance().format(summary.totalPromptTokens),
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Completion tokens
                                SummaryItem(
                                    label = stringResource(R.string.completion_tokens),
                                    value = NumberFormat.getNumberInstance().format(summary.totalCompletionTokens),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        is UsageViewModel.TokenUsageSummaryState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        is UsageViewModel.TokenUsageSummaryState.Empty -> {
                            EmptyUsageMessage()
                        }
                        is UsageViewModel.TokenUsageSummaryState.Error -> {
                            ErrorUsageMessage(
                                (tokenUsageSummaryState as UsageViewModel.TokenUsageSummaryState.Error).message
                            )
                        }
                    }
                }
            }
            
            // Model usage list
            when (tokenUsageSummaryState) {
                is UsageViewModel.TokenUsageSummaryState.Success -> {
                    val summary = (tokenUsageSummaryState as UsageViewModel.TokenUsageSummaryState.Success).summary
                    
                    if (summary.usageByModel.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.usage_by_model_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        // Sort models by total tokens (descending)
                        val sortedModels = summary.usageByModel.values.sortedByDescending { it.totalTokens }
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(sortedModels) { modelUsage ->
                                ModelUsageCard(modelUsage = modelUsage)
                            }
                        }
                    } else {
                        EmptyUsageMessage()
                    }
                }
                is UsageViewModel.TokenUsageSummaryState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is UsageViewModel.TokenUsageSummaryState.Empty -> {
                    EmptyUsageMessage()
                }
                is UsageViewModel.TokenUsageSummaryState.Error -> {
                    ErrorUsageMessage(
                        (tokenUsageSummaryState as UsageViewModel.TokenUsageSummaryState.Error).message
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun ModelUsageCard(modelUsage: TokenUsageSummary.ModelUsage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = modelUsage.modelName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.total_tokens),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = NumberFormat.getNumberInstance().format(modelUsage.totalTokens),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.cost),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format("%.4f", modelUsage.estimatedCost)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.prompt_tokens),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = NumberFormat.getNumberInstance().format(modelUsage.promptTokens),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.completion_tokens),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = NumberFormat.getNumberInstance().format(modelUsage.completionTokens),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyUsageMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.no_usage_data_available),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.usage_data_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorUsageMessage(errorMessage: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.error_loading_usage),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
} 