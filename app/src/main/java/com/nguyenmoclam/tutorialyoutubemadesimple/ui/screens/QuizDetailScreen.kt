package com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.MultipleChoiceQuestion
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.TrueFalseQuestion
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.LLMProcessor
import com.nguyenmoclam.tutorialyoutubemadesimple.navigation.AppScreens
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.MultiWaveLoadingAnimation
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizCreationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizDetailScreen(
    navController: NavHostController,
    quizViewModel: QuizCreationViewModel
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

    LaunchedEffect(quizViewModel.state.quizQuestionsJson) {
        if (quizViewModel.state.quizQuestionsJson.isNotEmpty()) {
            try {
                val llmProcessor = LLMProcessor()
                val (multipleChoiceQuestions, trueFalseQuestions) =
                    llmProcessor.parseQuizQuestions(quizViewModel.state.quizQuestionsJson)
                quizQuestions = multipleChoiceQuestions + trueFalseQuestions
            } catch (e: Exception) {
                println("Error parsing quiz questions: ${e.message}")
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
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
                        text = "Quiz",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Divider()

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate(AppScreens.Home.route)
                        }
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Materials") },
                    label = { Text("Materials") },
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
                            icon = { Icon(Icons.Default.Summarize, contentDescription = "Summary") },
                            label = { Text("Summary") },
                            selected = selectedContentIndex == 0,
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                }
                                selectedContentIndex = 0
                            }
                        )

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Quiz, contentDescription = "Questions") },
                            label = { Text("Questions") },
                            selected = selectedContentIndex == 1,
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                }
                                selectedContentIndex = 1
                            }
                        )
                    }
                }

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate(AppScreens.Settings.route)
                        }
                    }
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        },
        content = {
            Scaffold(
                topBar = {
                    if (!quizViewModel.state.isLoading) {
                        TopAppBar(
                            title = { Text("Quiz Details") },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            },
                            actions = {
                                IconButton(onClick = { /* Share action */ }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share")
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
                        quizViewModel.state.isLoading -> {
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
                                            progress = quizViewModel.state.currentStep.getProgressPercentage(),
                                            modifier = Modifier.size(200.dp)
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                            text = quizViewModel.state.currentStep.getMessage(LocalContext.current),
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        quizViewModel.state.errorMessage != null -> {
                            // Error state
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = quizViewModel.state.errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        else -> {
                            if (selectedContentIndex == 0) {
                                // Summary
                                if (quizViewModel.state.quizSummary.isNotEmpty()) {
                                    SummaryContent(quizViewModel.state.quizSummary)
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No summary available")
                                    }
                                }
                            } else {
                                // Questions
                                if (quizQuestions.isNotEmpty()) {
                                    QuizContent(
                                        quizQuestions = quizQuestions,
                                        currentQuestionIndex = currentQuestionIndex,
                                        onQuestionChange = { currentQuestionIndex = it },
                                        selectedAnswer = selectedAnswer,
                                        onAnswerSelected = { selectedAnswer = it },
                                        showFeedback = showFeedback,
                                        isCorrect = isCorrect,
                                        onSubmitAnswer = {
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
                                            showFeedback = true
                                        },
                                        onNextQuestion = {
                                            if (currentQuestionIndex < quizQuestions.size - 1) {
                                                currentQuestionIndex++
                                                selectedAnswer = ""
                                                showFeedback = false
                                            }
                                        }
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No questions available")
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
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                }
                webViewClient = WebViewClient()
                loadDataWithBaseURL(
                    null,
                    summaryHtml,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        modifier = Modifier.fillMaxSize()
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
    onNextQuestion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Question ${currentQuestionIndex + 1} of ${quizQuestions.size}",
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
                            text = "True",
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
                            text = "False",
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
                    Text("Next Question")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                }
            } else {
                Button(
                    onClick = onSubmitAnswer,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedAnswer.isNotEmpty()
                ) {
                    Text("Submit Answer")
                }
            }
        }
    }
}
