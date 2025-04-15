package com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.MultipleChoiceQuestion
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.quiz.TrueFalseQuestion
import com.nguyenmoclam.tutorialyoutubemadesimple.navigation.AppScreens
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.AndroidWebViewWithOfflineContent
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.ErrorStateComponent
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.ExitConfirmationDialog
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.LoadingState
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.MindMapContent
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.NetworkAwareWebView
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.OfflineModeIndicator
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.QuizContent
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.QuizResultsScreen
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.StartQuizScreen
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.SlidingRootNav
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.model.SlideGravity
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.model.SlidingRootNavState
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.model.rememberSlidingRootNavState
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.transform.ElevationTransformation
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.transform.ScaleTransformation
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.transform.YTranslationTransformation
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.LocalNetworkUtils
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.OfflineDataManager
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.OfflineSyncManager
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizCreationViewModel
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizDetailViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// CompositionLocal for providing NavController and QuizDetailViewModel to child composables
val LocalNavController =
    compositionLocalOf<NavHostController> { error("No NavController provided") }
val LocalQuizDetailViewModel =
    compositionLocalOf<QuizDetailViewModel> { error("No QuizDetailViewModel provided") }

/**
 * Displays summary content, automatically switching between online (NetworkAwareWebView)
 * and offline (OfflineSummaryWebView) modes. Includes a FAB to navigate to the video player.
 */
