package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.MultipleChoiceQuestion
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.TrueFalseQuestion
import java.util.concurrent.TimeUnit

// Define the QuestionStatus enum
enum class QuestionStatus {
    CORRECT, INCORRECT, SKIPPED
}

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
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.quiz_results_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.quiz_results_correct),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = stringResource(R.string.quiz_results_correct),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "$correctAnswers",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.quiz_results_incorrect),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         Icon(
                            imageVector = Icons.Filled.Cancel,
                            contentDescription = stringResource(R.string.quiz_results_incorrect),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "$incorrectAnswers",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.quiz_results_skipped),
                        style = MaterialTheme.typography.bodyMedium
                    )
                     Row(verticalAlignment = Alignment.CenterVertically) {
                         Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = stringResource(R.string.quiz_results_skipped),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "$skippedQuestions",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
 * Component displaying a list of questions based on their status.
 * It now receives status explicitly and answers maps.
 * Assumes parent handles scrolling.
 */
@Composable
fun QuestionStatusList(
    title: String,
    questionIndices: List<Int>,
    quizQuestions: List<Any>,
    status: QuestionStatus,
    userAnswers: Map<Int, String> = emptyMap(),
    correctAnswers: Map<Int, String> = emptyMap()
) {
    if (questionIndices.isNotEmpty()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, top = 16.dp),
            textAlign = TextAlign.Start
        )

        val statusColor = when (status) {
            QuestionStatus.CORRECT -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            QuestionStatus.INCORRECT -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            QuestionStatus.SKIPPED -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            questionIndices.forEach { originalIndex ->
                val question = quizQuestions[originalIndex]
                val questionText = when (question) {
                    is MultipleChoiceQuestion -> question.question
                    is TrueFalseQuestion -> question.statement
                    else -> stringResource(R.string.question_number, originalIndex + 1)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = statusColor)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(
                            text = "${originalIndex + 1}. $questionText",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (status == QuestionStatus.INCORRECT) {
                            val userAnswerText = userAnswers[originalIndex] ?: "N/A"
                            val correctAnswerText = correctAnswers[originalIndex] ?: "N/A"

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${stringResource(id = R.string.quiz_your_answer)}: $userAnswerText",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                             Text(
                                text = "${stringResource(id = R.string.quiz_correct_answer)}: $correctAnswerText",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}