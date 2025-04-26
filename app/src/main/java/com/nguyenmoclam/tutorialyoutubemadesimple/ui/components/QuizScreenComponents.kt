package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.MultipleChoiceQuestion
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.TrueFalseQuestion

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
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .verticalScroll(rememberScrollState()),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Quiz,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.quiz_ready_to_start),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.quiz_question_count, questionCount),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.quiz_time_tracking_info),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                FilledTonalButton(
                    onClick = onStartQuiz,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.start_quiz_button), style = MaterialTheme.typography.labelLarge)
                }
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
    userAnswers: Map<Int, String>,
    correctAnswersMap: Map<Int, String>,
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
            quizQuestions = quizQuestions,
            status = QuestionStatus.CORRECT
        )

        // Incorrect answers section
        QuestionStatusList(
            title = stringResource(R.string.quiz_incorrect_questions),
            questionIndices = incorrectQuestionIndices,
            quizQuestions = quizQuestions,
            status = QuestionStatus.INCORRECT,
            userAnswers = userAnswers,
            correctAnswers = correctAnswersMap
        )

        // Skipped questions section
        QuestionStatusList(
            title = stringResource(R.string.quiz_skipped_questions),
            questionIndices = skippedQuestionIndices,
            quizQuestions = quizQuestions,
            status = QuestionStatus.SKIPPED
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
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(
                R.string.question_index,
                currentQuestionIndex + 1,
                totalQuestions
            ),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LinearProgressIndicator(
            progress = { (currentQuestionIndex + 1) / totalQuestions.toFloat() },
            modifier = Modifier.fillMaxWidth()
        )
    }
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
    isCorrect: Boolean,
    isAnsweredCorrectly: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val cornerRadius = 12.dp

    val targetBorderColor = when {
        showFeedback && isCorrect -> MaterialTheme.colorScheme.primary
        showFeedback && isSelected && !isAnsweredCorrectly -> MaterialTheme.colorScheme.error
        isSelected && !showFeedback -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 300),
        label = "option_border_color"
    )

    val targetBackgroundColor = when {
        showFeedback && isCorrect -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        showFeedback && isSelected && !isAnsweredCorrectly -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        isSelected && !showFeedback -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val animatedBackgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(durationMillis = 300),
        label = "option_background_color"
    )

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(
                enabled = enabled,
                onClick = onClick,
                interactionSource = interactionSource,
                indication = rememberRipple()
            ),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.outlinedCardColors(containerColor = animatedBackgroundColor),
        border = BorderStroke(2.dp, animatedBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected && !showFeedback) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = optionKey,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = optionValue,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            AnimatedVisibility(visible = isSelected || (showFeedback && isCorrect)) {
                val icon = when {
                    showFeedback && isCorrect -> Icons.Filled.CheckCircle
                    showFeedback && isSelected -> Icons.Filled.Cancel
                    isSelected -> Icons.Filled.RadioButtonChecked
                    else -> null
                }
                val tint = when {
                    showFeedback && isCorrect -> MaterialTheme.colorScheme.primary
                    showFeedback && isSelected -> MaterialTheme.colorScheme.error
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> LocalContentColor.current
                }
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                }
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
    value: String,
    isSelected: Boolean,
    showFeedback: Boolean,
    isCorrectOption: Boolean,
    isAnsweredCorrectly: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val cornerRadius = 12.dp

    val targetBorderColor = when {
        showFeedback && isCorrectOption -> MaterialTheme.colorScheme.primary
        showFeedback && isSelected && !isCorrectOption -> MaterialTheme.colorScheme.error
        isSelected && !showFeedback -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 300),
        label = "tf_option_border_color"
    )

    val targetBackgroundColor = when {
        showFeedback && isCorrectOption -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        showFeedback && isSelected && !isCorrectOption -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        isSelected && !showFeedback -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val animatedBackgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(durationMillis = 300),
        label = "tf_option_background_color"
    )

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(
                enabled = enabled,
                onClick = onClick,
                interactionSource = interactionSource,
                indication = rememberRipple()
            ),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.outlinedCardColors(containerColor = animatedBackgroundColor),
        border = BorderStroke(2.dp, animatedBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected && !showFeedback) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )

            AnimatedVisibility(visible = isSelected || (showFeedback && isCorrectOption)) {
                val icon = when {
                    showFeedback && isCorrectOption -> Icons.Filled.CheckCircle
                    showFeedback && isSelected -> Icons.Filled.Cancel
                    isSelected -> Icons.Filled.RadioButtonChecked
                    else -> null
                }
                val tint = when {
                    showFeedback && isCorrectOption -> MaterialTheme.colorScheme.primary
                    showFeedback && isSelected -> MaterialTheme.colorScheme.error
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> LocalContentColor.current
                }
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                }
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
    selectedAnswer: String,
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
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showFeedback) {
            Button(
                onClick = onNextQuestion,
                modifier = Modifier.fillMaxWidth(),
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
            OutlinedButton(
                onClick = onSkipQuestion,
                modifier = Modifier.weight(1f),
                enabled = true
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
            .verticalScroll(rememberScrollState())
    ) {
        QuestionHeader(currentQuestionIndex, quizQuestions.size)
        Spacer(modifier = Modifier.height(24.dp))

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
        }

        Spacer(modifier = Modifier.height(32.dp))

        QuizNavigationButtons(
            showFeedback = showFeedback,
            isLastQuestion = currentQuestionIndex == quizQuestions.size - 1,
            selectedAnswerNotEmpty = selectedAnswer.isNotEmpty(),
            onSubmitAnswer = onSubmitAnswer,
            onSkipQuestion = onSkipQuestion,
            onNextQuestion = onNextQuestion
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// --- Definition for EmptyStateComponent ---
@Composable
fun EmptyStateComponent(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    description: String? = null,
    actionContent: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (description != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (actionContent != null) {
            Spacer(modifier = Modifier.height(32.dp))
            actionContent()
        }
    }
}
// --- End of EmptyStateComponent Definition ---

