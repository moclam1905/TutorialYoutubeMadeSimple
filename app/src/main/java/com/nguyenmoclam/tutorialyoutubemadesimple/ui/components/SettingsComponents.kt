package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.SettingsState

/**
 * Theme settings component that allows selecting between light, dark, and system theme modes
 */
@Composable
fun ThemeSettings(
    state: SettingsState,
    onThemeModeChanged: (String) -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedTheme by remember { mutableStateOf("") }
    var previewTheme by remember { mutableStateOf(state.themeMode) }
    val context = LocalContext.current

    Column(modifier = Modifier.selectableGroup()) {
        val themeOptions = listOf(
            "light" to "Light Mode",
            "dark" to "Dark Mode",
            "system" to "System Default"
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
            title = { Text(context.getString(R.string.theme_change_dialog_title)) },
            text = { Text(context.getString(R.string.theme_change_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onThemeModeChanged(selectedTheme)
                        showConfirmDialog = false
                    }
                ) {
                    Text(context.getString(R.string.apply_button))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        previewTheme = state.themeMode
                    }
                ) {
                    Text(context.getString(R.string.cancel_button))
                }
            }
        )
    }
}

/**
 * Quiz configuration settings component
 */
@Composable
fun QuizConfigSettings(
    state: SettingsState,
    onQuestionOrderChanged: (String) -> Unit,
    onMaxRetryCountChanged: (Int) -> Unit,
    onShowAnswerAfterWrongChanged: (Boolean) -> Unit,
    onAutoNextQuestionChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current

    Column {
        // Question order setting
        Text(context.getString(R.string.question_order), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.selectableGroup()) {
            val orderOptions = listOf(
                "sequential" to "Sequential",
                "shuffle" to "Random (Shuffle)"
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
            context.getString(R.string.maximum_retry_count, state.maxRetryCount),
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
            Text(context.getString(R.string.show_answer_after_wrong))
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
            Text(context.getString(R.string.auto_next_question))
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
@Composable
fun GoogleAccountSettings(
    state: SettingsState,
    onGoogleSignInChanged: (Boolean) -> Unit,
    onTranscriptModeChanged: (String) -> Unit,
    onClearAccountDataClick: () -> Unit,
    onSignInClick: () -> Unit = {}
) {
    val context = LocalContext.current

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
                Text(context.getString(R.string.google_account), fontWeight = FontWeight.Medium)
                Text(
                    text = if (state.isGoogleSignedIn) "Signed In" else "Not Signed In",
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
                Text(if (state.isGoogleSignedIn) "Sign Out" else "Sign In")
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Transcript mode setting
        Text(context.getString(R.string.transcript_mode), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.selectableGroup()) {
            val modeOptions = listOf(
                "google" to "Use Google Account",
                "anonymous" to "Anonymous Mode"
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
                text = "Clear Account Data",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Data management settings component
 */
@Composable
fun DataManagementSettings(
    state: SettingsState,
    onClearQuizHistoryClick: () -> Unit,
    onResetLearningProgressClick: () -> Unit,
    onClearCacheClick: () -> Unit
) {
    val context = LocalContext.current

    Column {
        // Storage usage info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(context.getString(R.string.storage_usage), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(context.getString(R.string.storage_used, formatBytes(state.usedStorageBytes)))
                Text(context.getString(R.string.quizzes_count, state.quizCount))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Data management actions
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClearQuizHistoryClick)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(context.getString(R.string.clear_quiz_history))
            }

            Divider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onResetLearningProgressClick)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(context.getString(R.string.reset_learning_progress))
            }

            Divider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClearCacheClick)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(context.getString(R.string.clear_cache))
            }
        }
    }
}

/**
 * Network settings component
 */
@Composable
fun NetworkSettings(
    state: SettingsState,
    onDataSaverModeChanged: (Boolean) -> Unit,
    onConnectionTypeChanged: (String) -> Unit,
    onConnectionTimeoutChanged: (Int) -> Unit,
    onRetryPolicyChanged: (String) -> Unit
) {
    val context = LocalContext.current

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
                    text = if (state.isNetworkAvailable) "Connected" else "No Connection",
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
            Column {
                Text(context.getString(R.string.data_saver_mode))
                Text(
                    "Reduces data usage by loading lower quality content",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = state.dataSaverMode,
                onCheckedChange = onDataSaverModeChanged
            )
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Connection type setting
        Text(
            context.getString(R.string.connection_type),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.selectableGroup()) {
            val connectionOptions = listOf(
                "any" to "Allow Mobile Data",
                "wifi_only" to "Wi-Fi Only"
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
            context.getString(R.string.connection_timeout, state.connectionTimeout),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = state.connectionTimeout.toFloat(),
            onValueChange = { onConnectionTimeoutChanged(it.toInt()) },
            valueRange = 10f..60f,
            steps = 5,
            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Retry policy setting
        Text(context.getString(R.string.retry_policy), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.selectableGroup()) {
            val policyOptions = listOf(
                "none" to "No Retry",
                "linear" to "Linear Backoff",
                "exponential" to "Exponential Backoff"
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
@Composable
fun LanguageSettings(
    state: SettingsState,
    onAppLanguageChanged: (String) -> Unit
) {
    Column(modifier = Modifier.selectableGroup()) {
        val languageOptions = listOf(
            "en" to "English",
            "vi" to "Tiếng Việt",
            "system" to "System Default"
        )

        languageOptions.forEach { (value, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = state.appLanguage == value,
                        onClick = { onAppLanguageChanged(value) }
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
}

/**
 * App information settings component
 */
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
        val context = LocalContext.current
        Text(
            context.getString(R.string.app_version, appVersion),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Links
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onGitHubClick)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(context.getString(R.string.github_repository))
            }

            Divider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onPrivacyPolicyClick)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(context.getString(R.string.privacy_policy))
            }

            Divider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onContactClick)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(context.getString(R.string.contact_information))
            }

            Divider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLicenseInfoClick)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(context.getString(R.string.license_information))
            }
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
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()

    return String.format(
        "%.1f %s",
        bytes / Math.pow(1024.0, digitGroups.toDouble()),
        units[digitGroups]
    )
}