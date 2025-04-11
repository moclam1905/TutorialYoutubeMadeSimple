package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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

// Existing components from the file
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
                text = stringResource(R.string.quiz_ready_to_start),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.quiz_question_count, questionCount),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.quiz_time_tracking_info),
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
                Text(stringResource(R.string.start_quiz_button))
            }
        }
    }
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
            text = stringResource(R.string.quiz_complete_message),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Results summary (Assuming QuizResultsSummaryCard is defined elsewhere or needs to be moved too)
        QuizResultsSummaryCard(
            correctAnswers = correctAnswers,
            incorrectAnswers = incorrectAnswers,
            skippedQuestions = skippedQuestions,
            completionTimeSeconds = completionTimeSeconds
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Correct answers section (Assuming QuestionStatusList is defined elsewhere or needs to be moved too)
        QuestionStatusList(
            title = stringResource(R.string.quiz_correct_questions),
            questionIndices = correctQuestionIndices,
            quizQuestions = quizQuestions
        )

        // Incorrect answers section
        QuestionStatusList(
            title = stringResource(R.string.quiz_incorrect_questions),
            questionIndices = incorrectQuestionIndices,
            quizQuestions = quizQuestions
        )

        // Skipped questions section
        QuestionStatusList(
            title = stringResource(R.string.quiz_skipped_questions),
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
            Text(stringResource(R.string.retry_quiz_button))
        }
    }
}

// --- Components moved from QuizDetailScreen ---

/**
 * Displays the header for a quiz question (e.g., "Question 1 of 10").
 */
@Composable
fun QuestionHeader(currentQuestionIndex: Int, totalQuestions: Int) {
    Text(
        text = stringResource(
            R.string.question_index,
            currentQuestionIndex + 1,
            totalQuestions
        ),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

/**
 * Displays a single option item for a Multiple Choice Question.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultipleChoiceOptionItem(
    optionKey: String,
    optionValue: String,
    isSelected: Boolean,
    showFeedback: Boolean,
    isCorrect: Boolean, // Is this specific option the correct one?
    isAnsweredCorrectly: Boolean, // Was the selected answer correct?
    enabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        !showFeedback -> if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        isSelected && isAnsweredCorrectly -> Color(0xFFDCEDC8) // Light Green for correct selection
        isSelected && !isAnsweredCorrectly -> Color(0xFFFFCDD2) // Light Red for incorrect selection
        isCorrect -> Color(0xFFDCEDC8).copy(alpha = 0.5f) // Dim Green for correct answer not selected
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f) // Dim default for others
    }
    val elevation = if (isSelected && !showFeedback) 4.dp else 1.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = optionKey,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(12.dp))
            val textColor =
                if (showFeedback && ((isSelected && isAnsweredCorrectly) || isCorrect || isSelected)) {
                    Color.Black // Use black text on green or red background for better contrast
                } else {
                    LocalContentColor.current // Use default text color otherwise
                }
            Text(
                text = optionValue,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )

            // Feedback Icons / Radio Button
            if (showFeedback) {
                if (isCorrect) { // Show check if this is a correct answer
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.correct_answer),
                        tint = Color.Green.copy(alpha = 0.8f) // Slightly darker green
                    )
                } else if (isSelected) { // Show close if this was selected but incorrect
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.incorrect_answer),
                        tint = Color.Red.copy(alpha = 0.8f) // Slightly darker red
                    )
                }
                // No icon otherwise
            } else {
                // Show RadioButton only when feedback is not shown
                RadioButton(
                    selected = isSelected,
                    onClick = null,
                    enabled = false
                ) // Disable direct click on radio
            }
        }
    }
}


/**
 * Displays the body content for a Multiple Choice Question.
 */
@Composable
fun MultipleChoiceQuestionBody(
    question: MultipleChoiceQuestion,
    selectedAnswer: String,
    showFeedback: Boolean,
    isCorrect: Boolean,
    onAnswerSelected: (String) -> Unit
) {
    Text(
        text = question.question,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(24.dp))

    question.options.forEach { (key, value) ->
        MultipleChoiceOptionItem(
            optionKey = key,
            optionValue = value,
            isSelected = selectedAnswer == key,
            showFeedback = showFeedback,
            isCorrect = question.correctAnswers.contains(key),
            isAnsweredCorrectly = isCorrect,
            enabled = !showFeedback,
            onClick = { onAnswerSelected(key) }
        )
    }
}

/**
 * Displays a single option item (True or False) for a True/False Question.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrueFalseOptionItem(
    text: String,
    value: String, // "True" or "False"
    isSelected: Boolean,
    showFeedback: Boolean,
    isCorrectOption: Boolean, // Is this option (True/False) the correct answer?
    isAnsweredCorrectly: Boolean, // Was the selected answer correct?
    enabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        !showFeedback -> if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        isSelected && isAnsweredCorrectly -> Color(0xFFDCEDC8) // Light Green
        isSelected && !isAnsweredCorrectly -> Color(0xFFFFCDD2) // Light Red
        isCorrectOption -> Color(0xFFDCEDC8).copy(alpha = 0.5f) // Dim Green
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f) // Dim default
    }
    val elevation = if (isSelected && !showFeedback) 4.dp else 1.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val textColor =
                if (showFeedback && ((isSelected && isAnsweredCorrectly) || isCorrectOption || isSelected)) {
                    Color.Black // Use black text on green or red background
                } else {
                    LocalContentColor.current // Use default text color
                }
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )

            if (showFeedback) {
                if (isCorrectOption) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.correct_answer),
                        tint = Color.Green.copy(alpha = 0.8f)
                    )
                } else if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.incorrect_answer),
                        tint = Color.Red.copy(alpha = 0.8f)
                    )
                }
            } else {
                RadioButton(selected = isSelected, onClick = null, enabled = false)
            }
        }
    }
}

/**
 * Displays the body content for a True/False Question.
 */
@Composable
fun TrueFalseQuestionBody(
    question: TrueFalseQuestion,
    selectedAnswer: String, // "True" or "False"
    showFeedback: Boolean,
    isCorrect: Boolean,
    onAnswerSelected: (String) -> Unit
) {
    Text(
        text = question.statement,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(24.dp))

    // True Option
    TrueFalseOptionItem(
        text = stringResource(R.string.true_txt),
        value = "True",
        isSelected = selectedAnswer == "True",
        showFeedback = showFeedback,
        isCorrectOption = question.isTrue,
        isAnsweredCorrectly = isCorrect,
        enabled = !showFeedback,
        onClick = { onAnswerSelected("True") }
    )

    // False Option
    TrueFalseOptionItem(
        text = stringResource(R.string.false_txt),
        value = "False",
        isSelected = selectedAnswer == "False",
        showFeedback = showFeedback,
        isCorrectOption = !question.isTrue,
        isAnsweredCorrectly = isCorrect,
        enabled = !showFeedback,
        onClick = { onAnswerSelected("False") }
    )
}

/**
 * Displays the navigation buttons (Submit/Next, Skip) for the quiz.
 */
@Composable
fun QuizNavigationButtons(
    showFeedback: Boolean,
    isLastQuestion: Boolean,
    selectedAnswerNotEmpty: Boolean,
    onSubmitAnswer: () -> Unit,
    onSkipQuestion: () -> Unit,
    onNextQuestion: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (showFeedback) {
            // Show "Next Question" or potentially "Finish Quiz" if it's the last question
            Button(
                onClick = onNextQuestion,
                modifier = Modifier.fillMaxWidth(),
                // enabled = !isLastQuestion // Enable even on last question to trigger completion check
            ) {
                Text(
                    if (isLastQuestion) stringResource(R.string.finish_quiz_button) else stringResource(
                        R.string.next_question_button
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = stringResource(R.string.next))
            }
        } else {
            // Show "Skip" and "Submit"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSkipQuestion,
                    modifier = Modifier.weight(1f),
                    enabled = !isLastQuestion // Disable skip on the last question? Or allow? Let's allow.
                ) {
                    Text(stringResource(R.string.skip_question_button))
                }
                Button(
                    onClick = onSubmitAnswer,
                    modifier = Modifier.weight(1f),
                    enabled = selectedAnswerNotEmpty
                ) {
                    Text(stringResource(R.string.submit_answer_button))
                }
            }
        }
    }
}

