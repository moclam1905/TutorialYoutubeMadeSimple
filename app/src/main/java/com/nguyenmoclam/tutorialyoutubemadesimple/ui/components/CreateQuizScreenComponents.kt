package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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

            // Language selection
            Text(stringResource(R.string.select_language), fontWeight = FontWeight.Medium)

            Box {
                OutlinedTextField(
                    value = selectedLanguage,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { onShowLanguageDropdownChange(!showLanguageDropdown) }) {
                            Icon(
                                Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Select language"
                            )
                        }
                    }
                )

                DropdownMenu(
                    expanded = showLanguageDropdown,
                    onDismissRequest = { onShowLanguageDropdownChange(false) },
                    modifier = Modifier.fillMaxWidth(0.9f)
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
            // Question type selection
            Text(
                stringResource(R.string.question_type),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Question type options in a styled surface
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Multiple choice option
                    RadioOptionItem(
                        selected = questionType == "multiple-choice",
                        onClick = { onQuestionTypeChange("multiple-choice") },
                        label = stringResource(R.string.multiple_choice),
                        modifier = Modifier.weight(1f)
                    )

                    // Divider between options
                    Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                        Divider(
                            modifier = Modifier
                                .height(36.dp)
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        )
                    }

                    // True/False option
                    RadioOptionItem(
                        selected = questionType == "true-false",
                        onClick = { onQuestionTypeChange("true-false") },
                        label = stringResource(R.string.true_false),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Question count mode selection
            Text(
                stringResource(R.string.number_of_questions),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Auto mode option with enhanced styling
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                color = if (questionCountMode == "auto")
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = questionCountMode == "auto",
                            onClick = { onQuestionCountModeChange("auto") }
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = questionCountMode == "auto",
                        onClick = null
                    )
                    Text(
                        text = stringResource(R.string.auto_generate),
                        modifier = Modifier.padding(start = 8.dp),
                        fontWeight = if (questionCountMode == "auto") FontWeight.Medium else FontWeight.Normal
                    )
                }
            }

            // Level selection (only visible in auto mode)
            if (questionCountMode == "auto") {
                Spacer(modifier = Modifier.height(12.dp))

                Column(modifier = Modifier.padding(start = 32.dp)) {
                    Text(
                        stringResource(R.string.question_level),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Level options in a styled row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .selectableGroup()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Low level option
                        LevelRadioOption(
                            selected = questionLevel == "low",
                            onClick = { onQuestionLevelChange("low") },
                            title = stringResource(R.string.low),
                            subtitle = stringResource(R.string.five_questions),
                            modifier = Modifier.weight(1f)
                        )

                        // Medium level option
                        LevelRadioOption(
                            selected = questionLevel == "medium",
                            onClick = { onQuestionLevelChange("medium") },
                            title = stringResource(R.string.medium),
                            subtitle = stringResource(R.string.ten_questions),
                            modifier = Modifier.weight(1f)
                        )

                        // High level option
                        LevelRadioOption(
                            selected = questionLevel == "high",
                            onClick = { onQuestionLevelChange("high") },
                            title = stringResource(R.string.high),
                            subtitle = stringResource(R.string.fifteen_questions),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Manual mode option with enhanced styling
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                color = if (questionCountMode == "manual")
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = questionCountMode == "manual",
                            onClick = { onQuestionCountModeChange("manual") }
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = questionCountMode == "manual",
                        onClick = null
                    )
                    Text(
                        text = "Manual",
                        modifier = Modifier.padding(start = 8.dp),
                        fontWeight = if (questionCountMode == "manual") FontWeight.Medium else FontWeight.Normal
                    )
                }
            }

            // Manual count input (only visible in manual mode)
            if (questionCountMode == "manual") {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = manualQuestionCount,
                    onValueChange = { value ->
                        // Only allow numeric input
                        if (value.isEmpty() || value.all { it.isDigit() }) {
                            onManualQuestionCountChange(value)
                        }
                    },
                    label = { Text(stringResource(R.string.number_of_questions_range)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

/**
 * Reusable radio option item for question type selection
 */
@Composable
private fun RadioOptionItem(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .selectable(
                selected = selected,
                onClick = onClick
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = 8.dp),
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

/**
 * Reusable level radio option for difficulty selection
 */
@Composable
private fun LevelRadioOption(
    selected: Boolean,
    onClick: () -> Unit,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .selectable(
                selected = selected,
                onClick = onClick
            )
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Column(
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Text(
                text = title,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                fontSize = 14.sp
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
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
            Text(stringResource(R.string.select_output_content), fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(16.dp))

            // Summary toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.generate_summary))
                Switch(
                    checked = generateSummary,
                    onCheckedChange = onGenerateSummaryChange,
                    enabled = !isLoading
                )
            }

            // Questions toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.generate_questions))
                Switch(
                    checked = generateQuestions,
                    onCheckedChange = onGenerateQuestionsChange,
                    enabled = !isLoading
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
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

        // Custom step indicator with circles and connecting lines
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Create step circles with connecting lines
            for (i in 1..totalSteps) {
                val isActive = i <= currentStep
                val isCurrentStep = i == currentStep

                // Step indicator column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    // Step circle
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
                                color = MaterialTheme.colorScheme.primary,
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

                    // Step label
                    Spacer(modifier = Modifier.height(4.dp))
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
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Add connecting line between circles (except after the last one)
                if (i < totalSteps) {
                    Divider(
                        modifier = Modifier
                            .weight(0.7f)
                            .height(2.dp),
                        color = if (i < currentStep) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
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
    viewModel: QuizViewModel
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

        Button(
            onClick = onNext,
            enabled = !viewModel.isLoading &&
                    when (currentStep) {
                        1 -> viewModel.youtubeUrl.isNotBlank()
                        2 -> viewModel.questionCountMode != "manual" ||
                                (viewModel.manualQuestionCount.toIntOrNull() != null &&
                                        viewModel.manualQuestionCount.toIntOrNull()!! in 1..20)

                        3 -> viewModel.generateSummary || viewModel.generateQuestions
                        else -> false
                    }
        ) {
            Text(if (currentStep < 3) "Next" else "Create")
            if (currentStep < 3) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}
