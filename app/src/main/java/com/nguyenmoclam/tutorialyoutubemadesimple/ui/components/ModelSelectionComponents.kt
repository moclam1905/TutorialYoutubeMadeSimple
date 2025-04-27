package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.ModelFilter
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.ModelInfo

/**
 * Displays a horizontally scrollable row of filter chips.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipRow(
    title: String,
    options: List<String>,
    selectedOptions: Set<String>,
    onOptionSelected: (String) -> Unit,
    onOptionDeselected: () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(options) { option ->
                val isSelected = selectedOptions.contains(option)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) {
                            onOptionDeselected()
                        } else {
                            onOptionSelected(option)
                        }
                    },
                    label = { Text(option) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null
                )
            }
        }
    }
}

/**
 * Card displaying model information.
 */
@Composable
fun ModelCard(
    model: ModelInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Provider Icon/Logo placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = model.providerName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Model information
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Provider name
                Text(
                    text = model.providerName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Feature badges row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Context length badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Text(
                            text = "${model.contextLength / 1000}K",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    
                    // Free badge if applicable
                    if (model.isFree) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ) {
                            Text(
                                text = "FREE",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    // Input modality badges
                    model.inputModalities.take(2).forEach { modality ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            Text(
                                text = modality.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            
            // Pricing information
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (model.isFree) "FREE" else "$${String.format("%.4f", model.promptPrice)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (model.isFree) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = stringResource(R.string.per_million_tokens),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Displays a list of models with search, filter and sort options.
 */
@Composable
fun ModelSelectionComponent(
    models: List<ModelInfo> = emptyList(),
    selectedModelId: String = "",
    onModelSelected: (String) -> Unit = {},
    currentFilters: Map<ModelFilter.Category, Set<String>> = emptyMap(),
    currentSortOption: ModelFilter.SortOption = ModelFilter.SortOption.TOP_WEEKLY,
    onApplyFilter: (ModelFilter.Category, String) -> Unit = { _, _ -> },
    onClearFilter: (ModelFilter.Category) -> Unit = { _ -> },
    onSetSortOption: (ModelFilter.SortOption) -> Unit = { _ -> },
    isLoading: Boolean = false,
    onRefresh: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    hasMoreModels: Boolean = false
) {
    // State for showing advanced filters
    var showAdvancedFilters by remember { mutableStateOf(false) }
    // State for search query
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_models)) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = stringResource(R.string.clear_search),
                        modifier = Modifier.clickable { searchQuery = "" }
                    )
                }
            },
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sorting options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sorting Dropdown
            var sortExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(
                    onClick = { sortExpanded = true },
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        when (currentSortOption) {
                            ModelFilter.SortOption.TOP_WEEKLY -> stringResource(R.string.sort_top_weekly)
                            ModelFilter.SortOption.NEWEST -> stringResource(R.string.sort_newest)
                            ModelFilter.SortOption.PRICE_LOW_TO_HIGH -> stringResource(R.string.sort_price_low_high)
                            ModelFilter.SortOption.PRICE_HIGH_TO_LOW -> stringResource(R.string.sort_price_high_low)
                            ModelFilter.SortOption.CONTEXT_HIGH_TO_LOW -> stringResource(R.string.sort_context_high_low)
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                
                DropdownMenu(
                    expanded = sortExpanded,
                    onDismissRequest = { sortExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sort_top_weekly)) },
                        onClick = {
                            onSetSortOption(ModelFilter.SortOption.TOP_WEEKLY)
                            sortExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sort_newest)) },
                        onClick = {
                            onSetSortOption(ModelFilter.SortOption.NEWEST)
                            sortExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sort_price_low_high)) },
                        onClick = {
                            onSetSortOption(ModelFilter.SortOption.PRICE_LOW_TO_HIGH)
                            sortExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sort_price_high_low)) },
                        onClick = {
                            onSetSortOption(ModelFilter.SortOption.PRICE_HIGH_TO_LOW)
                            sortExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sort_context_high_low)) },
                        onClick = {
                            onSetSortOption(ModelFilter.SortOption.CONTEXT_HIGH_TO_LOW)
                            sortExpanded = false
                        }
                    )
                }
            }
            
            // Advanced filters button
            OutlinedButton(
                onClick = { showAdvancedFilters = !showAdvancedFilters },
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    if (showAdvancedFilters) stringResource(R.string.hide_filters) else stringResource(R.string.show_filters),
                    style = MaterialTheme.typography.labelMedium
                )
                Icon(
                    if (showAdvancedFilters) Icons.Default.ExpandLess else Icons.Default.ExpandMore, 
                    contentDescription = null
                )
            }
        }
        
        // Filter chips section (horizontally scrollable)
        if (showAdvancedFilters) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Input Modality filters
            FilterChipRow(
                title = stringResource(R.string.input_modalities),
                options = ModelFilter.InputModalities.ALL,
                selectedOptions = currentFilters[ModelFilter.Category.INPUT_MODALITY] ?: emptySet(),
                onOptionSelected = { onApplyFilter(ModelFilter.Category.INPUT_MODALITY, it) },
                onOptionDeselected = { onClearFilter(ModelFilter.Category.INPUT_MODALITY) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Context length filters
            FilterChipRow(
                title = stringResource(R.string.context_length),
                options = ModelFilter.ContextLengths.ALL,
                selectedOptions = currentFilters[ModelFilter.Category.CONTEXT_LENGTH] ?: emptySet(),
                onOptionSelected = { onApplyFilter(ModelFilter.Category.CONTEXT_LENGTH, it) },
                onOptionDeselected = { onClearFilter(ModelFilter.Category.CONTEXT_LENGTH) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Pricing filters
            FilterChipRow(
                title = stringResource(R.string.pricing_tiers),
                options = ModelFilter.PricingTiers.ALL,
                selectedOptions = currentFilters[ModelFilter.Category.PRICING] ?: emptySet(),
                onOptionSelected = { onApplyFilter(ModelFilter.Category.PRICING, it) },
                onOptionDeselected = { onClearFilter(ModelFilter.Category.PRICING) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Model cards section
        // Show loading indicator if loading
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.material3.CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.loading_models),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else if (models.isEmpty()) {
            // No models found
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.no_models_found),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { onRefresh() }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.refresh_models))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.click_refresh_to_load_models),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Display model cards
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filter models by search query if provided
                val filteredModels = if (searchQuery.isNotEmpty()) {
                    models.filter { 
                        it.name.contains(searchQuery, ignoreCase = true) || 
                        it.providerName.contains(searchQuery, ignoreCase = true) 
                    }
                } else {
                    models
                }
                
                items(filteredModels) { model ->
                    ModelCard(
                        model = model,
                        isSelected = selectedModelId == model.id,
                        onClick = { onModelSelected(model.id) }
                    )
                }
                
                // Load more button if there are more models
                if (hasMoreModels) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            OutlinedButton(onClick = { onLoadMore() }) {
                                Text(stringResource(R.string.load_more_models))
                            }
                        }
                    }
                }
                
                // Show a message if no models are found after filtering
                if (filteredModels.isEmpty() && !isLoading && models.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.no_matching_models),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Collapsible help section with OpenRouter guidance.
 */
@Composable
fun OpenRouterHelpSection() {
    var expanded by remember { mutableStateOf(false) }
    var showTroubleshooting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val noBrowserAvailableDes=  stringResource(R.string.no_browser_available)

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
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Connection Status Indicator
                ConnectionStatusIndicator()
                
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
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
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
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
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
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
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
                
                // Documentation & Help Links
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Documentation Button
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://openrouter.ai/docs"))
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
                        }
                    ) {
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = "Open documentation"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.open_openrouter_docs))
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Troubleshooting Button
                    OutlinedButton(
                        onClick = { showTroubleshooting = !showTroubleshooting },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (showTroubleshooting) 
                                MaterialTheme.colorScheme.secondaryContainer
                            else 
                                MaterialTheme.colorScheme.surface
                        )
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
                    TroubleshootingGuide()
                }
            }
        }
    }
}

/**
 * Component that displays the current connection status to OpenRouter.
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
            modifier = Modifier.padding(start = 24.dp)
        )
    }
}

/**
 * Displays a message when API key validation is required.
 */
@Composable
fun ApiKeyRequiredMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Key,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.enter_valid_api_key),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
} 