package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nguyenmoclam.tutorialyoutubemadesimple.R

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