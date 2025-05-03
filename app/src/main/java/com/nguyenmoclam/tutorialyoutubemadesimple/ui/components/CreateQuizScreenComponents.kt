package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizViewModel

/**
 * Content for Step 1: YouTube URL input and language selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step1Content(
    youtubeUrlValue: TextFieldValue,
    onYoutubeUrlChange: (TextFieldValue) -> Unit,
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    showLanguageDropdown: Boolean,
    onShowLanguageDropdownChange: (Boolean) -> Unit,
    languages: List<String>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // YouTube URL input
            OutlinedTextField(
                value = youtubeUrlValue,
                onValueChange = onYoutubeUrlChange,
                label = { Text(stringResource(R.string.enter_youtube_url)) },
                placeholder = { Text(stringResource(R.string.youtube_url_example)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (youtubeUrlValue.text.isNotEmpty()) {
                        IconButton(onClick = { onYoutubeUrlChange(TextFieldValue("")) }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear text")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Language selection using ExposedDropdownMenuBox
            ExposedDropdownMenuBox(
                expanded = showLanguageDropdown,
                onExpandedChange = { onShowLanguageDropdownChange(!showLanguageDropdown) },
                modifier = Modifier.fillMaxWidth() // Ensure it takes full width
            ) {
                OutlinedTextField(
                    value = selectedLanguage,
                    onValueChange = {}, // No change needed here
                    readOnly = true,
                    label = { Text(stringResource(R.string.select_language)) }, // Use label here
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLanguageDropdown) },
                    modifier = Modifier
                        .menuAnchor() // Important: Anchor for the menu
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = showLanguageDropdown,
                    onDismissRequest = { onShowLanguageDropdownChange(false) }
                    // Modifier.fillMaxWidth() might not be needed here, test it
                ) {
                    languages.forEach { language ->
                        DropdownMenuItem(
                            text = { Text(language) },
                            onClick = {
                                onLanguageSelected(language)
                                onShowLanguageDropdownChange(false)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Language description
            Text(
                text = stringResource(R.string.choose_language),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Content for Step 2: Quiz configuration (question type and count).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step2Content(
    questionType: String,
    onQuestionTypeChange: (String) -> Unit,
    questionCountMode: String,
    onQuestionCountModeChange: (String) -> Unit,
    questionLevel: String,
    onQuestionLevelChange: (String) -> Unit,
    manualQuestionCount: String,
    onManualQuestionCountChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Question type selection using SegmentedButton
            val questionTypes = listOf(
                stringResource(R.string.multiple_choice) to "multiple-choice",
                stringResource(R.string.true_false) to "true-false"
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                questionTypes.forEachIndexed { index, item ->
                    SegmentedButton(
                        selected = questionType == item.second,
                        onClick = { onQuestionTypeChange(item.second) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = questionTypes.size
                        )
                        // icon = { } // Optional icon
                    ) {
                        Text(item.first)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Question count mode selection Title
            Text(
                stringResource(R.string.number_of_questions),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- Auto Mode Selection ---
            Card( // Using Card for better visual grouping and click area
                onClick = { onQuestionCountModeChange("auto") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = if (questionCountMode == "auto") BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                colors = CardDefaults.cardColors(
                    containerColor = if (questionCountMode == "auto") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // .selectable is handled by Card onClick
                        .padding(horizontal = 16.dp, vertical = 12.dp), // Adjusted padding
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = questionCountMode == "auto",
                        onClick = null // Controlled by Card's onClick
                    )
                    Text(
                        text = stringResource(R.string.auto_generate),
                        modifier = Modifier.padding(start = 12.dp), // Increased spacing
                        style = MaterialTheme.typography.bodyLarge, // Consistent typography
                        fontWeight = if (questionCountMode == "auto") FontWeight.Medium else FontWeight.Normal
                    )
                }
            }

            // Level selection (Animated Visibility)
            AnimatedVisibility(visible = questionCountMode == "auto") {
                Column(modifier = Modifier.padding(start = 16.dp, top = 12.dp)) { // Adjusted padding
                    Text(
                        stringResource(R.string.question_level),
                        style = MaterialTheme.typography.titleSmall, // Use title small for subtitle
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 16.dp) // Indent title slightly
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Level options using SegmentedButton
                    val levels = listOf(
                        Triple(stringResource(R.string.low), stringResource(R.string.five_questions), "low"),
                        Triple(stringResource(R.string.medium), stringResource(R.string.ten_questions), "medium"),
                        Triple(stringResource(R.string.high), stringResource(R.string.fifteen_questions), "high")
                    )
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp) // Padding for the row itself
                    ) {
                        levels.forEachIndexed { index, item ->
                            SegmentedButton(
                                selected = questionLevel == item.third,
                                onClick = { onQuestionLevelChange(item.third) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = levels.size
                                ),
                                modifier = Modifier.height(64.dp)
                            ) {
                                // Column for Title and Subtitle within the button
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .fillMaxHeight()
                                ) {
                                    Text(item.first, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                    Text(item.second, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Manual Mode Selection ---
            Card( // Using Card for better visual grouping and click area
                onClick = { onQuestionCountModeChange("manual") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = if (questionCountMode == "manual") BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                colors = CardDefaults.cardColors(
                    containerColor = if (questionCountMode == "manual") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column { // Use Column to stack RadioButton Row and TextField
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            // .selectable handled by Card onClick
                            .padding(horizontal = 16.dp, vertical = 12.dp), // Adjusted padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = questionCountMode == "manual",
                            onClick = null // Controlled by Card's onClick
                        )
                        Text(
                            text = stringResource(R.string.manual), // Use string resource
                            modifier = Modifier.padding(start = 12.dp), // Increased spacing
                            style = MaterialTheme.typography.bodyLarge, // Consistent typography
                            fontWeight = if (questionCountMode == "manual") FontWeight.Medium else FontWeight.Normal
                        )
                    }

                    // Manual count input (Animated Visibility)
                    AnimatedVisibility(visible = questionCountMode == "manual") {
                        OutlinedTextField(
                            value = manualQuestionCount,
                            onValueChange = { value ->
                                // Only allow numeric input and limit length (e.g., max 2 digits for 1-20)
                                if (value.isEmpty() || (value.all { it.isDigit() } && value.length <= 2)) {
                                    onManualQuestionCountChange(value)
                                }
                            },
                            label = { Text(stringResource(R.string.number_of_questions_range)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp), // Padding within the Card
                            shape = RoundedCornerShape(8.dp),
                            isError = questionCountMode == "manual" && (manualQuestionCount.toIntOrNull() ?: 0) !in 1..20 && manualQuestionCount.isNotEmpty() // Basic validation feedback
                        )
                    }
                }
            }
        }
    }
}

/**
 * Content for Step 3: Output options (summary and/or questions).
 */
