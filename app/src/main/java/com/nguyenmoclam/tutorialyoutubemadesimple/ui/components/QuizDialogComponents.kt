package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.nguyenmoclam.tutorialyoutubemadesimple.R

/**
 * Dialog shown when user attempts to exit the quiz before completion
 */
@Composable
fun ExitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(context.getString(R.string.exit_quiz_dialog_title)) },
        text = { Text(context.getString(R.string.exit_quiz_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(context.getString(R.string.exit_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(context.getString(R.string.continue_quiz_button))
            }
        }
    )
}