package com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.tag.Tag
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.ExitConfirmationDialog
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.ExpandableOutlinedTextField
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.LoadingState
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.QuizSettingTopAppBar
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.ReminderSettingsSection
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.TagsSettingsSection
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizSettingUiState
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizSettingViewModel
import kotlinx.coroutines.CoroutineScope

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuizSettingScreen(
    navController: NavController,
    viewModel: QuizSettingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope() // Get coroutine scope for snackbar

    // Handle back press: show confirmation if there are unsaved changes
    BackHandler(enabled = uiState.hasUnsavedChanges) {
        viewModel.attemptToGoBack()
    }

    // Show snackbar on save success or error
    LaunchedEffect(uiState.saveSuccess, uiState.errorMessage) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar(context.getString(R.string.settings_saved_success))
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage() // Clear error after showing
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            QuizSettingTopAppBar(
                onSaveClick = viewModel::saveSettings,
                onResetClick = viewModel::resetSettings,
                canSave = uiState.hasUnsavedChanges && !uiState.isLoading,
                canReset = uiState.hasUnsavedChanges && !uiState.isLoading
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.quiz == null) {
            LoadingState(progress = 0f, message = stringResource(R.string.loading_settings))
        } else if (uiState.quiz == null && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(uiState.errorMessage ?: stringResource(R.string.quiz_not_found_error))
            }
        } else {
            QuizSettingContent(
                modifier = Modifier.padding(paddingValues),
                uiState = uiState,
                snackbarHostState = snackbarHostState,
                scope = scope,
                onTitleChange = viewModel::onTitleChange,
                onDescriptionChange = viewModel::onDescriptionChange,
                onReminderIntervalChange = viewModel::onReminderIntervalChange,
                onTagSelected = viewModel::onTagSelected,
                onTagDeselected = viewModel::onTagDeselected,
                onCreateAndSelectTag = viewModel::createAndSelectTag
            )
        }

        if (uiState.showExitConfirmation) {
            ExitConfirmationDialog(
                onConfirm = {
                    viewModel.confirmExit()
                    navController.popBackStack()
                },
                onDismiss = viewModel::cancelExit
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun QuizSettingContent(
    modifier: Modifier = Modifier,
    uiState: QuizSettingUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onReminderIntervalChange: (Long?) -> Unit,
    onTagSelected: (Tag) -> Unit,
    onTagDeselected: (Tag) -> Unit,
    onCreateAndSelectTag: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = uiState.quiz?.title ?: "",
            onValueChange = onTitleChange,
            label = { Text(stringResource(R.string.quiz_title_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = uiState.quiz != null
        )

        ExpandableOutlinedTextField(
            value = uiState.quiz?.description ?: "",
            onValueChange = onDescriptionChange,
            label = { Text(stringResource(R.string.quiz_description_label)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.quiz != null
        )

        Divider()

        ReminderSettingsSection(
            currentInterval = uiState.quiz?.reminderInterval,
            onIntervalSelected = onReminderIntervalChange,
            enabled = uiState.quiz != null,
            snackbarHostState = snackbarHostState,
            scope = scope
        )

        Divider()

        TagsSettingsSection(
            allTags = uiState.allTags,
            selectedTags = uiState.selectedTags,
            onTagSelected = onTagSelected,
            onTagDeselected = onTagDeselected,
            onCreateAndSelectTag = onCreateAndSelectTag,
            enabled = uiState.quiz != null
        )
    }
}
