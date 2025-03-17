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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
            Text("Select Output Content:", fontWeight = FontWeight.Medium)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Summary toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Generate Summary")
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
                Text("Generate Questions")
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