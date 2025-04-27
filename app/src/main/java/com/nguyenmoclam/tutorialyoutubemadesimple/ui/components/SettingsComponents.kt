package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.SettingsState
import kotlin.math.log10
import kotlin.math.pow
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.TextField
import androidx.compose.material.icons.filled.ArrowDropDown
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.ApiKeyValidationState

/**
 * Theme settings component that allows selecting between light, dark, and system theme modes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettings(
    state: SettingsState,
    onThemeModeChanged: (String) -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedTheme by remember { mutableStateOf("") }
    var previewTheme by remember { mutableStateOf(state.themeMode) }

    Column(modifier = Modifier.selectableGroup()) {
        val themeOptions = listOf(
            "light" to stringResource(R.string.light_mode),
            "dark" to stringResource(R.string.dark_mode),
            "system" to stringResource(R.string.system_default)
        )

        themeOptions.forEach { (value, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = state.themeMode == value,
                        onClick = {
                            selectedTheme = value
                            previewTheme = value
                            showConfirmDialog = true
                        }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = state.themeMode == value,
                    onClick = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(label)
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showConfirmDialog = false
                previewTheme = state.themeMode
            },
            title = { Text(stringResource(R.string.theme_change_dialog_title)) },
            text = { Text(stringResource(R.string.theme_change_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onThemeModeChanged(selectedTheme)
                        showConfirmDialog = false
                    }
                ) {
                    Text(stringResource(R.string.apply_button))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        previewTheme = state.themeMode
                    }
                ) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }
}

/**
 * Quiz configuration settings component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizConfigSettings(
    state: SettingsState,
    onQuestionOrderChanged: (String) -> Unit,
    onMaxRetryCountChanged: (Int) -> Unit,
    onShowAnswerAfterWrongChanged: (Boolean) -> Unit,
    onAutoNextQuestionChanged: (Boolean) -> Unit
) {
    Column {
        // Question order setting
        Text(stringResource(R.string.question_order), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.selectableGroup()) {
            val orderOptions = listOf(
                "sequential" to stringResource(R.string.sequential),
                "shuffle" to stringResource(R.string.random_shuffle)
            )

            orderOptions.forEach { (value, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = state.questionOrder == value,
                            onClick = { onQuestionOrderChanged(value) }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.questionOrder == value,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(label)
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Max retry count setting
        Text(
            stringResource(R.string.maximum_retry_count, state.maxRetryCount),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = state.maxRetryCount.toFloat(),
            onValueChange = { onMaxRetryCountChanged(it.toInt()) },
            valueRange = 0f..3f,
            steps = 2,
            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Show answer after wrong setting
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.show_answer_after_wrong))
            Switch(
                checked = state.showAnswerAfterWrong,
                onCheckedChange = onShowAnswerAfterWrongChanged
            )
        }

        // Auto next question setting
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.auto_next_question))
            Switch(
                checked = state.autoNextQuestion,
                onCheckedChange = onAutoNextQuestionChanged
            )
        }
    }
}

/**
 * Google account settings component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleAccountSettings(
    state: SettingsState,
    onGoogleSignInChanged: (Boolean) -> Unit,
    onTranscriptModeChanged: (String) -> Unit,
    onClearAccountDataClick: () -> Unit,
    onSignInClick: () -> Unit = {}
) {
    Column {
        // Google account status and sign-in/out buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(stringResource(R.string.google_account), fontWeight = FontWeight.Medium)
                Text(
                    text = if (state.isGoogleSignedIn) stringResource(R.string.signed_in) else stringResource(
                        R.string.not_signed_in
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.isGoogleSignedIn)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            TextButton(
                onClick = {
                    if (state.isGoogleSignedIn) {
                        onGoogleSignInChanged(false) // Sign out
                    } else {
                        onSignInClick() // Launch sign-in flow
                    }
                }
            ) {
                Text(
                    if (state.isGoogleSignedIn) stringResource(R.string.sign_out) else stringResource(
                        R.string.sign_in
                    )
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Transcript mode setting
        Text(stringResource(R.string.transcript_mode), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.selectableGroup()) {
            val modeOptions = listOf(
                "google" to stringResource(R.string.use_google_account),
                "anonymous" to stringResource(R.string.anonymous_mode)
            )

            modeOptions.forEach { (value, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = state.transcriptMode == value,
                            onClick = { onTranscriptModeChanged(value) }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.transcriptMode == value,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(label)
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Clear account data button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClearAccountDataClick)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.clear_account_data),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Data management settings component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementSettings(
    state: SettingsState,
    onClearQuizHistoryClick: () -> Unit,
    onResetLearningProgressClick: () -> Unit,
    onClearCacheClick: () -> Unit
) {
    Column {
        // Storage usage info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.storage_usage), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.storage_used, formatBytes(state.usedStorageBytes)))
                Text(stringResource(R.string.quizzes_count, state.quizCount))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Data management actions
        Column {
            SettingsItem(
                title = stringResource(R.string.clear_quiz_history),
                onClick = onClearQuizHistoryClick,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )

            Divider()

            SettingsItem(
                title = stringResource(R.string.reset_learning_progress),
                onClick = onResetLearningProgressClick,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ClearAll,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )

            Divider()

            SettingsItem(
                title = stringResource(R.string.clear_cache),
                onClick = onClearCacheClick,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}

/**
 * Network settings component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkSettings(
    state: SettingsState,
    onDataSaverModeChanged: (Boolean) -> Unit,
    onConnectionTypeChanged: (String) -> Unit,
    onConnectionTimeoutChanged: (Int) -> Unit,
    onRetryPolicyChanged: (String) -> Unit
) {
    Column {
        // Network status indicator
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (state.isNetworkAvailable) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Wifi,
                    contentDescription = "Network Status",
                    tint = if (state.isNetworkAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.isNetworkAvailable) stringResource(R.string.connected) else stringResource(
                        R.string.no_connection
                    ),
                    color = if (state.isNetworkAvailable) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Data saver mode toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.data_saver_mode))
                Text(
                    stringResource(R.string.reduce_data_usage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = state.dataSaverMode,
                onCheckedChange = onDataSaverModeChanged,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Connection type setting
        Text(
            stringResource(R.string.connection_type),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.selectableGroup()) {
            val connectionOptions = listOf(
                "any" to stringResource(R.string.allow_mobile_data),
                "wifi_only" to stringResource(R.string.wifi_only),
                "mobile_only" to stringResource(R.string.mobile_data_only)
            )

            connectionOptions.forEach { (value, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = state.connectionType == value,
                            onClick = { onConnectionTypeChanged(value) }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.connectionType == value,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(label)
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Connection timeout setting
        Text(
            stringResource(R.string.connection_timeout, state.connectionTimeout),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = state.connectionTimeout.toFloat(),
            onValueChange = { onConnectionTimeoutChanged(it.toInt()) },
            valueRange = 120f..240f,
            steps = 24,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        )
        
        // Min/max labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "120s",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "240s",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Retry policy setting
        Text(stringResource(R.string.retry_policy), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.selectableGroup()) {
            val policyOptions = listOf(
                "none" to stringResource(R.string.no_retry),
                "linear" to stringResource(R.string.linear_backoff),
                "exponential" to stringResource(R.string.exponential_backoff)
            )

            policyOptions.forEach { (value, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = state.retryPolicy == value,
                            onClick = { onRetryPolicyChanged(value) }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.retryPolicy == value,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(label)
                }
            }
        }
    }
}

/**
 * Language settings component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettings(
    state: SettingsState,
    onAppLanguageChanged: (String) -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("") }

    Column(modifier = Modifier.selectableGroup()) {
        val languageOptions = listOf(
            "en" to stringResource(R.string.english),
            "vi" to stringResource(R.string.vietnamese),
            "system" to stringResource(R.string.system_default)
        )

        languageOptions.forEach { (value, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = state.appLanguage == value,
                        onClick = {
                            if (value != state.appLanguage) {
                                selectedLanguage = value
                                showConfirmDialog = true
                            }
                        }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = state.appLanguage == value,
                    onClick = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(label)
            }
        }
    }

    // Confirmation dialog
    if (showConfirmDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.language_change_title)) },
            text = { Text(stringResource(R.string.language_change_message)) },
            onDismissRequest = { showConfirmDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAppLanguageChanged(selectedLanguage)
                        showConfirmDialog = false
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/**
 * App information settings component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppInfoSettings(
    appVersion: String,
    onGitHubClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onContactClick: () -> Unit,
    onLicenseInfoClick: () -> Unit
) {
    Column {
        // App version
        Text(
            stringResource(R.string.app_version, appVersion),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Links
        Column {
            SettingsItem(
                title = stringResource(R.string.github_repository),
                onClick = onGitHubClick,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
            
            Divider()
            
            SettingsItem(
                title = stringResource(R.string.privacy_policy),
                onClick = onPrivacyPolicyClick,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
            
            Divider()
            
            SettingsItem(
                title = stringResource(R.string.contact_information),
                onClick = onContactClick,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ContactSupport,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
            
            Divider()
            
            SettingsItem(
                title = stringResource(R.string.license_information),
                onClick = onLicenseInfoClick,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    }
}

/**
 * Helper function to format bytes into a human-readable string
 */