@Composable
fun Step3Content(
    generateSummary: Boolean,
    onGenerateSummaryChange: (Boolean) -> Unit,
    generateQuestions: Boolean,
    onGenerateQuestionsChange: (Boolean) -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.select_output_content), style = MaterialTheme.typography.titleMedium) // Use title style

            Spacer(modifier = Modifier.height(8.dp)) // Reduced spacer

            // Use ListItem for options
            ListItem(
                headlineContent = { Text(stringResource(R.string.generate_summary)) },
                leadingContent = { Icon(Icons.Outlined.Description, contentDescription = null) }, // Optional Icon
                trailingContent = {
                    Switch(
                        checked = generateSummary,
                        onCheckedChange = onGenerateSummaryChange,
                        enabled = !isLoading
                    )
                }
                // No modifier needed usually unless specific padding/background
            )
            Divider(modifier = Modifier.padding(horizontal = 16.dp)) // Add divider between items
            ListItem(
                headlineContent = { Text(stringResource(R.string.generate_questions)) },
                leadingContent = { Icon(Icons.Outlined.Quiz, contentDescription = null) }, // Optional Icon
                trailingContent = {
                    Switch(
                        checked = generateQuestions,
                        onCheckedChange = onGenerateQuestionsChange,
                        enabled = !isLoading
                    )
                }
            )
        }
    }
}

