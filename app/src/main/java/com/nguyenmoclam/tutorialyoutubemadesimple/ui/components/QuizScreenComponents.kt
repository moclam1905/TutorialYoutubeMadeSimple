package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

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

// Using the refactored ExitConfirmationDialog component from components package

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
        QuizResultsSummaryCard(
            correctAnswers = correctAnswers,
            incorrectAnswers = incorrectAnswers,
            skippedQuestions = skippedQuestions,
            completionTimeSeconds = completionTimeSeconds
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Correct answers section
        QuestionStatusList(
            title = "Các câu hỏi đã trả lời đúng",
            questionIndices = correctQuestionIndices,
            quizQuestions = quizQuestions
        )
        
        // Incorrect answers section
        QuestionStatusList(
            title = "Các câu hỏi đã trả lời sai",
            questionIndices = incorrectQuestionIndices,
            quizQuestions = quizQuestions
        )
        
        // Skipped questions section
        QuestionStatusList(
            title = "Các câu hỏi đã bỏ qua",
            questionIndices = skippedQuestionIndices,
            quizQuestions = quizQuestions
        )
        
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