@SuppressLint("DefaultLocale")
private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()

    return String.format(
        "%.1f %s",
        bytes / 1024.0.pow(digitGroups.toDouble()),
        units[digitGroups]
    )
}

/**
 * Reusable settings item with consistent styling for clickable settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    showArrow: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Leading icon if provided
        leadingIcon?.let {
            Box(modifier = Modifier.padding(end = 16.dp)) {
                leadingIcon()
            }
        }
        
        // Text column (title and optional subtitle)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Arrow icon if showArrow is true
        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * AI Model Settings component that displays AI model configuration options.
 */
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIModelSettings(
    openRouterApiKey: String,
    onApiKeyChanged: (String) -> Unit,
    selectedModel: String,
    onModelSelected: (String) -> Unit,
    currentCredits: Double,
    isLoading: Boolean = false,
    validationState: ApiKeyValidationState = ApiKeyValidationState.NONE
) {
    // State for password visibility
    var passwordVisible by remember { mutableStateOf(false) }
    // State for dropdown menu expansion
    var isModelDropdownExpanded by remember { mutableStateOf(false) }
    // Placeholder for available models - replace with actual list
    val availableModels = listOf("google/gemini-flash-1.5", "openai/gpt-4o", "anthropic/claude-3.5-sonnet")

    // Clipboard manager for paste functionality
    val clipboardManager = LocalClipboardManager.current

    Column {
        // API Key Section
        Text(
            stringResource(R.string.api_key_label),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            stringResource(R.string.api_key_description),
            style = MaterialTheme.typography.bodySmall,
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
                    ApiKeyValidationState.INVALID, ApiKeyValidationState.INVALID_FORMAT -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
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

        // Status Indicator (Using Card instead of Chip)
        if (validationState != ApiKeyValidationState.NONE) {
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

        Spacer(modifier = Modifier.height(24.dp))

        // Model Selection Dropdown
        Text(
            stringResource(R.string.select_ai_model),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = isModelDropdownExpanded,
            onExpandedChange = {
                // Only allow expanding if API key is valid
                if (validationState == ApiKeyValidationState.VALID) {
                    isModelDropdownExpanded = !isModelDropdownExpanded
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = selectedModel.ifEmpty { stringResource(R.string.model_not_selected) },
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.selected_model_label)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isModelDropdownExpanded)
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                enabled = validationState == ApiKeyValidationState.VALID
            )

            ExposedDropdownMenu(
                expanded = isModelDropdownExpanded,
                onDismissRequest = { isModelDropdownExpanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                availableModels.forEach { model ->
                    DropdownMenuItem(
                        text = { Text(model) },
                        onClick = {
                            onModelSelected(model)
                            isModelDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Credits display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.available_credits), fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(8.dp))
                    // Show loading indicator next to credits title if loading
                    if (isLoading && validationState != ApiKeyValidationState.VALIDATING) {
                         CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                // Determine the text and color based on state
                val creditsText = when {
                    validationState == ApiKeyValidationState.VALID && !isLoading -> 
                        "$${String.format("%.2f", currentCredits)}"
                    isLoading -> stringResource(R.string.loading_credits)
                    else -> stringResource(R.string.credits_not_available)
                }
                val creditsColor = if (validationState == ApiKeyValidationState.VALID && !isLoading) {
                    MaterialTheme.colorScheme.primary 
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
                
                Text(
                    text = creditsText,
                    style = MaterialTheme.typography.headlineSmall,
                    color = creditsColor
                )
            }
        }
    }
}