package com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.tag.Tag
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.ExitConfirmationDialog
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.LoadingState
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizSettingUiState
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizSettingViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuizSettingScreen(
    navController: NavController,
    viewModel: QuizSettingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

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
                navController = navController,
                onSaveClick = viewModel::saveSettings,
                onResetClick = viewModel::resetSettings,
                canSave = uiState.hasUnsavedChanges && !uiState.isLoading,
                canReset = uiState.hasUnsavedChanges && !uiState.isLoading
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.quiz == null) { // Show loading only initially
            // Pass 0f for progress when indeterminate or no specific progress available
            LoadingState(progress = 0f, message = stringResource(R.string.loading_settings))
        } else if (uiState.quiz == null && !uiState.isLoading) {
            // Handle case where quiz is null after loading (e.g., not found)
             Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                 Text(uiState.errorMessage ?: stringResource(R.string.quiz_not_found_error))
             }
        } else {
            // Quiz data is available (even if still loading tags initially)
            QuizSettingContent(
                modifier = Modifier.padding(paddingValues),
                uiState = uiState,
                onTitleChange = viewModel::onTitleChange,
                onDescriptionChange = viewModel::onDescriptionChange,
                onReminderIntervalChange = viewModel::onReminderIntervalChange,
                onTagSelected = viewModel::onTagSelected,
                onTagDeselected = viewModel::onTagDeselected,
                onCreateAndSelectTag = viewModel::createAndSelectTag
            )
        }

        // Show exit confirmation dialog
        if (uiState.showExitConfirmation) {
            ExitConfirmationDialog(
                onConfirm = {
                    viewModel.confirmExit()
                    navController.popBackStack() // Navigate back after confirmation
                },
                onDismiss = viewModel::cancelExit
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizSettingTopAppBar(
    navController: NavController,
    onSaveClick: () -> Unit,
    onResetClick: () -> Unit,
    canSave: Boolean,
    canReset: Boolean
) {
    TopAppBar(
        title = { Text(stringResource(R.string.quiz_settings_title)) },
        // Removed navigationIcon as requested
        actions = {
            // Reset Button
            IconButton(onClick = onResetClick, enabled = canReset) {
                Icon(
                    Icons.Default.Restore,
                    contentDescription = stringResource(R.string.reset_settings),
                    tint = if (canReset) LocalContentColor.current else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
            // Save Button
            IconButton(onClick = onSaveClick, enabled = canSave) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = stringResource(R.string.save_settings),
                    tint = if (canSave) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }
    )
}

@Composable
fun QuizSettingContent(
    modifier: Modifier = Modifier,
    uiState: QuizSettingUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onReminderIntervalChange: (Long?) -> Unit,
    onTagSelected: (Tag) -> Unit,
    onTagDeselected: (Tag) -> Unit,
    onCreateAndSelectTag: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Field
        OutlinedTextField(
            value = uiState.quiz?.title ?: "",
            onValueChange = onTitleChange,
            label = { Text(stringResource(R.string.quiz_title_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = uiState.quiz != null
        )

        // Description Field
        OutlinedTextField(
            value = uiState.quiz?.description ?: "",
            onValueChange = onDescriptionChange,
            label = { Text(stringResource(R.string.quiz_description_label)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            enabled = uiState.quiz != null
        )

        Divider()

        // Reminder Section
        ReminderSettingsSection(
            currentInterval = uiState.quiz?.reminderInterval,
            onIntervalSelected = onReminderIntervalChange,
             enabled = uiState.quiz != null
        )

        Divider()

        // Tags Section
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingsSection(
    currentInterval: Long?,
    onIntervalSelected: (Long?) -> Unit,
    enabled: Boolean
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    // Permission Launcher (for Android 13+)
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // Optional: Show a snackbar or message explaining why the permission is needed
            // For now, the reminder just won't work without permission.
        }
        // Re-evaluate reminder state if needed, though ViewModel handles scheduling logic
    }

    // Function to check and request permission
    val checkAndRequestNotificationPermission: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                // Optional: Check if rationale should be shown
                // ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.POST_NOTIFICATIONS) -> { ... }
                else -> {
                    // Directly ask for the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    // Define reminder options (value in milliseconds, label)
    val reminderOptions = remember {
        mapOf(
            null to context.getString(R.string.reminder_off), // Use null for "Off"
            86400000L to context.getString(R.string.reminder_daily), // 24 * 60 * 60 * 1000
            604800000L to context.getString(R.string.reminder_weekly) // 7 * 24 * 60 * 60 * 1000
        )
    }
    val currentLabel = reminderOptions[currentInterval] ?: reminderOptions[null]!!

    Column {
        Text(
            text = stringResource(R.string.reminder_notification_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = currentLabel,
                onValueChange = {}, // Read-only
                readOnly = true,
                label = { Text(stringResource(R.string.reminder_frequency_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(), // Important for dropdown anchor
                enabled = enabled,
                 colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                reminderOptions.forEach { (interval, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            // Check/Request permission only if enabling reminder
                            if (interval != null && interval > 0) {
                                checkAndRequestNotificationPermission()
                            }
                            onIntervalSelected(interval)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class) // Keep OptIn here
@Composable
fun TagsSettingsSection(
    allTags: List<Tag>,
    selectedTags: List<Tag>,
    onTagSelected: (Tag) -> Unit,
    onTagDeselected: (Tag) -> Unit,
    onCreateAndSelectTag: (String) -> Unit,
    enabled: Boolean
) {
    var newTagText by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column {
        Text(
            text = stringResource(R.string.tags_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Input for creating new tags
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newTagText,
                onValueChange = { newTagText = it },
                label = { Text(stringResource(R.string.add_new_tag_label)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                enabled = enabled
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (newTagText.isNotBlank()) {
                        onCreateAndSelectTag(newTagText.trim())
                        newTagText = "" // Clear input after adding
                    }
                },
                enabled = enabled && newTagText.isNotBlank()
            ) {
                Text(stringResource(R.string.add_tag_button))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display selected tags
        Text(stringResource(R.string.selected_tags_label), style = MaterialTheme.typography.titleSmall)
        FlowRow(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            if (selectedTags.isEmpty()) {
                 Text(stringResource(R.string.no_tags_selected), style = MaterialTheme.typography.bodyMedium)
            } else {
                selectedTags.sortedBy { it.name }.forEach { tag ->
                    InputChip(
                        selected = true, // Always selected in this context
                        onClick = { if (enabled) onTagDeselected(tag) },
                        label = { Text(tag.name) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.deselect_tag_desc, tag.name),
                                modifier = Modifier.size(InputChipDefaults.IconSize)
                            )
                        },
                        modifier = Modifier.padding(end = 4.dp, bottom = 4.dp),
                        enabled = enabled
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display available tags to select
        Text(stringResource(R.string.available_tags_label), style = MaterialTheme.typography.titleSmall)
         FlowRow(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            val availableTags = allTags.filter { it !in selectedTags }.sortedBy { it.name }
             if (availableTags.isEmpty()) {
                 Text(stringResource(R.string.no_available_tags), style = MaterialTheme.typography.bodyMedium)
             } else {
                 availableTags.forEach { tag ->
                     FilterChip(
                         selected = false, // Not selected in this context
                         onClick = { if (enabled) onTagSelected(tag) },
                         label = { Text(tag.name) },
                         modifier = Modifier.padding(end = 4.dp, bottom = 4.dp),
                         enabled = enabled
                     )
                 }
             }
        }
    }
}
