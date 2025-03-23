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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nguyenmoclam.tutorialyoutubemadesimple.R

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