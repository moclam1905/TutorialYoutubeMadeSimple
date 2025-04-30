package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.CreditStatus
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.UsageViewModel
import java.util.Date

/**
 * Collapsible help section with OpenRouter guidance.
 * (Moved from SettingsComponents.kt)
 */
@Composable
fun OpenRouterHelpSection() {
    var expanded by remember { mutableStateOf(false) }
    var showTroubleshooting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val noBrowserAvailableDes = stringResource(R.string.no_browser_available)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Main Header - Clickable to expand/collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(R.string.openrouter_help_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            // Expanded content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column { // Wrap expanded content in a Column for AnimatedVisibility
                    Spacer(modifier = Modifier.height(16.dp))

                    // Connection Status Indicator
                    ConnectionStatusIndicator() // Assuming moved here

                    Spacer(modifier = Modifier.height(16.dp))

                    // Step-by-step instructions
                    Text(
                        text = stringResource(R.string.openrouter_instructions_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Step 1
                    Row(verticalAlignment = Alignment.Top) {
                        Card(
                            modifier = Modifier.size(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = CircleShape
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    "1",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = stringResource(R.string.openrouter_visit),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(R.string.openrouter_create_account),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Step 2
                    Row(verticalAlignment = Alignment.Top) {
                        Card(
                            modifier = Modifier.size(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = CircleShape
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    "2",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = stringResource(R.string.openrouter_generate_key),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(R.string.openrouter_key_instructions),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Step 3
                    Row(verticalAlignment = Alignment.Top) {
                        Card(
                            modifier = Modifier.size(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = CircleShape
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    "3",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = stringResource(R.string.openrouter_copy_key),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(R.string.openrouter_paste_instructions),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Documentation & Help Links - Changed to Column for better text wrapping
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally // Center buttons in the column
                    ) {
                        // Documentation Button
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    "https://openrouter.ai/docs".toUri()
                                )
                                try {
                                    context.startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    // Handle case where no browser is available
                                    Toast.makeText(
                                        context,
                                        noBrowserAvailableDes,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(0.8f) // Occupy 80% of width, adjust as needed
                        ) {
                            Icon(
                                Icons.Default.OpenInNew,
                                contentDescription = "Open documentation"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.open_openrouter_docs))
                        }

                        Spacer(modifier = Modifier.height(8.dp)) // Vertical spacer

                        // Troubleshooting Button
                        OutlinedButton(
                            onClick = { showTroubleshooting = !showTroubleshooting },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (showTroubleshooting)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else
                                    MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth(0.8f) // Occupy 80% of width, adjust as needed
                        ) {
                            Icon(
                                if (showTroubleshooting) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Troubleshooting"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.troubleshooting_button))
                        }
                    }

                    // Troubleshooting Guide - only visible when troubleshooting is expanded
                    AnimatedVisibility(
                        visible = showTroubleshooting,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        TroubleshootingGuide() // Assuming moved here
                    }
                }
            }
        }
    }
}


/**
 * Component that displays the current connection status to OpenRouter.
 * (Moved from SettingsComponents.kt)
 */
@Composable
private fun ConnectionStatusIndicator() {
    // We would want to connect this to a real API status check
    // For now, determining status based on ApiKeyValidationState
    val context = LocalContext.current
    val isConnected = remember {
        // This is a simplified check - in a real app, you would want
        // to validate actual connection status with the API
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.activeNetwork != null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                contentDescription = "Connection Status",
                tint = if (isConnected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        if (isConnected)
                            R.string.openrouter_connection_available
                        else
                            R.string.openrouter_connection_unavailable
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = stringResource(
                        if (isConnected)
                            R.string.openrouter_services_available
                        else
                            R.string.openrouter_services_unavailable
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


/**
 * Expandable troubleshooting guide for OpenRouter integration.
 * (Moved from SettingsComponents.kt)
 */
@Composable
private fun TroubleshootingGuide() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.troubleshooting_guide),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Trouble 1: API Key Not Working
        TroubleshootingItem(
            title = stringResource(R.string.openrouter_trouble_1_title),
            description = stringResource(R.string.openrouter_trouble_1_desc)
        )

        // Trouble 2: No Models Showing
        TroubleshootingItem(
            title = stringResource(R.string.openrouter_trouble_2_title),
            description = stringResource(R.string.openrouter_trouble_2_desc)
        )

        // Trouble 3: Connection Issues
        TroubleshootingItem(
            title = stringResource(R.string.openrouter_trouble_3_title),
            description = stringResource(R.string.openrouter_trouble_3_desc)
        )

        // Trouble 4: Billing Issues
        TroubleshootingItem(
            title = stringResource(R.string.openrouter_trouble_4_title),
            description = stringResource(R.string.openrouter_trouble_4_desc)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.openrouter_discord_help),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

/**
 * Individual troubleshooting item with title and description.
 * (Moved from SettingsComponents.kt)
 */
@Composable
private fun TroubleshootingItem(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 24.dp) // Indent description
        )
    }
}

/**
 * Component for displaying credit balance and token usage monitoring.
 * Shows current credits, usage status, and detailed statistics.
 * (Moved from SettingsComponents.kt)
 */
@SuppressLint("DefaultLocale")
@Composable
fun CreditAndUsageMonitoring(
    creditStatusState: UsageViewModel.CreditStatusState,
    tokenUsageSummaryState: UsageViewModel.TokenUsageSummaryState,
    selectedTimeRange: UsageViewModel.TimeRange,
    onTimeRangeSelected: (UsageViewModel.TimeRange) -> Unit,
    onRefreshCredits: () -> Unit,
    onViewAllModelsClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header for this sub-section
        Text(
            text = stringResource(R.string.credits_and_usage),
            style = MaterialTheme.typography.titleMedium, // Title for this sub-section
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Credit Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (creditStatusState) {
                    is UsageViewModel.CreditStatusState.Success -> {
                        when (creditStatusState.creditStatus.status) {
                            CreditStatus.BalanceStatus.CRITICAL -> MaterialTheme.colorScheme.errorContainer
                            CreditStatus.BalanceStatus.LOW -> MaterialTheme.colorScheme.secondaryContainer
                            CreditStatus.BalanceStatus.OK -> MaterialTheme.colorScheme.tertiaryContainer
                        }
                    }

                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.available_credits),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Refresh button
                        IconButton(
                            onClick = onRefreshCredits,
                            modifier = Modifier.size(36.dp)
                        ) {
                            when (creditStatusState) {
                                is UsageViewModel.CreditStatusState.Loading -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                }

                                else -> {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = stringResource(R.string.refresh_credits),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    when (creditStatusState) {
                        is UsageViewModel.CreditStatusState.Loading -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.loading_credits),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        is UsageViewModel.CreditStatusState.Success -> {
                            val creditStatus = creditStatusState.creditStatus

                            // Credit amount
                            Text(
                                text = "$${String.format("%.2f", creditStatus.credits)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = when (creditStatus.status) {
                                    CreditStatus.BalanceStatus.CRITICAL -> MaterialTheme.colorScheme.error
                                    CreditStatus.BalanceStatus.LOW -> MaterialTheme.colorScheme.secondary
                                    CreditStatus.BalanceStatus.OK -> MaterialTheme.colorScheme.tertiary
                                }
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Status message
                            val statusMessage = when (creditStatus.status) {
                                CreditStatus.BalanceStatus.CRITICAL -> stringResource(
                                    R.string.critical_balance_warning,
                                    String.format("%.2f", creditStatus.credits)
                                )

                                CreditStatus.BalanceStatus.LOW -> stringResource(
                                    R.string.low_balance_warning,
                                    String.format("%.2f", creditStatus.credits)
                                )

                                CreditStatus.BalanceStatus.OK -> stringResource(
                                    R.string.credit_balance_status,
                                    String.format("%.2f", creditStatus.credits)
                                )
                            }

                            Text(
                                text = statusMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Credit details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.credit_granted),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$${
                                            String.format(
                                                "%.2f",
                                                creditStatus.creditGranted
                                            )
                                        }",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = stringResource(R.string.credit_used),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$${String.format("%.2f", creditStatus.creditUsed)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Last updated
                            Text(
                                text = stringResource(
                                    R.string.last_updated,
                                    formatLastUpdated(creditStatus.lastUpdated) // Use moved function
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                        }

                        is UsageViewModel.CreditStatusState.Error -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.credit_fetch_error),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = creditStatusState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Token Usage Section
        Text(
            text = stringResource(R.string.token_usage),
            style = MaterialTheme.typography.titleMedium, // Title for this sub-section
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Time range selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Time range chips
            listOf(
                UsageViewModel.TimeRange.LAST_7_DAYS to stringResource(R.string.last_7_days),
                UsageViewModel.TimeRange.LAST_30_DAYS to stringResource(R.string.last_30_days),
                UsageViewModel.TimeRange.ALL_TIME to stringResource(R.string.all_time)
            ).forEach { (range, label) ->
                val isSelected = selectedTimeRange == range

                FilterChip(
                    selected = isSelected,
                    onClick = { onTimeRangeSelected(range) },
                    label = { Text(label) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Token usage summary
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                when (tokenUsageSummaryState) {
                    is UsageViewModel.TokenUsageSummaryState.Loading -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.loading_usage_data),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    is UsageViewModel.TokenUsageSummaryState.Success -> {
                        val summary = tokenUsageSummaryState.summary

                        // Total tokens used
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.total_tokens),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = summary.totalTokens.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Token breakdown
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
                                    text = summary.totalPromptTokens.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = stringResource(R.string.completion_tokens),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = summary.totalCompletionTokens.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Total estimated cost
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.estimated_cost),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$${String.format("%.4f", summary.totalCost)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        // Display usage by model if there are models
                        if (summary.usageByModel.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = stringResource(R.string.usage_by_model),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Top 3 most used models
                            val topModels = summary.usageByModel.values
                                .sortedByDescending { it.totalTokens }
                                .take(3)

                            topModels.forEach { modelUsage ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = modelUsage.modelName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${modelUsage.totalTokens} tokens",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = "$${
                                            String.format(
                                                "%.4f",
                                                modelUsage.estimatedCost
                                            )
                                        }",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            // Show "View all models" button if there are more than 3 models
                            if (summary.usageByModel.size > 3) {
                                TextButton(
                                    onClick = onViewAllModelsClick,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    Text(stringResource(R.string.view_all_models))
                                }
                            }
                        }
                    }

                    is UsageViewModel.TokenUsageSummaryState.Empty -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.no_usage_data),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    is UsageViewModel.TokenUsageSummaryState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.usage_data_error),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tokenUsageSummaryState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Formats a Date as a relative time string (e.g., "5 minutes ago").
 * (Moved from SettingsComponents.kt)
 */
@Composable
@SuppressLint("DefaultLocale")
private fun formatLastUpdated(date: Date): String {
    val now = System.currentTimeMillis()
    val time = date.time
    val diff = now - time

    return when {
        diff < 60_000 -> stringResource(R.string.time_just_now) // "just now"
        diff < 3_600_000 -> stringResource(
            R.string.time_minutes_ago,
            diff / 60_000
        ) // "${diff / 60_000} minutes ago"
        diff < 86_400_000 -> stringResource(
            R.string.time_hours_ago,
            diff / 3_600_000
        ) // "${diff / 3_600_000} hours ago"
        diff < 604_800_000 -> stringResource(
            R.string.time_days_ago,
            diff / 86_400_000
        ) // "${diff / 86_400_000} days ago"
        else -> stringResource(
            R.string.time_weeks_ago,
            diff / 604_800_000
        ) // "${diff / 604_800_000} weeks ago"
    }
}