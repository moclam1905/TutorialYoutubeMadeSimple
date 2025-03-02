package com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nguyenmoclam.tutorialyoutubemadesimple.MainActivity
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.SummaryViewModel

/**
 * Home screen composable that displays the URL/ID input interface.
 *
 * Features:
 * - Text input field for YouTube URL or video ID
 * - Error message display
 * - Summarize button with loading state handling
 * - Navigation to result screen on successful input
 *
 * @param viewModel SummaryViewModel instance for managing video data and processing
 * @param navController NavHostController for handling screen navigation
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun HomeScreen(
    viewModel: SummaryViewModel,
    navController: NavHostController
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ErrorMessage(viewModel)
        InputSection(textFieldValue) { textFieldValue = it }
        Spacer(modifier = Modifier.height(16.dp))
        ActionButton(viewModel, textFieldValue.text, navController)
    }
}

/**
 * Displays error messages from the ViewModel when present and not in loading state.
 * The error message is shown in the Material Design error color scheme.
 *
 * @param viewModel The SummaryViewModel containing error state and loading status
 */
@Composable
private fun ErrorMessage(viewModel: SummaryViewModel) {
    // Only show error message if it exists and we're not in loading state
    if (viewModel.errorMessage != null && !viewModel.isLoading) {
        Text(
            text = viewModel.errorMessage ?: "",
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(8.dp)
        )
    }
}

/**
 * Input field section for YouTube URL or video ID.
 * Provides a single-line outlined text field with localized label and placeholder.
 *
 * @param textFieldValue Current value of the text field
 * @param onValueChange Callback invoked when the text field value changes
 */
@Composable
private fun InputSection(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
) {
    val context = LocalContext.current
    OutlinedTextField(
        value = textFieldValue,
        onValueChange = onValueChange,
        label = { Text(context.getString(R.string.input_url_label)) },
        placeholder = { Text(context.getString(R.string.input_url_placeholder)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Action button that triggers video summarization process.
 * Handles the summarization workflow and navigation to results screen.
 *
 * Features:
 * - Initiates video summarization with provided input and API key
 * - Disables during loading state to prevent multiple submissions
 * - Navigates to result screen on successful input validation
 * - Uses localized button text
 *
 * @param viewModel SummaryViewModel to handle summarization process
 * @param inputText Current text input to process
 * @param navController Navigation controller for screen transitions
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun ActionButton(
    viewModel: SummaryViewModel,
    inputText: String,
    navController: NavHostController
) {
    val context = LocalContext.current
    Button(
        onClick = {
            // Start summarization process with input text and API key
            viewModel.startSummarization(
                inputText,
                MainActivity.YOUTUBE_API_KEY
            )
            // Navigate to result screen if no validation errors occurred
            if (viewModel.errorMessage == null) {
                navController.navigate("result")
            }
        },
        enabled = !viewModel.isLoading,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(context.getString(R.string.summarize_button))
    }
}