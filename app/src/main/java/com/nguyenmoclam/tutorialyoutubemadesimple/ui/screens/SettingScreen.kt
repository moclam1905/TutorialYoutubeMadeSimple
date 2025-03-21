package com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.AppInfoSettings
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.DataManagementSettings
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.GoogleAccountSettings
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.LanguageSettings
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.NetworkSettings
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.QuizConfigSettings
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.ThemeSettings
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.SettingsViewModel

/**
 * SettingScreen composable that displays all app settings organized by category.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    viewModel: SettingsViewModel
) {
    val state = viewModel.settingsState
    var showResetDialog by remember { mutableStateOf(false) }
    
    // Set up the Google Sign-In activity result launcher
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        viewModel.handleSignInResult(task)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Theme Settings Section
            SettingsSection(
                title = "Theme",
                icon = Icons.Default.Settings
            ) {
                ThemeSettings(state, viewModel::setThemeMode)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
//            // Quiz Configuration Section
//            SettingsSection(
//                title = "Quiz Configuration",
//                icon = Icons.Default.Quiz
//            ) {
//                QuizConfigSettings(
//                    state = state,
//                    onQuestionOrderChanged = viewModel::setQuestionOrder,
//                    onMaxRetryCountChanged = viewModel::setMaxRetryCount,
//                    onShowAnswerAfterWrongChanged = viewModel::setShowAnswerAfterWrong,
//                    onAutoNextQuestionChanged = viewModel::setAutoNextQuestion
//                )
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
            
            // Google Account Section
            SettingsSection(
                title = "Google Account",
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
                title = "Data Management",
                icon = Icons.Default.Storage
            ) {
                DataManagementSettings(
                    state = state,
                    onClearQuizHistoryClick = viewModel::clearQuizHistory,
                    onResetLearningProgressClick = viewModel::resetLearningProgress,
                    onClearCacheClick = viewModel::clearCache
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Network Settings Section
            SettingsSection(
                title = "Network Settings",
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
                title = "Language",
                icon = Icons.Default.Language
            ) {
                LanguageSettings(
                    state = state,
                    onAppLanguageChanged = viewModel::setAppLanguage
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // App Information Section
            SettingsSection(
                title = "App Information",
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
                    "Reset All Settings",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        
        // Reset settings confirmation dialog
        if (showResetDialog) {
            AlertDialog(
                title = { Text("Reset All Settings") },
                text = { Text("Are you sure you want to reset all settings to default values? This action cannot be undone.") },
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
                            viewModel.setConnectionTimeout(30)
                            viewModel.setRetryPolicy("exponential")
                            viewModel.setAppLanguage("system")
                            showResetDialog = false
                        }
                    ) {
                        Text("Reset")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel")
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Section header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Divider()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Section content
            content()
        }
    }
}