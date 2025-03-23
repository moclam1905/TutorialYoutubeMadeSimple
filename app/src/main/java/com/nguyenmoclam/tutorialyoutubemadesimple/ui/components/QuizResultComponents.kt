package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.MultipleChoiceQuestion
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.TrueFalseQuestion
import java.util.concurrent.TimeUnit

/**
 * Card displaying quiz results summary
 */
@SuppressLint("DefaultLocale")
@Composable
fun QuizResultsSummaryCard(
    correctAnswers: Int,
    incorrectAnswers: Int,
    skippedQuestions: Int,
    completionTimeSeconds: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.quiz_results_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.quiz_results_correct),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$correctAnswers",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.quiz_results_incorrect),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$incorrectAnswers",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.quiz_results_skipped),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$skippedQuestions",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Format completion time
            val hours = TimeUnit.SECONDS.toHours(completionTimeSeconds.toLong())
            val minutes = TimeUnit.SECONDS.toMinutes(completionTimeSeconds.toLong()) % 60
            val seconds = completionTimeSeconds % 60
            val timeFormatted = if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }

            Text(
                text = stringResource(R.string.quiz_completion_time, timeFormatted),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Component displaying a list of questions with their status (correct, incorrect, or skipped)
 */
@Composable
fun QuestionStatusList(
    title: String,
    questionIndices: List<Int>,
    quizQuestions: List<Any>
) {
    if (questionIndices.isNotEmpty()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            items(questionIndices) { index ->
                val question = quizQuestions[index]
                val questionText = when (question) {
                    is MultipleChoiceQuestion -> question.question
                    is TrueFalseQuestion -> question.statement
                    else -> stringResource(R.string.question_number, index + 1)
                }

                Text(
                    text = "${index + 1}. $questionText",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                if (index < questionIndices.lastIndex) {
                    Divider()
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}