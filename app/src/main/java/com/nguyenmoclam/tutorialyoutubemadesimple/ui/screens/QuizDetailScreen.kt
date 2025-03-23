package com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.MultipleChoiceQuestion
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.TrueFalseQuestion
import com.nguyenmoclam.tutorialyoutubemadesimple.navigation.AppScreens
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.ExitConfirmationDialog
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.MultiWaveLoadingAnimation
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.NetworkAwareWebView
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.QuizResultsScreen
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.StartQuizScreen
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.SlidingRootNav
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.model.SlideGravity
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.model.rememberSlidingRootNavState
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.transform.ElevationTransformation
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.transform.ScaleTransformation
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.transform.YTranslationTransformation
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.LocalNetworkUtils
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizCreationViewModel
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizDetailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizDetailScreen(
    navController: NavHostController,
    quizViewModel: QuizCreationViewModel,
    quizId: Long = -1L,
    quizDetailViewModel: QuizDetailViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var materialsExpanded by remember { mutableStateOf(true) }
    var selectedContentIndex by remember { mutableIntStateOf(0) }

    var quizQuestions by remember { mutableStateOf<List<Any>>(emptyList()) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableStateOf("") }
    var showFeedback by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }

    // Track answered questions and their selected answers - now using ViewModel state
    var answeredQuestions by remember { mutableStateOf<Map<Int, String>>(quizDetailViewModel.state.answeredQuestions) }

    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) {
        configuration.screenWidthDp.dp.toPx()
    }
    val dragDistance = (0.4f * screenWidthPx).toInt()

    // Handle system back button press
    BackHandler(enabled = quizDetailViewModel.state.quizStarted && !quizDetailViewModel.state.quizCompleted && selectedContentIndex == 1) {
        quizDetailViewModel.showExitConfirmation()
    }

    // Load quiz data from database if quizId is provided
    LaunchedEffect(quizId) {
        if (quizId > 0) {
            quizDetailViewModel.loadQuizById(quizId)
        }
    }

    // Load quiz questions from QuizCreationViewModel (when coming from quiz creation)
    LaunchedEffect(quizViewModel.state.quizIdInserted) {
        if (quizId <= 0 && quizViewModel.state.quizIdInserted >= 0L) {
            try {
                quizDetailViewModel.loadQuizById(quizViewModel.state.quizIdInserted)
            } catch (e: Exception) {
                println("Error parsing quiz questions: ${e.message}")
            }
        }
    }

    LaunchedEffect(quizDetailViewModel.state.questions) {
        if (quizDetailViewModel.state.questions.isNotEmpty()) {
            quizQuestions = quizDetailViewModel.state.questions
        }
    }

    // Set the current question index based on answered questions from ViewModel
    LaunchedEffect(quizQuestions, quizDetailViewModel.state.answeredQuestions) {
        if (quizQuestions.isNotEmpty()) {
            // Update local state with ViewModel state
            answeredQuestions = quizDetailViewModel.state.answeredQuestions

            // If we have answered questions, show the last answered question
            if (answeredQuestions.isNotEmpty()) {
                // Use the ViewModel's method to get the last answered question index
                currentQuestionIndex = quizDetailViewModel.getLastAnsweredQuestionIndex()
                selectedAnswer = quizDetailViewModel.getAnswerForQuestion(currentQuestionIndex)
                showFeedback = selectedAnswer.isNotEmpty()

                // Check if the answer was correct
                if (showFeedback) {
                    val currentQuestion = quizQuestions[currentQuestionIndex]
                    isCorrect = when (currentQuestion) {
                        is MultipleChoiceQuestion -> {
                            currentQuestion.correctAnswers.contains(selectedAnswer)
                        }

                        is TrueFalseQuestion -> {
                            (selectedAnswer == "True" && currentQuestion.isTrue) ||
                                    (selectedAnswer == "False" && !currentQuestion.isTrue)
                        }

                        else -> false
                    }
                }
            } else {
                // If no questions have been answered, show the first question
                currentQuestionIndex = quizDetailViewModel.state.currentQuestionIndex
                selectedAnswer = ""
                showFeedback = false
            }
        }
    }

    // Create a state for the sliding root navigation
    val slidingNavState =
        rememberSlidingRootNavState(initialDragProgress = if (drawerState.currentValue == DrawerValue.Open) 1f else 0f)

    // Use SlidingRootNav directly with the state
    SlidingRootNav(
        state = slidingNavState,
        modifier = Modifier,
        dragDistance = dragDistance,
        gravity = SlideGravity.LEFT,
        transformations = listOf(
            ScaleTransformation(0.65f),
            ElevationTransformation(20.dp.value),
            YTranslationTransformation(20.dp.value)
        ),
        menuContent = {
            Column(modifier = Modifier.fillMaxHeight()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.QuestionAnswer,
                        contentDescription = "App Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.quiz),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Divider()

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text(stringResource(R.string.dashboard_tab)) },
                    selected = false,
                    onClick = {
                        scope.launch {

                            if (quizDetailViewModel.state.quizStarted && !quizDetailViewModel.state.quizCompleted && selectedContentIndex == 1) {
                                quizDetailViewModel.showExitConfirmation()
                            } else {
                                slidingNavState.dragProgress = 0f
                                navController.navigate(AppScreens.Home.route)
                            }
                        }
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Materials") },
                    label = { Text(stringResource(R.string.materials_tab)) },
                    badge = {
                        if (materialsExpanded) {
                            Icon(Icons.Default.ExpandLess, contentDescription = "Collapse")
                        } else {
                            Icon(Icons.Default.ExpandMore, contentDescription = "Expand")
                        }
                    },
                    selected = false,
                    onClick = { materialsExpanded = !materialsExpanded }
                )

                if (materialsExpanded) {
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    Icons.Default.Summarize,
                                    contentDescription = "Summary"
                                )
                            },
                            label = { Text(stringResource(R.string.summary_tab)) },
                            selected = selectedContentIndex == 0,
                            onClick = {
                                scope.launch {
                                    slidingNavState.dragProgress = 0f
                                }
                                selectedContentIndex = 0
                            }
                        )

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Quiz, contentDescription = "Questions") },
                            label = { Text(stringResource(R.string.questions_tab)) },
                            selected = selectedContentIndex == 1,
                            onClick = {
                                scope.launch {
                                    slidingNavState.dragProgress = 0f
                                }
                                selectedContentIndex = 1
                            }
                        )
                    }
                }

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text(stringResource(R.string.settings_tab)) },
                    selected = false,
                    onClick = {
                        scope.launch {
                            slidingNavState.closeMenu()
                            navController.navigate(AppScreens.Settings.route)
                        }
                    }
                )
            }
        },
        contentContent = {
            Scaffold(
                topBar = {
                    if (!quizViewModel.state.isLoading && !quizDetailViewModel.state.isLoading) {
                        TopAppBar(
                            title = { Text(stringResource(R.string.quiz_details_title)) },
                            navigationIcon = {
                                val isDrawerOpen = slidingNavState.isMenuOpened
                                val icon =
                                    if (isDrawerOpen) Icons.Default.Close else Icons.Default.Menu

                                IconButton(onClick = {
                                    scope.launch {
                                        //slidingNavState.dragProgress = 1f
                                        if (isDrawerOpen) {
                                            slidingNavState.closeMenu()
                                        } else {
                                            slidingNavState.openMenu()
                                        }
                                    }
                                }) {
                                    Icon(icon, contentDescription = "Menu")
                                }
                            }
                        )
                    }
                }
            ) { paddingValues ->
                // Main content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when {
                        quizViewModel.state.isLoading || quizDetailViewModel.state.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .wrapContentHeight()
                                        .animateContentSize()
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        MultiWaveLoadingAnimation(
                                            progress = if (quizId <= 0) {
                                                // Coming from quiz creation - use QuizCreationViewModel
                                                quizViewModel.state.currentStep.getProgressPercentage()
                                            } else {
                                                // Coming from HomeScreen - use a fixed progress for QuizDetailViewModel
                                                if (quizDetailViewModel.state.isLoading) 50f else 0f
                                            },
                                            modifier = Modifier.size(200.dp)
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                            text = if (quizId <= 0) {
                                                // Coming from quiz creation - use QuizCreationViewModel message
                                                quizViewModel.state.currentStep.getMessage(
                                                    LocalContext.current
                                                )
                                            } else {
                                                // Coming from HomeScreen - use a generic loading message
                                                stringResource(R.string.loading_quiz_details)
                                            },
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        quizViewModel.state.errorMessage != null || quizDetailViewModel.state.errorMessage != null -> {
                            // Error state
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = quizViewModel.state.errorMessage
                                        ?: quizDetailViewModel.state.errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        else -> {
                            if (selectedContentIndex == 0) {
                                // Summary
                                // Get summary from either QuizCreationViewModel or QuizDetailViewModel
                                val summaryContent = if (quizId > 0) {
                                    quizDetailViewModel.state.summary
                                } else {
                                    quizViewModel.state.quizSummary
                                }

                                if (summaryContent.isNotEmpty()) {
                                    SummaryContent(summaryContent)
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(stringResource(R.string.no_summary_available))
                                    }
                                }
                            } else {
                                // Questions
                                if (quizQuestions.isNotEmpty()) {
                                    // Check if quiz is completed to show results screen
                                    if (quizDetailViewModel.state.quizCompleted) {
                                        // skippedQuestions, skippedQuestionIndices get data from Database if needed
                                        QuizResultsScreen(
                                            quizQuestions = quizQuestions,
                                            correctAnswers = quizDetailViewModel.getCorrectAnswersCount(),
                                            incorrectAnswers = quizDetailViewModel.getIncorrectAnswersCount(),
                                            skippedQuestions =
                                                if (quizDetailViewModel.state.skippedQuestions.isNotEmpty())
                                                    quizDetailViewModel.state.skippedQuestions.size
                                                else
                                                    quizDetailViewModel.getSkippedQuestionsCount(),
                                            completionTimeSeconds = quizDetailViewModel.getQuizCompletionTimeInSeconds(),
                                            correctQuestionIndices = quizDetailViewModel.getCorrectlyAnsweredQuestions(),
                                            incorrectQuestionIndices = quizDetailViewModel.getIncorrectlyAnsweredQuestions(),
                                            skippedQuestionIndices =
                                                if (quizDetailViewModel.state.skippedQuestions.isNotEmpty())
                                                    quizDetailViewModel.state.skippedQuestions.toList()
                                                else
                                                    quizDetailViewModel.getSkippedQuestions(),
                                            onRetryQuiz = {
                                                quizDetailViewModel.resetQuiz()
                                            }
                                        )
                                    }
                                    // Check if quiz has not started yet
                                    else if (!quizDetailViewModel.state.quizStarted) {
                                        StartQuizScreen(
                                            questionCount = quizQuestions.size,
                                            onStartQuiz = {
                                                quizDetailViewModel.startQuiz()
                                                selectedAnswer = ""
                                                showFeedback = false
                                                currentQuestionIndex = 0
                                            }
                                        )
                                    }
                                    // Show quiz content if quiz is started and not completed
                                    else {
                                        // Show exit confirmation dialog if needed
                                        if (quizDetailViewModel.state.showExitConfirmation) {
                                            ExitConfirmationDialog(
                                                onConfirm = {
                                                    quizDetailViewModel.confirmExit()
                                                    navController.popBackStack()
                                                },
                                                onDismiss = {
                                                    quizDetailViewModel.hideExitConfirmation()
                                                }
                                            )
                                        }

                                        QuizContent(
                                            quizQuestions = quizQuestions,
                                            currentQuestionIndex = currentQuestionIndex,
                                            onQuestionChange = { currentQuestionIndex = it },
                                            selectedAnswer = selectedAnswer,
                                            onAnswerSelected = { selectedAnswer = it },
                                            showFeedback = showFeedback,
                                            isCorrect = isCorrect,
                                            onSubmitAnswer = {
                                                val currentQuestion =
                                                    quizQuestions[currentQuestionIndex]
                                                isCorrect = when (currentQuestion) {
                                                    is MultipleChoiceQuestion -> {
                                                        currentQuestion.correctAnswers.contains(
                                                            selectedAnswer
                                                        )
                                                    }

                                                    is TrueFalseQuestion -> {
                                                        (selectedAnswer == "True" && currentQuestion.isTrue) ||
                                                                (selectedAnswer == "False" && !currentQuestion.isTrue)
                                                    }

                                                    else -> false
                                                }
                                                showFeedback = true

                                                // Save the progress in the ViewModel after checking correctness
                                                quizDetailViewModel.saveQuizProgress(
                                                    currentQuestionIndex,
                                                    selectedAnswer
                                                )

                                                // Update local state to match ViewModel state
                                                answeredQuestions =
                                                    quizDetailViewModel.state.answeredQuestions
                                            },
                                            onSkipQuestion = {
                                                quizDetailViewModel.skipQuestion(
                                                    currentQuestionIndex
                                                )
                                                // Update local state to match ViewModel state
                                                currentQuestionIndex =
                                                    quizDetailViewModel.state.currentQuestionIndex
                                                if (currentQuestionIndex < quizQuestions.size - 1) {
                                                    currentQuestionIndex++
                                                    selectedAnswer = ""
                                                    showFeedback = false
                                                }
                                            },
                                            onNextQuestion = {
                                                if (currentQuestionIndex < quizQuestions.size - 1) {
                                                    // Move to the next question
                                                    currentQuestionIndex++
                                                    selectedAnswer = ""
                                                    showFeedback = false

                                                }
                                                // If this is the last question and all questions are answered/skipped,
                                                // we need to explicitly check completion
                                                else if (currentQuestionIndex == quizQuestions.size - 1) {
                                                    // Force a check for quiz completion
                                                    quizDetailViewModel.checkQuizCompletion()
                                                }
                                            }
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(stringResource(R.string.no_questions_available))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SummaryContent(summaryHtml: String) {
    val networkUtils = LocalNetworkUtils.current

    NetworkAwareWebView(
        modifier = Modifier.fillMaxSize(),
        url = "",
        html = summaryHtml,
        networkUtils = networkUtils,
        isJavaScriptEnabled = true,
        onPageFinished = {},
        onRetryClick = {}
    )
}

@Composable
fun QuizContent(
    quizQuestions: List<Any>,
    currentQuestionIndex: Int,
    onQuestionChange: (Int) -> Unit,
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
        Text(
            text = stringResource(
                R.string.question_index,
                currentQuestionIndex + 1,
                quizQuestions.size
            ),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        val currentQuestion = quizQuestions[currentQuestionIndex]
        when (currentQuestion) {
            is MultipleChoiceQuestion -> {
                Text(
                    text = currentQuestion.question,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                currentQuestion.options.forEach { (key, value) ->
                    val isSelected = selectedAnswer == key
                    val backgroundColor = when {
                        !showFeedback -> {
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        }

                        isSelected && isCorrect -> Color(0xFFDCEDC8)
                        isSelected && !isCorrect -> Color(0xFFFFCDD2)
                        currentQuestion.correctAnswers.contains(key) -> Color(0xFFDCEDC8)
                        else -> MaterialTheme.colorScheme.surface
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable(enabled = !showFeedback) {
                                onAnswerSelected(key)
                            },
                        colors = CardDefaults.cardColors(containerColor = backgroundColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = key,
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
                            Text(text = value, modifier = Modifier.weight(1f))

                            if (showFeedback) {
                                if (currentQuestion.correctAnswers.contains(key)) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Correct",
                                        tint = Color.Green
                                    )
                                } else if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Incorrect",
                                        tint = Color.Red
                                    )
                                }
                            } else if (isSelected) {
                                RadioButton(selected = true, onClick = null)
                            } else {
                                RadioButton(selected = false, onClick = null)
                            }
                        }
                    }
                }
            }

            is TrueFalseQuestion -> {
                Text(
                    text = currentQuestion.statement,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Option True
                val isTrueSelected = selectedAnswer == "True"
                val trueBackgroundColor = when {
                    !showFeedback -> {
                        if (isTrueSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    }

                    isTrueSelected && currentQuestion.isTrue -> Color(0xFFDCEDC8)
                    isTrueSelected && !currentQuestion.isTrue -> Color(0xFFFFCDD2)
                    currentQuestion.isTrue -> Color(0xFFDCEDC8)
                    else -> MaterialTheme.colorScheme.surface
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = !showFeedback) {
                            onAnswerSelected("True")
                        },
                    colors = CardDefaults.cardColors(containerColor = trueBackgroundColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.true_txt),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        if (showFeedback) {
                            if (currentQuestion.isTrue) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Correct",
                                    tint = Color.Green
                                )
                            } else if (isTrueSelected) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Incorrect",
                                    tint = Color.Red
                                )
                            }
                        } else if (isTrueSelected) {
                            RadioButton(selected = true, onClick = null)
                        } else {
                            RadioButton(selected = false, onClick = null)
                        }
                    }
                }

                // Option False
                val isFalseSelected = selectedAnswer == "False"
                val falseBackgroundColor = when {
                    !showFeedback -> {
                        if (isFalseSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    }

                    isFalseSelected && !currentQuestion.isTrue -> Color(0xFFDCEDC8)
                    isFalseSelected && currentQuestion.isTrue -> Color(0xFFFFCDD2)
                    !currentQuestion.isTrue -> Color(0xFFDCEDC8)
                    else -> MaterialTheme.colorScheme.surface
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = !showFeedback) {
                            onAnswerSelected("False")
                        },
                    colors = CardDefaults.cardColors(containerColor = falseBackgroundColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.false_txt),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        if (showFeedback) {
                            if (!currentQuestion.isTrue) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Correct",
                                    tint = Color.Green
                                )
                            } else if (isFalseSelected) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Incorrect",
                                    tint = Color.Red
                                )
                            }
                        } else if (isFalseSelected) {
                            RadioButton(selected = true, onClick = null)
                        } else {
                            RadioButton(selected = false, onClick = null)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (showFeedback) {
                Button(
                    onClick = onNextQuestion,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = currentQuestionIndex < quizQuestions.size - 1
                ) {
                    Text(stringResource(R.string.next_question_button))
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSkipQuestion,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.skip_question_button))
                    }

                    Button(
                        onClick = onSubmitAnswer,
                        modifier = Modifier.weight(1f),
                        enabled = selectedAnswer.isNotEmpty()
                    ) {
                        Text(stringResource(R.string.submit_answer_button))
                    }
                }
            }
        }
    }
}