/**
 * Displays a step indicator showing the current progress in the quiz creation process.
 *
 * @param currentStep The current step number
 * @param totalSteps The total number of steps
 */
@Composable
fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Step title with improved styling
        Text(
            text = when (currentStep) {
                1 -> stringResource(R.string.step_1_enter_video)
                2 -> stringResource(R.string.step_2_configure_quiz)
                3 -> stringResource(R.string.step_3_select_output)
                else -> ""
            },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Row for Circles and Dividers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp), // Adjusted padding
            horizontalArrangement = Arrangement.SpaceBetween, // Distribute items
            verticalAlignment = Alignment.CenterVertically // Center vertically
        ) {
            for (i in 1..totalSteps) {
                val isActive = i <= currentStep
                val isCurrentStep = i == currentStep

                // Step circle (No weight needed with SpaceBetween)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = if (isCurrentStep) 2.dp else 0.dp,
                            color = if (isCurrentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.0f
                            ), // Transparent border if not current
                            shape = CircleShape
                        )
                ) {
                    Text(
                        text = "$i",
                        color = if (isActive) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // Connecting line (Use weight to fill space)
                if (i < totalSteps) {
                    Divider(
                        modifier = Modifier
                            .weight(1f) // Weight fills the space
                            .height(2.dp),
                        color = if (i < currentStep) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // Row for Labels (Separate Row below)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp) // Match horizontal padding
                .padding(top = 4.dp), // Space below circles
            horizontalArrangement = Arrangement.SpaceBetween // Match arrangement
        ) {
            for (i in 1..totalSteps) {
                val isActive = i <= currentStep
                val isCurrentStep = i == currentStep
                // Step label (Aligned under circle using weight)
                Text(
                    text = when (i) {
                        1 -> stringResource(R.string.enter_video)
                        2 -> stringResource(R.string.configure)
                        3 -> stringResource(R.string.output)
                        else -> ""
                    },
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = if (isCurrentStep) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .weight(1f) // Weight aligns proportionally with Box above
                        .padding(top = 2.dp)
                )
                // Add Spacer to align with Divider space
                if (i < totalSteps) {
                    Spacer(Modifier.weight(1f)) // Spacer takes up the divider's weighted space
                }
            }
        }

        // Progress percentage text
        Text(
            text = "${(currentStep * 100) / totalSteps}%",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            textAlign = TextAlign.End
        )
    }
}

/**
 * Navigation buttons for the quiz creation process.
 */
@Composable
fun NavigationButtons(
    currentStep: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    viewModel: QuizViewModel,
    isSettingsLoaded: Boolean,
    isNextEnabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (currentStep > 1) {
            Button(onClick = onBack, enabled = !viewModel.isLoading) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Back")
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        // Combine the internal validation logic with the external isNextEnabled flag
        val finalIsNextEnabled = isNextEnabled && // External flag (e.g., !creationState.isLoading)
                when (currentStep) {
                    1 -> viewModel.youtubeUrl.isNotBlank()
                    2 -> viewModel.questionCountMode != "manual" ||
                            (viewModel.manualQuestionCount.toIntOrNull() != null &&
                                    viewModel.manualQuestionCount.toIntOrNull()!! in 1..20)

                    3 -> (viewModel.generateSummary || viewModel.generateQuestions) && isSettingsLoaded
                    else -> false
                }

        Button(
            onClick = onNext,
            enabled = finalIsNextEnabled // Use the combined enabled state
        ) {
            Text(if (currentStep < 3) "Next" else "Create")
            if (currentStep < 3) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

/**
 * A dialog to display error messages.
 *
 * @param errorMessage The error message to display.
 * @param onDismiss Lambda function to be called when the dialog is dismissed.
 */
@Composable
fun ErrorDialog(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.ErrorOutline, contentDescription = "Error Icon") },
        title = { Text(stringResource(R.string.error_occurred)) },
        text = { Text(errorMessage) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.ok))
            }
        }
    )
}
