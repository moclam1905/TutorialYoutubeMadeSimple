package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.ApiKeyValidationState
import com.nguyenmoclam.tutorialyoutubemadesimple.navigation.AppScreens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Dialog shown when the user's free trial is exhausted and they need to enter an API key
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrialExhaustedDialog(
    apiKey: String,
    onApiKeyChanged: (String) -> Unit,
    validationState: ApiKeyValidationState = ApiKeyValidationState.NOT_VALIDATED,
    onDismiss: () -> Unit,
    onOpenApiSettings: () -> Unit,
    onValidateApiKey: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var passwordVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(apiKey) {
        if (apiKey.isNotBlank() && apiKey.length >= 10 && validationState != ApiKeyValidationState.VALIDATING) {
            delay(800)
            onValidateApiKey?.invoke(apiKey)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = {
            Text(
                text = stringResource(R.string.trial_exhausted_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.trial_exhausted_message),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // API key input field with visibility toggle
                OutlinedTextField(
                    value = apiKey,
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
                                    coroutineScope.launch {
                                        delay(500)
                                        onValidateApiKey?.invoke(it)
                                    }
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
                                        .width(24.dp)
                                        .height(24.dp)
                                        .padding(4.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                        else -> null
                    },
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // "Get API Key" button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.trial_exhausted_api_key_prompt),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://openrouter.ai/keys"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(stringResource(R.string.get_api_key_button))
                        }
                    }
                }
                
                // Display trial status
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = stringResource(R.string.trial_0_remaining),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validationState == ApiKeyValidationState.VALID) {
                        onDismiss()
                    } else {
                        onOpenApiSettings()
                    }
                },
                enabled = true
            ) {
                Text(
                    if (validationState == ApiKeyValidationState.VALID) {
                        stringResource(R.string.ok)
                    } else {
                        stringResource(R.string.open_settings_button)
                    }
                )
            }
        },
        dismissButton = {
            if (validationState != ApiKeyValidationState.VALID) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    )
}

/**
 * Dialog to inform users about API requirements (network, API key, or model selection)
 * Used for other API-related errors or requirements
 */
@Composable
fun ApiRequirementDialog(
    title: String,
    message: String,
    showSettingsButton: Boolean = false,
    navController: NavController? = null,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            if (showSettingsButton && navController != null) {
                Button(
                    onClick = {
                        navController.navigate(AppScreens.AIModelSettings.route)
                        onDismiss()
                    }
                ) {
                    Text(stringResource(R.string.go_to_settings))
                }
            } else {
                Button(onClick = onDismiss) {
                    Text(stringResource(R.string.ok))
                }
            }
        },
        dismissButton = {
            if (showSettingsButton && navController != null) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    )
}

/**
 * Component to display a warning about the number of trial calls remaining
 */
@Composable
fun TrialRemainingWarning(
    callsRemaining: Int,
    onGetApiKey: () -> Unit = {}
) {
    // Only show warning when fewer than 4 calls remain
    if (callsRemaining >= 4) return
    
    val backgroundColor = when (callsRemaining) {
        0 -> MaterialTheme.colorScheme.errorContainer
        1 -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
        2 -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
    }
    
    val textColor = if (callsRemaining <= 1) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }
    
    val messageId = if (callsRemaining <= 1) {
        R.string.trial_remaining_low
    } else {
        R.string.trial_remaining_normal
    }
    
    // Create visual indicator of remaining calls
    val maxCalls = 10
    val remainingRatio = callsRemaining.toFloat() / maxCalls
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (callsRemaining <= 1) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = stringResource(messageId, callsRemaining),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (callsRemaining <= 2) {
                    TextButton(onClick = onGetApiKey) {
                        Text(
                            text = stringResource(R.string.trial_get_own_key),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            
            // Add visual indicator
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), shape = RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(remainingRatio)
                        .height(8.dp)
                        .background(
                            color = when {
                                callsRemaining <= 1 -> MaterialTheme.colorScheme.error
                                callsRemaining <= 3 -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            },
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
} 