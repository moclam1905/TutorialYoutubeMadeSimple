package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizViewModel

/**
 * Displays error messages from the ViewModel when present and not in loading state.
 *
 * @param viewModel The SummaryViewModel containing error state and loading status
 */
@Composable
fun ErrorMessage(viewModel: QuizViewModel) {
    // Only show error message if it exists and we're not in loading state
    if (viewModel.errorMessage != null && !viewModel.isLoading) {
        Text(
            text = viewModel.errorMessage ?: "",
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(8.dp)
        )
    }
}