package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

// Import R explicitly
import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.tag.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizSettingTopAppBar(
    onSaveClick: () -> Unit,
    onResetClick: () -> Unit,
    canSave: Boolean,
    canReset: Boolean
) {
    TopAppBar(
        title = { Text(stringResource(R.string.quiz_settings_title)) },
        actions = {
            IconButton(onClick = onResetClick, enabled = canReset) {
                Icon(
                    Icons.Default.Restore,
                    contentDescription = stringResource(R.string.reset_settings),
                    tint = if (canReset) LocalContentColor.current else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.38f
                    )
                )
            }
            IconButton(onClick = onSaveClick, enabled = canSave) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = stringResource(R.string.save_settings),
                    tint = if (canSave) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.38f
                    )
                )
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingsSection(
    currentInterval: Long?,
    onIntervalSelected: (Long?) -> Unit,
    enabled: Boolean,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var showRationaleDialog by remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // Show snackbar if permission is denied after request
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.permission_denied_snackbar),
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // Function to check and request permission
    val checkAndRequestNotificationPermission: () -> Unit = {
        // Find the activity from the context
        var currentContext = context
        var activity = currentContext as? Activity
        // Loop to find the underlying Activity if the context is a wrapper
        // Simplified loop condition to address lint warning
        while (currentContext is android.content.ContextWrapper) {
            activity = currentContext as? Activity
            if (activity != null) break // Found the activity
            currentContext = currentContext.baseContext
        }

        if (activity != null) { // Check if activity was found
            when {
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted, proceed immediately
                    // We call onIntervalSelected in the onClick lambda now
                    // onIntervalSelected(currentInterval)
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, // Use the found activity
                    Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    // Show rationale dialog before requesting again
                    showRationaleDialog = true
                }

                else -> {
                    // Directly request the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Handle case where Activity context couldn't be found (e.g., preview)
            // Optionally show a different snackbar or log an error
            scope.launch {
                snackbarHostState.showSnackbar("Could not find Activity to request permission.")
            }
        }
    }

    // Rationale Dialog
    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = { Text(stringResource(R.string.permission_rationale_title)) },
            text = { Text(stringResource(R.string.permission_rationale_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRationaleDialog = false
                        // Request permission again after showing rationale
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                ) {
                    Text(stringResource(R.string.permission_rationale_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationaleDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    val reminderOptions = remember {
        mapOf(
            null to context.getString(R.string.reminder_off),
            86400000L to context.getString(R.string.reminder_daily),
            604800000L to context.getString(R.string.reminder_weekly)
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
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.reminder_frequency_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
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
                            // Always update the state first
                            onIntervalSelected(interval)
                            expanded = false
                            // Then, if enabling reminder, check/request permission
                            if (interval != null && interval > 0) {
                                checkAndRequestNotificationPermission()
                            }
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val canAddTag = newTagText.isNotBlank() && enabled

    Column {
        Text(
            text = stringResource(R.string.tags_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = newTagText,
            onValueChange = { newTagText = it },
            label = { Text(stringResource(R.string.add_new_tag_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = enabled,
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (canAddTag) {
                            onCreateAndSelectTag(newTagText.trim())
                            newTagText = ""
                        }
                    },
                    enabled = canAddTag
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_tag_button)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            stringResource(R.string.selected_tags_label),
            style = MaterialTheme.typography.titleSmall
        )
        FlowRow(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)) {
            if (selectedTags.isEmpty()) {
                Text(
                    stringResource(R.string.no_tags_selected),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                selectedTags.sortedBy { it.name }.forEach { tag ->
                    InputChip(
                        selected = true,
                        onClick = { if (enabled) onTagDeselected(tag) },
                        label = { Text(tag.name) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(
                                    R.string.deselect_tag_desc,
                                    tag.name
                                ),
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

        Text(
            stringResource(R.string.available_tags_label),
            style = MaterialTheme.typography.titleSmall
        )
        FlowRow(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)) {
            val availableTags = allTags.filter { it !in selectedTags }.sortedBy { it.name }
            if (availableTags.isEmpty()) {
                Text(
                    stringResource(R.string.no_available_tags),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                availableTags.forEach { tag ->
                    FilterChip(
                        selected = false,
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
