package com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.MultipleChoiceQuestion
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.TrueFalseQuestion
import java.util.concurrent.TimeUnit

/**
 * Screen shown when a quiz is ready to start but hasn't been started yet
 */
@Composable
fun StartQuizScreen(
    questionCount: Int,
    onStartQuiz: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Ready to Start Quiz",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "This quiz contains $questionCount questions.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Your time will be tracked from when you start until you complete all questions.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onStartQuiz,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Text("Start Quiz")
            }
        }
    }
}

/**
 * Dialog shown when user attempts to exit the quiz before completion
 */
@Composable
fun ExitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exit Quiz?") },
        text = { Text("If you exit now, your progress will be saved but your timer will be reset when you return.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Exit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Continue Quiz")
            }
        }
    )
}

/**
 * Screen shown when all quiz questions have been answered or skipped
 */
@SuppressLint("DefaultLocale")
@Composable
fun QuizResultsScreen(
    quizQuestions: List<Any>,
    correctAnswers: Int,
    incorrectAnswers: Int,
    skippedQuestions: Int,
    completionTimeSeconds: Int,
    correctQuestionIndices: List<Int>,
    incorrectQuestionIndices: List<Int>,
    skippedQuestionIndices: List<Int>,
    onRetryQuiz: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Complete the quiz to see your results.",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Results summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Quiz Results",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Correct",
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
                            text = "Incorrect",
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
                            text = "Skipped",
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
                    text = "Completion Time: $timeFormatted",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Correct answers section
        if (correctQuestionIndices.isNotEmpty()) {
            Text(
                text = "Các câu hỏi đã trả lời đúng",
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
                items(correctQuestionIndices) { index ->
                    val question = quizQuestions[index]
                    val questionText = when (question) {
                        is MultipleChoiceQuestion -> question.question
                        is TrueFalseQuestion -> question.statement
                        else -> "Question ${index + 1}"
                    }
                    
                    Text(
                        text = "${index + 1}. $questionText",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    if (index < correctQuestionIndices.lastIndex) {
                        Divider()
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Incorrect answers section
        if (incorrectQuestionIndices.isNotEmpty()) {
            Text(
                text = "Các câu hỏi đã trả lời sai",
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
                items(incorrectQuestionIndices) { index ->
                    val question = quizQuestions[index]
                    val questionText = when (question) {
                        is MultipleChoiceQuestion -> question.question
                        is TrueFalseQuestion -> question.statement
                        else -> "Question ${index + 1}"
                    }
                    
                    Text(
                        text = "${index + 1}. $questionText",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    if (index < incorrectQuestionIndices.lastIndex) {
                        Divider()
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Skipped questions section
        if (skippedQuestionIndices.isNotEmpty()) {
            Text(
                text = "Các câu hỏi đã bỏ qua",
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
                items(skippedQuestionIndices) { index ->
                    val question = quizQuestions[index]
                    val questionText = when (question) {
                        is MultipleChoiceQuestion -> question.question
                        is TrueFalseQuestion -> question.statement
                        else -> "Question ${index + 1}"
                    }
                    
                    Text(
                        text = "${index + 1}. $questionText",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    if (index < skippedQuestionIndices.lastIndex) {
                        Divider()
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Retry button
        Button(
            onClick = onRetryQuiz,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Retry")
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            Text("Retry Quiz")
        }
    }
}