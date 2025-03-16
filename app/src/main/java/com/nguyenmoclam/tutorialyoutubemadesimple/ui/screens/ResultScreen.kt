package com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.ProcessingCreateStep
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizViewModel

/**
 * Result screen composable that displays the summarized content and sharing options.
 *
 * Features:
 * - Loading state with progress indicator
 * - Error state with return button
 * - WebView for displaying HTML content
 * - Export and share functionality
 * - Responsive layout with proper spacing
 *
 * Implementation details:
 * - Uses AndroidView to embed WebView
 * - Configures WebView settings for optimal viewing
 * - Handles JavaScript and zoom controls
 * - Provides file sharing capabilities
 *
 * @param viewModel SummaryViewModel instance containing the summary data
 * @param navController NavHostController for navigation management
 */
/**
 * Dialog component that confirms user's intention to exit the result screen.
 * Provides a Material Design dialog with localized text and two action buttons.
 *
 * @param onDismiss Callback invoked when the dialog is dismissed or canceled
 * @param onConfirm Callback invoked when the user confirms the exit action
 */
@Composable
private fun ExitSummaryConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(context.getString(R.string.exit_dialog_title)) },
        text = { Text(context.getString(R.string.exit_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(context.getString(R.string.exit_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(context.getString(R.string.exit_dialog_cancel))
            }
        }
    )
}

/**
 * Loading state component that displays a progress indicator and current processing step.
 * Features animated text transitions between different processing steps.
 *
 * @param currentStep Current processing step from the ViewModel
 */
@Composable
private fun LoadingContent(currentStep: ProcessingCreateStep) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(8.dp))
        // Animated text that smoothly transitions between processing steps
        AnimatedContent(
            targetState = if (currentStep != ProcessingCreateStep.NONE) {
                currentStep.getMessage(context)
            } else {
                context.getString(R.string.loading_summary)
            },
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "Processing Step Animation"
        ) { text ->
            Text(text = text)
        }
    }
}

/**
 * Error state component that displays error message and a back button.
 * Centers content vertically and horizontally in the available space.
 *
 * @param errorMessage Error message to display, falls back to generic error if null
 * @param onBackClick Callback invoked when the back button is clicked
 */
@Composable
private fun ErrorContent(
    errorMessage: String?,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = errorMessage ?: context.getString(R.string.error_generic),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(16.dp)
        )
        Button(onClick = onBackClick) {
            Text(context.getString(R.string.back_button))
        }
    }
}

/**
 * WebView component for displaying HTML content with optimized settings.
 * Configures WebView with JavaScript support, zoom controls, and proper content scaling.
 *
 * Features:
 * - JavaScript enabled for dynamic content
 * - Pinch-to-zoom support with hidden zoom controls
 * - Content scaling for better readability
 * - UTF-8 encoding for proper text rendering
 *
 * @param summaryData HTML content to display in the WebView
 * @param modifier Modifier for customizing the WebView's layout
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SummaryWebView(
    summaryData: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                // Configure WebView settings for optimal viewing experience
                settings.apply {
                    javaScriptEnabled = true  // Enable JavaScript for dynamic content
                    useWideViewPort = true    // Use the viewport meta tag
                    loadWithOverviewMode = true  // Scale content to fit the viewport
                    setSupportZoom(true)      // Enable pinch-to-zoom
                    builtInZoomControls = true  // Enable zoom controls
                    displayZoomControls = false  // Hide the zoom controls UI
                }
                webViewClient = WebViewClient()
                // Load HTML content with proper encoding
                loadDataWithBaseURL(
                    null,
                    summaryData,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        modifier = modifier
    )
}

/**
 * Success state component that displays the summarized content and export button.
 * Provides a scrollable WebView for content and a bottom-aligned export button.
 *
 * @param summaryData HTML content to display in the WebView
 * @param onExportClick Callback invoked when the export button is clicked
 */
@Composable
private fun SuccessContent(
    summaryData: String,
    onExportClick: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        // Only show WebView if there's content to display
        if (summaryData.isNotEmpty()) {
            SummaryWebView(
                summaryData = summaryData,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)  // Take remaining space after export button
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onExportClick) {
            Text(context.getString(R.string.export_share_button))
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ResultScreen(viewModel: QuizViewModel, navController: NavHostController) {
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        ExitSummaryConfirmationDialog(
            onDismiss = { showExitDialog = false },
            onConfirm = {
                showExitDialog = false
                navController.popBackStack()
            }
        )
    }

    when {
        viewModel.isLoading -> LoadingContent(viewModel.currentStep)
        viewModel.errorMessage != null -> {
            ErrorContent(
                errorMessage = viewModel.errorMessage,
                onBackClick = { navController.popBackStack() }
            )
        }

        else -> {
            SuccessContent(
                summaryData = viewModel.summaryText,
                onExportClick = { viewModel.exportSummaryToHtml(context) }
            )
        }
    }
}