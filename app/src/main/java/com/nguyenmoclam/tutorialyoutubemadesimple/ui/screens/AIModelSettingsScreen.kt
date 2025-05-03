package com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nguyenmoclam.tutorialyoutubemadesimple.BuildConfig
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.ApiKeyValidationState
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.ModelFilter
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.ModelInfo
import com.nguyenmoclam.tutorialyoutubemadesimple.navigation.AppScreens
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.ApiKeyRequiredMessage
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.CreditAndUsageMonitoring
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.ModelSelectionComponent
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.OpenRouterHelpSection
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.TrialRemainingWarning
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.AuthViewModel
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.SettingsViewModel
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.UsageViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIModelSettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel,
    usageViewModel: UsageViewModel = hiltViewModel(),
    authViewModel: AuthViewModel
) {
    val state = settingsViewModel.settingsState
    val creditStatusState by usageViewModel.creditStatusState.collectAsStateWithLifecycle()
    val tokenUsageSummaryState by usageViewModel.tokenUsageSummary.collectAsStateWithLifecycle()
    val selectedTimeRange by usageViewModel.timeRange.collectAsStateWithLifecycle()
    val displayedModels by settingsViewModel.displayedModels.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    // Add current user state
    val currentUser by settingsViewModel.currentUser.collectAsStateWithLifecycle()
    val freeCallsRemaining by authViewModel.freeCallsStateFlow.collectAsState()
    val context = LocalContext.current

    val localTrialApiKey = BuildConfig.OPENROUTER_API_KEY
    val shouldClearApiKey = freeCallsRemaining == 0 && state.openRouterApiKey == localTrialApiKey

    LaunchedEffect(shouldClearApiKey) {
        if (shouldClearApiKey) {
            settingsViewModel.setOpenRouterApiKey("")
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ai_model_settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp) // Add padding around the content
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            // Show trial remaining warning if the user is logged in and has limited calls remaining
            if (currentUser != null && freeCallsRemaining in 0..3) {
                TrialRemainingWarning(
                    callsRemaining = freeCallsRemaining ?: 0,
                    onGetApiKey = {
                        // Open OpenRouter keys page in browser
                        val intent =
                            Intent(Intent.ACTION_VIEW, "https://openrouter.ai/keys".toUri())
                        context.startActivity(intent)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Moved AIModelSettings Content
            AIModelSettingsContent(
                openRouterApiKey = state.openRouterApiKey,
                onApiKeyChanged = settingsViewModel::setOpenRouterApiKey,
                selectedModel = state.selectedModel,
                onModelSelected = settingsViewModel::setSelectedModel,
                currentCredits = state.apiKeyCredits, // Keep for now, maybe remove later
                isLoading = settingsViewModel.isLoadingModels, // Use viewModel loading state
                validationState = state.apiKeyValidationState,
                models = displayedModels, // <-- Use the computed list here
                onRefreshModels = settingsViewModel::fetchModels,
                // Pass filter/sort state and callbacks correctly
                currentFilters = state.modelFilters,
                currentSortOption = state.modelSortOption,
                onApplyFilter = settingsViewModel::applyModelFilter,
                onClearFilter = settingsViewModel::clearModelFilter,
                onSetSortOption = settingsViewModel::setModelSortOption,
                // Pass pagination state and callbacks (currently placeholders)
                onLoadMoreModels = { /* settingsViewModel::loadMoreModels */ }, // Connect if/when implemented
                hasMoreModels = false, // <-- Use actual state if/when implemented (e.g., settingsViewModel.hasMoreModels)
                // Credits and Usage from UsageViewModel state
                creditStatusState = creditStatusState,
                tokenUsageSummaryState = tokenUsageSummaryState,
                selectedTimeRange = selectedTimeRange,
                onTimeRangeSelected = usageViewModel::setTimeRange,
                onRefreshCredits = usageViewModel::refreshCreditStatus,
                onViewAllModelsClick = {
                    navController.navigate(AppScreens.DetailedModelUsage.route)
                }
            )
        }
    }
}


/**
 * AI Model Settings content component that displays AI model configuration options.
 * (Previously AIModelSettings in SettingsComponents.kt)
 */
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AIModelSettingsContent(
    openRouterApiKey: String,
    onApiKeyChanged: (String) -> Unit,
    selectedModel: String,
    onModelSelected: (String) -> Unit,
    currentCredits: Double,
    isLoading: Boolean = false,
    validationState: ApiKeyValidationState = ApiKeyValidationState.NOT_VALIDATED,
    models: List<ModelInfo> = emptyList(),
    onRefreshModels: () -> Unit = {},
    onApplyFilter: (ModelFilter.Category, String) -> Unit = { _, _ -> },
    onClearFilter: (ModelFilter.Category) -> Unit = { _ -> },
    onSetSortOption: (ModelFilter.SortOption) -> Unit = { _ -> },
    currentFilters: Map<ModelFilter.Category, Set<String>> = emptyMap(),
    currentSortOption: ModelFilter.SortOption = ModelFilter.SortOption.TOP_WEEKLY,
    onLoadMoreModels: () -> Unit = {},
    hasMoreModels: Boolean = false,
    creditStatusState: UsageViewModel.CreditStatusState = UsageViewModel.CreditStatusState.Loading,
    tokenUsageSummaryState: UsageViewModel.TokenUsageSummaryState = UsageViewModel.TokenUsageSummaryState.Loading,
    selectedTimeRange: UsageViewModel.TimeRange = UsageViewModel.TimeRange.LAST_30_DAYS,
    onTimeRangeSelected: (UsageViewModel.TimeRange) -> Unit = {},
    onRefreshCredits: () -> Unit = {},
    onViewAllModelsClick: () -> Unit = {}
) {
    // State for password visibility
    var passwordVisible by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    // State for Model Selection Bottom Sheet
    var showModelSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Find the name of the currently selected model
    val selectedModelInfo = remember(models, selectedModel) {
        models.find { it.id == selectedModel }
    }
    val selectedModelName = selectedModelInfo?.name ?: stringResource(R.string.no_model_selected)

    Column(modifier = Modifier.fillMaxWidth()) {
        // --- API Key Section ---
        Text(
            stringResource(R.string.api_key_label),
            style = MaterialTheme.typography.titleLarge // Make section titles larger
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            stringResource(R.string.api_key_description),
            style = MaterialTheme.typography.bodyMedium, // Slightly larger body text
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // API key input field with password visibility toggle
        OutlinedTextField(
            value = openRouterApiKey,
            onValueChange = {
                onApiKeyChanged(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.api_key_label)) },
            placeholder = { Text(stringResource(R.string.api_key_hint)) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            trailingIcon = {
                Row {
                    // Paste button
                    IconButton(onClick = {
                        clipboardManager.getText()?.text?.let {
                            onApiKeyChanged(it)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = stringResource(R.string.api_key_paste)
                        )
                    }

                    // Password visibility toggle
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = stringResource(R.string.api_key_toggle_visibility)
                        )
                    }
                }
            },
            // Show validation indicator
            leadingIcon = when (validationState) {
                ApiKeyValidationState.VALID -> {
                    {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Valid",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                ApiKeyValidationState.INVALID, ApiKeyValidationState.INVALID_FORMAT -> {
                    {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Invalid",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                ApiKeyValidationState.VALIDATING -> {
                    {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(4.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }

                else -> null
            },
            // Set colors based on validation state
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = when (validationState) {
                    ApiKeyValidationState.VALID -> MaterialTheme.colorScheme.primary
                    ApiKeyValidationState.INVALID, ApiKeyValidationState.INVALID_FORMAT -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                },
                unfocusedBorderColor = when (validationState) {
                    ApiKeyValidationState.VALID -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    ApiKeyValidationState.INVALID, ApiKeyValidationState.INVALID_FORMAT -> MaterialTheme.colorScheme.error.copy(
                        alpha = 0.5f
                    )

                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                }
            ),
            supportingText = {
                Text(
                    text = when (validationState) {
                        ApiKeyValidationState.INVALID_FORMAT -> stringResource(R.string.api_key_format_error)
                        ApiKeyValidationState.INVALID -> stringResource(R.string.api_key_validation_error)
                        ApiKeyValidationState.VALID -> stringResource(R.string.api_key_validation_success)
                        ApiKeyValidationState.VALIDATING -> stringResource(R.string.api_key_validating)
                        else -> stringResource(R.string.api_key_helper)
                    },
                    color = when (validationState) {
                        ApiKeyValidationState.INVALID_FORMAT, ApiKeyValidationState.INVALID -> MaterialTheme.colorScheme.error
                        ApiKeyValidationState.VALID -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        )

        // Status Indicator (Using Card instead of Chip for better visibility)
        if (validationState != ApiKeyValidationState.NOT_VALIDATED) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Card(
                    modifier = Modifier.padding(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (validationState) {
                            ApiKeyValidationState.VALID -> MaterialTheme.colorScheme.primaryContainer
                            ApiKeyValidationState.INVALID, ApiKeyValidationState.INVALID_FORMAT -> MaterialTheme.colorScheme.errorContainer
                            ApiKeyValidationState.VALIDATING -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Leading icon
                        when (validationState) {
                            ApiKeyValidationState.VALID -> {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            ApiKeyValidationState.INVALID, ApiKeyValidationState.INVALID_FORMAT -> {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            ApiKeyValidationState.VALIDATING -> {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(2.dp),
                                    strokeWidth = 2.dp
                                )
                            }

                            else -> {}
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Text
                        Text(
                            text = when (validationState) {
                                ApiKeyValidationState.VALID -> stringResource(R.string.api_key_validation_success)
                                ApiKeyValidationState.INVALID -> stringResource(R.string.api_key_validation_error)
                                ApiKeyValidationState.INVALID_FORMAT -> stringResource(R.string.api_key_format_error)
                                ApiKeyValidationState.VALIDATING -> stringResource(R.string.api_key_validating)
                                else -> ""
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

        // Divider for visual separation
        Spacer(modifier = Modifier.height(24.dp))
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // --- Credits and Usage Monitoring Section ---
        if (validationState == ApiKeyValidationState.VALID) {
            CreditAndUsageMonitoring(
                creditStatusState = creditStatusState,
                tokenUsageSummaryState = tokenUsageSummaryState,
                selectedTimeRange = selectedTimeRange,
                onTimeRangeSelected = onTimeRangeSelected,
                onRefreshCredits = onRefreshCredits,
                onViewAllModelsClick = onViewAllModelsClick
            )

            Spacer(modifier = Modifier.height(24.dp))
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }

        // --- Model Selection Section - Changed to trigger BottomSheet ---
        Text(
            stringResource(R.string.select_ai_model),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (validationState == ApiKeyValidationState.VALID) {
            // Clickable Card to show current selection and open sheet
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { showModelSheet = true }),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.selected_model),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = selectedModelName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(R.string.change_model),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            // Show message requiring valid API key
            ApiKeyRequiredMessage()
        }

        // Divider for visual separation
        Spacer(modifier = Modifier.height(24.dp))
        Divider(modifier = Modifier.padding(vertical = 8.dp))


        // --- Help Section ---
        OpenRouterHelpSection()
        Spacer(modifier = Modifier.height(80.dp)) // Add space at the bottom
    }

    // --- Modal Bottom Sheet for Model Selection ---
    if (showModelSheet) {
        ModalBottomSheet(
            onDismissRequest = { showModelSheet = false },
            sheetState = sheetState,
            // Consider adding dragHandle
            dragHandle = { BottomSheetDefaults.DragHandle() },
            // Optional: Set window insets to handle keyboard, etc.
            windowInsets = WindowInsets.ime.union(WindowInsets.navigationBars)
        ) {
            // Content of the Bottom Sheet
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    //.fillMaxHeight(fraction = 0.95f)
                    // Add padding inside the sheet
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    // Ensure content doesn't go under navigation bars inside the sheet
                    .navigationBarsPadding()
                    // Limit max height to prevent sheet taking full screen unnecessarily
                    .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.85f)
            ) {
                // Title inside the sheet
                Text(
                    stringResource(R.string.select_ai_model),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ModelSelectionComponent(
                    models = models, // Pass the displayed (filtered/sorted) list
                    selectedModelId = selectedModel,
                    onModelSelected = { modelId ->
                        onModelSelected(modelId) // Call original ViewModel function
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showModelSheet = false
                            }
                        }
                    },
                    currentFilters = currentFilters,
                    currentSortOption = currentSortOption,
                    onApplyFilter = onApplyFilter,
                    onClearFilter = onClearFilter,
                    onSetSortOption = onSetSortOption,
                    isLoading = isLoading,
                    onRefresh = onRefreshModels,
                    onLoadMore = onLoadMoreModels,
                    hasMoreModels = hasMoreModels
                )
                // Add some space at the bottom of the sheet content
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}