/**
 * Displays the main content of a single quiz question (MCQ or T/F).
 */
@Composable
fun QuizContent(
    quizQuestions: List<Any>,
    currentQuestionIndex: Int,
    selectedAnswer: String,
    onAnswerSelected: (String) -> Unit,
    showFeedback: Boolean,
    isCorrect: Boolean,
    onSubmitAnswer: () -> Unit,
    onSkipQuestion: () -> Unit,
    onNextQuestion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) // Enable scrolling for long questions/options
    ) {
        QuestionHeader(currentQuestionIndex, quizQuestions.size)
        Spacer(modifier = Modifier.height(16.dp))

        val currentQuestion = quizQuestions[currentQuestionIndex]
        when (currentQuestion) {
            is MultipleChoiceQuestion -> MultipleChoiceQuestionBody(
                question = currentQuestion,
                selectedAnswer = selectedAnswer,
                showFeedback = showFeedback,
                isCorrect = isCorrect,
                onAnswerSelected = onAnswerSelected
            )

            is TrueFalseQuestion -> TrueFalseQuestionBody(
                question = currentQuestion,
                selectedAnswer = selectedAnswer,
                showFeedback = showFeedback,
                isCorrect = isCorrect,
                onAnswerSelected = onAnswerSelected
            )
            // Add cases for other question types if necessary
        }

        Spacer(modifier = Modifier.height(32.dp)) // Space before buttons

        QuizNavigationButtons(
            showFeedback = showFeedback,
            isLastQuestion = currentQuestionIndex == quizQuestions.size - 1,
            selectedAnswerNotEmpty = selectedAnswer.isNotEmpty(),
            onSubmitAnswer = onSubmitAnswer,
            onSkipQuestion = onSkipQuestion,
            onNextQuestion = onNextQuestion
        )

        Spacer(modifier = Modifier.height(16.dp)) // Padding at the bottom
    }
}
