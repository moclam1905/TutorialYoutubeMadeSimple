package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nguyenmoclam.tutorialyoutubemadesimple.R

/**
 * Dialog shown when user attempts to exit the quiz before completion
 */
@Composable
fun ExitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.exit_quiz_dialog_title)) },
        text = { Text(stringResource(R.string.exit_quiz_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.exit_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.continue_quiz_button))
            }
        }
    )
}