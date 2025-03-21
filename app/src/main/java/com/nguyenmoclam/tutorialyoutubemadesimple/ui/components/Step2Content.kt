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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nguyenmoclam.tutorialyoutubemadesimple.R

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
    val context = LocalContext.current
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
                context.getString(R.string.question_type),
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
                        label = context.getString(R.string.multiple_choice),
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
                        label = context.getString(R.string.true_false),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Question count mode selection
            Text(
                context.getString(R.string.number_of_questions),
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
                        text = context.getString(R.string.auto_generate),
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
                        context.getString(R.string.question_level),
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
                            title = context.getString(R.string.low),
                            subtitle = context.getString(R.string.five_questions),
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Medium level option
                        LevelRadioOption(
                            selected = questionLevel == "medium",
                            onClick = { onQuestionLevelChange("medium") },
                            title = context.getString(R.string.medium),
                            subtitle = context.getString(R.string.ten_questions),
                            modifier = Modifier.weight(1f)
                        )
                        
                        // High level option
                        LevelRadioOption(
                            selected = questionLevel == "high",
                            onClick = { onQuestionLevelChange("high") },
                            title = context.getString(R.string.high),
                            subtitle = context.getString(R.string.fifteen_questions),
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
                    label = { Text(context.getString(R.string.number_of_questions_range)) },
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