@Composable
fun SummaryContent(summaryHtml: String, quizId: Long) {
    val networkUtils = LocalNetworkUtils.current
    val navController = LocalNavController.current
    val quizDetailViewModel = LocalQuizDetailViewModel.current
    val context = LocalContext.current
    val offlineDataManager = remember { OfflineDataManager(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Content Area (WebView)
        if (networkUtils.isNetworkAvailable() && networkUtils.shouldLoadContent()) {
            NetworkAwareWebView(
                modifier = Modifier.fillMaxSize(),
                url = "", // URL is not used when HTML is provided directly
                html = summaryHtml,
                networkUtils = networkUtils,
                isJavaScriptEnabled = true,
                onPageFinished = {},
                onRetryClick = {}
            )
        } else {
            // Use the component from the components package
            AndroidWebViewWithOfflineContent(
                modifier = Modifier.fillMaxSize(),
                htmlContent = summaryHtml,
                offlineDataManager = offlineDataManager
            )
        }

        // FAB to Watch Video
        quizDetailViewModel.state.quiz?.let { quiz ->
            FloatingActionButton(
                onClick = {
                    val encodedUrl =
                        URLEncoder.encode(quiz.videoUrl, StandardCharsets.UTF_8.toString())
                    navController.navigate(AppScreens.VideoPlayer.route + "/${quiz.id}/$encodedUrl")
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = stringResource(R.string.watch_video)
                )
            }
        }

        // Offline Indicator
        if (!networkUtils.isNetworkAvailable()) {
            OfflineModeIndicator(modifier = Modifier.align(Alignment.TopEnd))
        }
    }
}

/**
 * Content for the navigation drawer in QuizDetailScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizDetailDrawerContent(
    navController: NavHostController,
    quizDetailViewModel: QuizDetailViewModel,
    slidingNavState: SlidingRootNavState,
    selectedContentIndex: Int,
    onContentIndexChange: (Int) -> Unit,
    scope: CoroutineScope
) {
    var materialsExpanded by remember { mutableStateOf(true) }
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues() // Get status bar padding

    // Apply top padding from status bar insets to the root Column
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(top = statusBarPadding.calculateTopPadding()) // Apply top padding
    ) {
        // Drawer Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.QuestionAnswer,
                contentDescription = stringResource(R.string.app_logo),
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

        // Navigation Items
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
            label = { Text(stringResource(R.string.dashboard_tab)) },
            selected = false,
            onClick = {
                scope.launch {
                    if (quizDetailViewModel.state.quizStarted && !quizDetailViewModel.state.quizCompleted && selectedContentIndex == 1) {
                        quizDetailViewModel.showExitConfirmation() // Show confirmation if quiz in progress
                    } else {
                        slidingNavState.closeMenu()
                        navController.navigate(AppScreens.Home.route) {
                            popUpTo(AppScreens.Home.route) {
                                inclusive = true
                            } // Avoid building up backstack
                            launchSingleTop = true
                        }
                    }
                }
            }
        )

        // Materials Section (Expandable)
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
            label = { Text(stringResource(R.string.materials_tab)) },
            badge = {
                Icon(
                    if (materialsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (materialsExpanded) stringResource(R.string.collapse) else stringResource(
                        R.string.expand
                    )
                )
            },
            // Materials header should not be selectable itself
            selected = false,
            onClick = { materialsExpanded = !materialsExpanded }
        )

        if (materialsExpanded) {
            // Apply standard horizontal padding for drawer content to the Column
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                // Summary Item
                NavigationDrawerItem(
                    // Reduce internal padding if needed, though shape change might be enough
                    modifier = Modifier.fillMaxWidth(), // Ensure item tries to fill width
                    icon = { Icon(Icons.Default.Summarize, contentDescription = null) },
                    label = { Text(stringResource(R.string.summary_tab)) },
                    selected = selectedContentIndex == 0,
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), // Subtle highlight background
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        selectedIconColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RectangleShape, // Use RectangleShape for full width highlight
                    onClick = {
                        scope.launch { slidingNavState.closeMenu() }
                        onContentIndexChange(0)
                    }
                )
                // Questions Item
                NavigationDrawerItem(
                    modifier = Modifier.fillMaxWidth(), // Ensure item tries to fill width
                    icon = { Icon(Icons.Default.Quiz, contentDescription = null) },
                    label = { Text(stringResource(R.string.questions_tab)) },
                    selected = selectedContentIndex == 1,
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        selectedIconColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RectangleShape, // Use RectangleShape for full width highlight
                    onClick = {
                        scope.launch { slidingNavState.closeMenu() }
                        onContentIndexChange(1)
                    }
                )
                // Mind Map Item
                NavigationDrawerItem(
                    modifier = Modifier.fillMaxWidth(), // Ensure item tries to fill width
                    icon = { Icon(Icons.Default.AccountTree, contentDescription = null) },
                    label = { Text(stringResource(R.string.mindmap_tab)) },
                    selected = selectedContentIndex == 2,
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        selectedIconColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RectangleShape, // Use RectangleShape for full width highlight
                    onClick = {
                        scope.launch { slidingNavState.closeMenu() }
                        onContentIndexChange(2)
                    }
                )
            }
        }

        // Settings Item
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.settings_tab)) },
            selected = false,
            onClick = {
                scope.launch {
                    slidingNavState.closeMenu()
                    // Navigate to QuizSettingScreen with quizId
                    val quizId = quizDetailViewModel.state.quiz?.id
                    if (quizId != null && quizId > 0) { // Ensure quizId is valid
                        navController.navigate(AppScreens.QuizSetting.route + "/$quizId")
                    } else {
                        // Handle error: Quiz ID not available (optional: show snackbar)
                    }
                }
            }
        )
    }
}

/**
 * TopAppBar for the QuizDetailScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizDetailTopAppBar(
    slidingNavState: SlidingRootNavState,
    scope: CoroutineScope
) {
    TopAppBar(
        title = { Text(stringResource(R.string.quiz_details_title)) },
        navigationIcon = {
            val isDrawerOpen = slidingNavState.isMenuOpened
            val icon = if (isDrawerOpen) Icons.Default.Close else Icons.Default.Menu
            val contentDesc =
                if (isDrawerOpen) stringResource(R.string.close_menu) else stringResource(R.string.open_menu)

            IconButton(onClick = {
                scope.launch {
                    if (isDrawerOpen) slidingNavState.closeMenu() else slidingNavState.openMenu()
                }
            }) {
                Icon(icon, contentDescription = contentDesc)
            }
        }
    )
}

// Removed definitions for:
// - OfflineModeIndicator
// - OfflineSummaryWebView
// - LoadingState
// - ErrorState
// - QuestionHeader
// - MultipleChoiceOptionItem
// - MultipleChoiceQuestionBody
// - TrueFalseOptionItem
// - TrueFalseQuestionBody
// - QuizNavigationButtons
// - QuizContent


/**
 * The main content area of the QuizDetailScreen, displayed within the Scaffold.
 * Handles switching between Loading, Error, Summary, Questions, and MindMap views.
 */
@Composable
fun QuizDetailScreenContent(
    paddingValues: PaddingValues,
    quizIdLong: Long,
    quizViewModel: QuizCreationViewModel,
    quizDetailViewModel: QuizDetailViewModel,
    selectedContentIndex: Int,
    quizQuestions: List<Any>,
    currentQuestionIndex: Int,
    onCurrentQuestionIndexChange: (Int) -> Unit,
    selectedAnswer: String,
    onSelectedAnswerChange: (String) -> Unit,
    showFeedback: Boolean,
    onShowFeedbackChange: (Boolean) -> Unit,
    isCorrect: Boolean,
    onIsCorrectChange: (Boolean) -> Unit,
    answeredQuestions: Map<Int, String> // Pass down for QuizContent logic
) {
    val context = LocalContext.current
    val navController = LocalNavController.current // Get NavController from CompositionLocal

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues) // Apply padding from Scaffold
    ) {
        when {
            // Loading State
            quizViewModel.state.isLoading || quizDetailViewModel.state.isLoading -> {
                val progress = when {
                    quizIdLong <= 0 && selectedContentIndex != 2 -> quizViewModel.state.currentStep.getProgressPercentage() // Creation flow
                    selectedContentIndex == 2 -> quizDetailViewModel.state.currentMindMapStep.getProgressPercentage() // Mind map generation
                    else -> if (quizDetailViewModel.state.isLoading) 50f else 0f // Detail loading
                }
                val message = when {
                    quizIdLong <= 0 && selectedContentIndex != 2 -> quizViewModel.state.currentStep.getMessage(
                        context
                    )

                    selectedContentIndex == 2 -> quizDetailViewModel.state.currentMindMapStep.getMessage(
                        context
                    )

                    else -> stringResource(R.string.loading_quiz_details)
                }
                LoadingState(progress = progress, message = message)
            }

            // Error State
            quizViewModel.state.errorMessage != null || quizDetailViewModel.state.errorMessage != null -> {
                // Use the component from CommonComponents.kt
                ErrorStateComponent(
                    errorMessage = quizViewModel.state.errorMessage
                        ?: quizDetailViewModel.state.errorMessage
                        ?: stringResource(R.string.unknown_error)
                )
            }

            // Content State
            else -> {
                when (selectedContentIndex) {
                    // Summary Tab
                    0 -> {
                        val summaryContent =
                            if (quizIdLong > 0) quizDetailViewModel.state.summary else quizViewModel.state.quizSummary
                        if (summaryContent.isNotEmpty()) {
                            SummaryContent(summaryContent, quizIdLong)
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stringResource(R.string.no_summary_available))
                            }
                        }
                    }

                    // Questions Tab
                    1 -> {
                        if (quizQuestions.isNotEmpty()) {
                            when {
                                // Quiz Completed: Show Results
                                quizDetailViewModel.state.quizCompleted -> {
                                    QuizResultsScreen(
                                        quizQuestions = quizQuestions,
                                        correctAnswers = quizDetailViewModel.getCorrectAnswersCount(),
                                        incorrectAnswers = quizDetailViewModel.getIncorrectAnswersCount(),
                                        skippedQuestions = quizDetailViewModel.getSkippedQuestionsCount(), // Use calculated skipped count
                                        completionTimeSeconds = quizDetailViewModel.getQuizCompletionTimeInSeconds(),
                                        correctQuestionIndices = quizDetailViewModel.getCorrectlyAnsweredQuestions(),
                                        incorrectQuestionIndices = quizDetailViewModel.getIncorrectlyAnsweredQuestions(),
                                        skippedQuestionIndices = quizDetailViewModel.getSkippedQuestions(), // Use calculated skipped indices
                                        onRetryQuiz = { quizDetailViewModel.resetQuiz() }
                                    )
                                }
                                // Quiz Not Started: Show Start Screen
                                !quizDetailViewModel.state.quizStarted -> {
                                    StartQuizScreen(
                                        questionCount = quizQuestions.size,
                                        onStartQuiz = {
                                            quizDetailViewModel.startQuiz()
                                            onSelectedAnswerChange("")
                                            onShowFeedbackChange(false)
                                            onCurrentQuestionIndexChange(0) // Start from first question
                                        }
                                    )
                                }
                                // Quiz In Progress: Show Quiz Content
                                else -> {
                                    // Exit Confirmation Dialog
                                    if (quizDetailViewModel.state.showExitConfirmation) {
                                        ExitConfirmationDialog(
                                            onConfirm = {
                                                quizDetailViewModel.confirmExit()
                                                navController.popBackStack() // Use NavController from CompositionLocal
                                            },
                                            onDismiss = { quizDetailViewModel.hideExitConfirmation() }
                                        )
                                    }

                                    QuizContent(
                                        quizQuestions = quizQuestions,
                                        currentQuestionIndex = currentQuestionIndex,
                                        selectedAnswer = selectedAnswer,
                                        onAnswerSelected = onSelectedAnswerChange,
                                        showFeedback = showFeedback,
                                        isCorrect = isCorrect,
                                        onSubmitAnswer = {
                                            val currentQ = quizQuestions[currentQuestionIndex]
                                            val correct = when (currentQ) {
                                                is MultipleChoiceQuestion -> currentQ.correctAnswers.contains(
                                                    selectedAnswer
                                                )

                                                is TrueFalseQuestion -> (selectedAnswer == "True" && currentQ.isTrue) || (selectedAnswer == "False" && !currentQ.isTrue)
                                                else -> false
                                            }
                                            onIsCorrectChange(correct)
                                            onShowFeedbackChange(true)
                                            quizDetailViewModel.saveQuizProgress(
                                                currentQuestionIndex,
                                                selectedAnswer
                                            )
                                            // answeredQuestions state is updated via LaunchedEffect observing ViewModel
                                        },
                                        onSkipQuestion = {
                                            quizDetailViewModel.skipQuestion(currentQuestionIndex)
                                            // ViewModel state change triggers LaunchedEffect to update currentQuestionIndex
                                            onSelectedAnswerChange("")
                                            onShowFeedbackChange(false)
                                        },
                                        onNextQuestion = {
                                            if (currentQuestionIndex < quizQuestions.size - 1) {
                                                onCurrentQuestionIndexChange(currentQuestionIndex + 1)
                                                onSelectedAnswerChange("")
                                                onShowFeedbackChange(false)
                                            } else {
                                                // Last question answered/skipped, trigger completion check
                                                quizDetailViewModel.checkQuizCompletion()
                                            }
                                        }
                                    )
                                }
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

                    // Mind Map Tab
                    2 -> {
                        if (quizDetailViewModel.state.mindMapCode.isNotEmpty()) {
                            MindMapContent(
                                code = quizDetailViewModel.state.mindMapCode,
                                onFixCodeRequested = { originalCode, errorMsg ->
                                    val llmProcessor = quizDetailViewModel.getLLMProcessor()
                                    val language = quizDetailViewModel.getLanguage()
                                    val fixedCode = llmProcessor.fixMindMapCode(
                                        originalCode,
                                        errorMsg,
                                        language
                                    )
                                    quizDetailViewModel.updateMindMapCode(fixedCode)
                                    fixedCode // Return the fixed code
                                }
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(onClick = { quizDetailViewModel.generateMindMap() }) {
                                    Text(stringResource(R.string.generate_mindmap))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- Main Screen Composable ---

/**
 * QuizDetailScreen: Displays quiz details (summary, questions, mind map) with a navigation drawer.
 * Handles loading, error states, and quiz progression logic via ViewModels.
 * Incorporates automatic offline mode support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizDetailScreen(
    quizId: String,
    navController: NavHostController,
    quizDetailViewModel: QuizDetailViewModel = hiltViewModel(), // Use hiltViewModel here
    quizViewModel: QuizCreationViewModel = hiltViewModel() // For creation flow state
) {
    val quizIdLong = remember(quizId) { quizId.toLongOrNull() ?: -1L }

    // Provide dependencies down the tree
    CompositionLocalProvider(
        LocalNavController provides navController,
        LocalQuizDetailViewModel provides quizDetailViewModel,
        LocalNetworkUtils provides LocalNetworkUtils.current // Ensure NetworkUtils is available if needed lower down
    ) {
        val context = LocalContext.current
        val networkUtils = LocalNetworkUtils.current
        val offlineDataManager = remember { OfflineDataManager(context) }
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        // --- State Management ---
        var selectedContentIndex by remember { mutableIntStateOf(0) }
        // Quiz-specific state hoisted from QuizContent/QuizDetailScreenContent
        var quizQuestions by remember { mutableStateOf<List<Any>>(emptyList()) }
        var currentQuestionIndex by remember { mutableIntStateOf(quizDetailViewModel.state.currentQuestionIndex) }
        var selectedAnswer by remember { mutableStateOf("") }
        var showFeedback by remember { mutableStateOf(false) }
        var isCorrect by remember { mutableStateOf(false) }
        // Local copy of answered questions, updated via LaunchedEffect from ViewModel
        var answeredQuestions by remember { mutableStateOf<Map<Int, String>>(quizDetailViewModel.state.answeredQuestions) }


        // --- Drawer Setup ---
        val configuration = LocalConfiguration.current
        val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
        val dragDistance =
            remember(screenWidthPx) { (0.7f * screenWidthPx).toInt() } // Adjust drag distance if needed
        val slidingNavState = rememberSlidingRootNavState()

        // --- Effects ---

        // Back Navigation Handler (only when quiz is active)
        BackHandler(enabled = quizDetailViewModel.state.quizStarted && !quizDetailViewModel.state.quizCompleted && selectedContentIndex == 1) {
            quizDetailViewModel.showExitConfirmation()
        }

        // Load Quiz Data Effect
        LaunchedEffect(quizIdLong, quizViewModel.state.quizIdInserted) {
            val idToLoad = if (quizIdLong > 0) quizIdLong else quizViewModel.state.quizIdInserted
            if (idToLoad >= 0L) {
                quizDetailViewModel.loadQuizById(idToLoad)
            }
        }

        // Network & Offline Sync Effect
        LaunchedEffect(Unit) {
            val offlineSyncManager = OfflineSyncManager(
                networkUtils,
                quizDetailViewModel.getQuizRepository(),
                offlineDataManager
            )
            offlineSyncManager.startObservingNetworkChanges()
            if (!networkUtils.isNetworkAvailable()) {
                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.offline_mode_snackbar)) }
            }
        }

        // Update Local Quiz Questions Effect
        LaunchedEffect(quizDetailViewModel.state.questions) {
            quizQuestions = quizDetailViewModel.state.questions
        }

        // Sync Quiz Progress State Effect (ViewModel -> Local State)
        LaunchedEffect(
            quizQuestions,
            quizDetailViewModel.state.answeredQuestions,
            quizDetailViewModel.state.currentQuestionIndex,
            quizDetailViewModel.state.quizStarted
        ) {
            if (quizQuestions.isNotEmpty() && quizDetailViewModel.state.quizStarted) {
                answeredQuestions = quizDetailViewModel.state.answeredQuestions
                val lastAnsweredIndex =
                    quizDetailViewModel.getLastAnsweredQuestionIndex() // Get latest index from VM
                val nextIndexToShow =
                    if (lastAnsweredIndex >= 0 && lastAnsweredIndex < quizQuestions.size) lastAnsweredIndex else quizDetailViewModel.state.currentQuestionIndex

                // Only update local state if it differs from ViewModel's intended index
                if (currentQuestionIndex != nextIndexToShow) {
                    currentQuestionIndex = nextIndexToShow
                }

                // Restore feedback state for the current question if it was answered
                val answerForCurrent =
                    quizDetailViewModel.getAnswerForQuestion(currentQuestionIndex)
                if (answerForCurrent.isNotEmpty()) {
                    selectedAnswer = answerForCurrent
                    showFeedback = true
                    val currentQ = quizQuestions[currentQuestionIndex]
                    isCorrect = when (currentQ) {
                        is MultipleChoiceQuestion -> currentQ.correctAnswers.contains(
                            answerForCurrent
                        )

                        is TrueFalseQuestion -> (answerForCurrent == "True" && currentQ.isTrue) || (answerForCurrent == "False" && !currentQ.isTrue)
                        else -> false
                    }
                } else {
                    // Reset if moving to an unanswered question
                    selectedAnswer = ""
                    showFeedback = false
                    isCorrect = false
                }
            } else if (!quizDetailViewModel.state.quizStarted) {
                // Reset state if quiz is reset or not started
                currentQuestionIndex = 0
                selectedAnswer = ""
                showFeedback = false
                isCorrect = false
                answeredQuestions = emptyMap()
            }
        }


        // --- UI Structure ---
        SlidingRootNav(
            state = slidingNavState,
            modifier = Modifier,
            dragDistance = dragDistance,
            gravity = SlideGravity.LEFT,
            transformations = listOf(
                ScaleTransformation(0.75f), // Slightly less scale
                ElevationTransformation(12.dp.value), // Less elevation
                YTranslationTransformation(10.dp.value) // Less translation
            ),
            menuContent = {
                QuizDetailDrawerContent(
                    navController = navController,
                    quizDetailViewModel = quizDetailViewModel,
                    slidingNavState = slidingNavState,
                    selectedContentIndex = selectedContentIndex,
                    onContentIndexChange = { selectedContentIndex = it },
                    scope = scope
                )
            },
            contentContent = {
                Scaffold(
                    topBar = {
                        // Show TopAppBar only when not loading
                        if (!quizViewModel.state.isLoading && !quizDetailViewModel.state.isLoading) {
                            QuizDetailTopAppBar(slidingNavState = slidingNavState, scope = scope)
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { paddingValues ->
                    QuizDetailScreenContent(
                        paddingValues = paddingValues,
                        quizIdLong = quizIdLong,
                        quizViewModel = quizViewModel,
                        quizDetailViewModel = quizDetailViewModel,
                        selectedContentIndex = selectedContentIndex,
                        quizQuestions = quizQuestions,
                        currentQuestionIndex = currentQuestionIndex,
                        onCurrentQuestionIndexChange = { currentQuestionIndex = it },
                        selectedAnswer = selectedAnswer,
                        onSelectedAnswerChange = { selectedAnswer = it },
                        showFeedback = showFeedback,
                        onShowFeedbackChange = { showFeedback = it },
                        isCorrect = isCorrect,
                        onIsCorrectChange = { isCorrect = it },
                        answeredQuestions = answeredQuestions // Pass down
                    )
                }
            }
        )
    }
}
