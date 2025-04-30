package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import androidx.navigation.NavHostController
import com.nguyenmoclam.tutorialyoutubemadesimple.navigation.AppScreens

/**
 * Dialog to inform users about API requirements (network, API key, or model selection)
 */
@Composable
fun ApiRequirementDialog(
    title: String,
    message: String,
    showSettingsButton: Boolean = false,
    navController: NavHostController? = null,
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
                        navController.navigate(AppScreens.Settings.route)
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