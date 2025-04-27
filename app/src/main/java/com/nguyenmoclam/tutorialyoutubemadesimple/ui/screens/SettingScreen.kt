package com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.AppInfoSettings
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.DataManagementSettings
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.GoogleAccountSettings
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.LanguageSettings
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.NetworkSettings
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.ThemeSettings
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.AIModelSettings
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.SettingsViewModel
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.ApiKeyValidationState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.UsageViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * SettingScreen composable that displays all app settings organized by category.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    viewModel: SettingsViewModel,
    usageViewModel: UsageViewModel
) {
    val state = viewModel.settingsState
    var showResetDialog by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    
    // Collect states from UsageViewModel
    val creditStatusState by usageViewModel.creditStatusState.collectAsStateWithLifecycle()
    val tokenUsageSummaryState by usageViewModel.tokenUsageSummary.collectAsStateWithLifecycle()
    val selectedTimeRange by usageViewModel.timeRange.collectAsStateWithLifecycle()

    // Set up the Google Sign-In activity result launcher
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        viewModel.handleSignInResult(task)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
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
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            // Theme Settings Section
            SettingsSection(
                title = stringResource(R.string.theme),
                icon = Icons.Default.Settings
            ) {
                ThemeSettings(state, viewModel::setThemeMode)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Account Section
            SettingsSection(
                title = stringResource(R.string.google_account),
                icon = Icons.Default.AccountCircle
            ) {
                GoogleAccountSettings(
                    state = state,
                    onGoogleSignInChanged = viewModel::setGoogleSignIn,
                    onTranscriptModeChanged = viewModel::setTranscriptMode,
                    onClearAccountDataClick = viewModel::clearAccountData,
                    onSignInClick = { signInLauncher.launch(viewModel.getSignInIntent()) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Data Management Section
            SettingsSection(
                title = stringResource(R.string.data_management),
                icon = Icons.Default.Storage
            ) {
                var showClearQuizHistoryDialog by remember { mutableStateOf(false) }
                var showResetLearningProgressDialog by remember { mutableStateOf(false) }
                var showClearCacheDialog by remember { mutableStateOf(false) }

                DataManagementSettings(
                    state = state,
                    onClearQuizHistoryClick = { showClearQuizHistoryDialog = true },
                    onResetLearningProgressClick = { showResetLearningProgressDialog = true },
                    onClearCacheClick = { showClearCacheDialog = true }
                )

                // Clear Quiz History Dialog
                if (showClearQuizHistoryDialog) {
                    AlertDialog(
                        title = { Text(stringResource(R.string.confirm_delete_history)) },
                        text = { Text(stringResource(R.string.delete_history_message)) },
                        onDismissRequest = { showClearQuizHistoryDialog = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.clearQuizHistory()
                                    showClearQuizHistoryDialog = false
                                }
                            ) {
                                Text(stringResource(R.string.delete))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showClearQuizHistoryDialog = false }) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    )
                }

                // Reset Learning Progress Dialog
                if (showResetLearningProgressDialog) {
                    AlertDialog(
                        title = { Text(stringResource(R.string.confirm_reset_progress)) },
                        text = { Text(stringResource(R.string.reset_progress_message)) },
                        onDismissRequest = { showResetLearningProgressDialog = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.resetLearningProgress()
                                    showResetLearningProgressDialog = false
                                }
                            ) {
                                Text(stringResource(R.string.reset))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showResetLearningProgressDialog = false }) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    )
                }

                // Clear Cache Dialog
                if (showClearCacheDialog) {
                    AlertDialog(
                        title = { Text(stringResource(R.string.confirm_clear_cache)) },
                        text = { Text(stringResource(R.string.clear_cache_message)) },
                        onDismissRequest = { showClearCacheDialog = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.clearCache()
                                    showClearCacheDialog = false
                                }
                            ) {
                                Text(stringResource(R.string.delete))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showClearCacheDialog = false }) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Network Settings Section
            SettingsSection(
                title = stringResource(R.string.network_settings),
                icon = Icons.Default.NetworkWifi
            ) {
                NetworkSettings(
                    state = state,
                    onDataSaverModeChanged = viewModel::setDataSaverMode,
                    onConnectionTypeChanged = viewModel::setConnectionType,
                    onConnectionTimeoutChanged = viewModel::setConnectionTimeout,
                    onRetryPolicyChanged = viewModel::setRetryPolicy
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Language Settings Section
            SettingsSection(
                title = stringResource(R.string.language),
                icon = Icons.Default.Language
            ) {
                LanguageSettings(
                    state = state,
                    onAppLanguageChanged = viewModel::setAppLanguage
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Model Settings Section
            SettingsSection(
                title = stringResource(R.string.ai_model_settings),
                icon = Icons.Default.SmartToy
            ) {
                AIModelSettings(
                    openRouterApiKey = state.openRouterApiKey,
                    onApiKeyChanged = viewModel::setOpenRouterApiKey,
                    selectedModel = state.selectedModel,
                    onModelSelected = viewModel::setSelectedModel,
                    currentCredits = state.apiKeyCredits,
                    isLoading = state.apiKeyValidationState == ApiKeyValidationState.VALIDATING,
                    validationState = state.apiKeyValidationState,
                    creditStatusState = creditStatusState,
                    tokenUsageSummaryState = tokenUsageSummaryState,
                    selectedTimeRange = selectedTimeRange,
                    onTimeRangeSelected = usageViewModel::setTimeRange,
                    onRefreshCredits = usageViewModel::refreshCreditStatus
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Information Section
            SettingsSection(
                title = stringResource(R.string.app_information),
                icon = Icons.Default.Info
            ) {
                AppInfoSettings(
                    appVersion = viewModel.getAppVersion(),
                    onGitHubClick = { /* Open GitHub URL */ },
                    onPrivacyPolicyClick = { /* Open Privacy Policy */ },
                    onContactClick = { /* Open Contact Info */ },
                    onLicenseInfoClick = { /* Open License Info */ }
                )
            }

            // Reset All Settings button
            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.reset_all_settings),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Reset settings confirmation dialog
        if (showResetDialog) {
            AlertDialog(
                title = { Text(stringResource(R.string.reset_settings_title)) },
                text = { Text(stringResource(R.string.reset_settings_message)) },
                onDismissRequest = { showResetDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Reset all settings to defaults
                            viewModel.setThemeMode("system")
                            viewModel.setQuestionOrder("sequential")
                            viewModel.setMaxRetryCount(1)
                            viewModel.setShowAnswerAfterWrong(false)
                            viewModel.setAutoNextQuestion(false)
                            viewModel.setDataSaverMode(false)
                            viewModel.setConnectionType("any")
                            viewModel.setConnectionTimeout(120)
                            viewModel.setRetryPolicy("exponential")
                            viewModel.setAppLanguage("system")
                            showResetDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.reset))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

/**
 * Reusable section component for settings categories
 */
@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 0.dp,
            focusedElevation = 4.dp
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Section header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section content
            content()
        }
    }
}