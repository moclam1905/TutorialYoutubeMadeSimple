package com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.EmptyStateComponent
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
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

    // Use ModalDrawerSheet or a Column with elevation/background for better Material 3 feel
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(top = statusBarPadding.calculateTopPadding())
            // Optional: Add background color matching drawer background
             .background(MaterialTheme.colorScheme.surface) // Or surfaceVariant
    ) {
        // --- Improved Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 20.dp), // Increased padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.QuestionAnswer, // Keep filled for logo
                contentDescription = stringResource(R.string.app_logo),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp) // Slightly larger icon
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.quiz), // Or App Name
                style = MaterialTheme.typography.titleLarge, // Larger title
                color = MaterialTheme.colorScheme.onSurface // Use onSurface color
            )
        }
        // Consider removing Divider or using a lighter one if needed
        // Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(12.dp)) // Use Spacer instead of Divider

        // --- Navigation Items ---
        // Use padding on each item for consistent spacing
        val itemModifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)

        NavigationDrawerItem(
            modifier = itemModifier,
            icon = { Icon(Icons.Outlined.Dashboard, contentDescription = null) }, // Outlined icon
            label = { Text(stringResource(R.string.dashboard_tab)) },
            selected = false, // Dashboard is never selected in this screen's context
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
             },
            shape = MaterialTheme.shapes.medium // Rounded shape
        )

        // --- Materials Section (Expandable with Animation) ---
        NavigationDrawerItem(
            modifier = itemModifier,
            // Use filled icon if the section is expanded, otherwise outlined
            icon = { Icon(if (materialsExpanded) Icons.Default.MenuBook else Icons.Outlined.MenuBook, contentDescription = null) },
            label = { Text(stringResource(R.string.materials_tab)) },
            badge = { // Animated badge icon
                Icon(
                    if (materialsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (materialsExpanded) stringResource(R.string.collapse) else stringResource(
                        R.string.expand
                    )
                    // Consider adding animation to the badge rotation if desired
                )
            },
            selected = false, // Header is not selectable
            onClick = { materialsExpanded = !materialsExpanded },
            shape = MaterialTheme.shapes.medium // Rounded shape
        )

        // Animated Visibility for expandable content
        AnimatedVisibility(
            visible = materialsExpanded,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            // Indent content visually using padding within this Column
            Column(modifier = Modifier.padding(start = 28.dp)) { // Indentation for sub-items
                val subItemModifier = Modifier.padding(horizontal = 0.dp, vertical = 4.dp) // Adjust padding inside

                 // Summary Item
                NavigationDrawerItem(
                    modifier = subItemModifier.fillMaxWidth(),
                    icon = { Icon(if (selectedContentIndex == 0) Icons.Default.Summarize else Icons.Outlined.Summarize, contentDescription = null) },
                    label = { Text(stringResource(R.string.summary_tab)) },
                    selected = selectedContentIndex == 0,
                    onClick = {
                         scope.launch { slidingNavState.closeMenu() } // Close drawer on click
                         onContentIndexChange(0)
                    },
                    shape = MaterialTheme.shapes.medium // Rounded shape for sub-items too
                    // Colors are often handled well by default in M3, but you can customize:
                    // colors = NavigationDrawerItemDefaults.colors(
                    //     selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    //     unselectedContainerColor = Color.Transparent, // Keep unselected transparent
                    //     selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    //     selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer
                    // )
                )
                // Questions Item
                 NavigationDrawerItem(
                    modifier = subItemModifier.fillMaxWidth(),
                    icon = { Icon(if (selectedContentIndex == 1) Icons.Default.Quiz else Icons.Outlined.Quiz, contentDescription = null) },
                    label = { Text(stringResource(R.string.questions_tab)) },
                    selected = selectedContentIndex == 1,
                    onClick = {
                        scope.launch { slidingNavState.closeMenu() }
                        onContentIndexChange(1)
                     },
                    shape = MaterialTheme.shapes.medium
                )
                // Mind Map Item
                 NavigationDrawerItem(
                    modifier = subItemModifier.fillMaxWidth(),
                    icon = { Icon(if (selectedContentIndex == 2) Icons.Default.AccountTree else Icons.Outlined.AccountTree, contentDescription = null) },
                    label = { Text(stringResource(R.string.mindmap_tab)) },
                    selected = selectedContentIndex == 2,
                    onClick = {
                        scope.launch { slidingNavState.closeMenu() }
                        onContentIndexChange(2)
                     },
                     shape = MaterialTheme.shapes.medium
                 )
                 Spacer(modifier = Modifier.height(8.dp)) // Spacing after sub-items
            }
        }

        // --- Settings Item ---
        NavigationDrawerItem(
            modifier = itemModifier,
            icon = { Icon(Icons.Outlined.Settings, contentDescription = null) }, // Outlined
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
            },
            shape = MaterialTheme.shapes.medium // Rounded shape
        )

        // Optional: Add Spacer at the bottom if needed
        Spacer(modifier = Modifier.weight(1f))
    }
}

/**
 * TopAppBar for the QuizDetailScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizDetailTopAppBar(
    slidingNavState: SlidingRootNavState,
    scope: CoroutineScope,
    scrollBehavior: TopAppBarScrollBehavior? = null
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
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        scrollBehavior = scrollBehavior
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
                            // Sử dụng EmptyStateComponent cho Summary
                            EmptyStateComponent(
                                icon = Icons.Outlined.Summarize,
                                title = stringResource(R.string.no_summary_available),
                                description = stringResource(R.string.no_summary_description)
                            )
                        }
                    }

                    // Questions Tab
                    1 -> {
                        if (quizQuestions.isNotEmpty()) {
                            when {
                                // Quiz Completed: Show Results
                                quizDetailViewModel.state.quizCompleted -> {
                                    // Fetch the answer maps from the ViewModel
                                    val userAnswersMap = quizDetailViewModel.getUserAnswersMap()
                                    val correctAnswersMap = quizDetailViewModel.getCorrectAnswersMap()

                                    QuizResultsScreen(
                                        quizQuestions = quizQuestions,
                                        correctAnswers = quizDetailViewModel.getCorrectAnswersCount(),
                                        incorrectAnswers = quizDetailViewModel.getIncorrectAnswersCount(),
                                        skippedQuestions = quizDetailViewModel.getSkippedQuestionsCount(),
                                        completionTimeSeconds = quizDetailViewModel.getQuizCompletionTimeInSeconds(),
                                        correctQuestionIndices = quizDetailViewModel.getCorrectlyAnsweredQuestions(),
                                        incorrectQuestionIndices = quizDetailViewModel.getIncorrectlyAnsweredQuestions(),
                                        skippedQuestionIndices = quizDetailViewModel.getSkippedQuestions(),
                                        // Pass the fetched maps
                                        userAnswers = userAnswersMap,
                                        correctAnswersMap = correctAnswersMap,
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
                                            // If this was the last question, immediately check completion
                                            if (currentQuestionIndex >= quizQuestions.size - 1) {
                                                quizDetailViewModel.checkQuizCompletion()
                                            }
                                            // Let LaunchedEffect handle the rest of navigation after state update
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
                            // Sử dụng EmptyStateComponent cho Questions
                            EmptyStateComponent(
                                icon = Icons.Outlined.Quiz,
                                title = stringResource(R.string.no_questions_available),
                                description = stringResource(R.string.no_questions_description)
                            )
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
                            // Sử dụng EmptyStateComponent cho Mind Map
                            EmptyStateComponent(
                                icon = Icons.Outlined.AccountTree,
                                title = stringResource(R.string.mindmap_tab),
                                description = stringResource(R.string.mindmap_generation_description),
                                actionContent = {
                                    Button(onClick = { quizDetailViewModel.generateMindMap() }) {
                                        Text(stringResource(R.string.generate_mindmap))
                                    }
                                }
                            )
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
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

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
            quizDetailViewModel.state.skippedQuestions,
            quizDetailViewModel.state.currentQuestionIndex,
            quizDetailViewModel.state.quizStarted
        ) {
            if (quizQuestions.isNotEmpty() && quizDetailViewModel.state.quizStarted) {
                answeredQuestions = quizDetailViewModel.state.answeredQuestions
                
                // Check if we just skipped a question
                val justSkipped = quizDetailViewModel.state.skippedQuestions.contains(currentQuestionIndex)
                
                // Check for quiz completion if we're on the last question and it was skipped
                // This needs to happen BEFORE we calculate nextIndexToShow
                if (justSkipped && currentQuestionIndex >= quizQuestions.size - 1) {
                    quizDetailViewModel.checkQuizCompletion()
                }
                
                // If the quiz is now completed due to skipping the last question, don't proceed with navigation
                if (quizDetailViewModel.state.quizCompleted) {
                    return@LaunchedEffect  // Exit the effect early
                }
                
                val lastAnsweredIndex =
                    quizDetailViewModel.getLastAnsweredQuestionIndex() // Get latest index from VM
                val nextIndexToShow =
                    if (justSkipped && currentQuestionIndex < quizQuestions.size - 1) {
                        // If we just skipped and not on the last question, advance to the next question
                        currentQuestionIndex + 1
                    } else if (lastAnsweredIndex >= 0 && lastAnsweredIndex < quizQuestions.size) {
                        lastAnsweredIndex 
                    } else {
                        quizDetailViewModel.state.currentQuestionIndex
                    }

                // Only update local state if it differs from ViewModel's intended index
                if (currentQuestionIndex != nextIndexToShow) {
                    currentQuestionIndex = nextIndexToShow
                    // Reset input fields when transitioning to a new question after skip
                    if (justSkipped) {
                        selectedAnswer = ""
                        showFeedback = false
                    }
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
                } else if (!justSkipped) {
                    // Reset if moving to an unanswered question (but not right after a skip since we already reset above)
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
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        // Show TopAppBar only when not loading
                        if (!quizViewModel.state.isLoading && !quizDetailViewModel.state.isLoading) {
                            QuizDetailTopAppBar(
                                slidingNavState = slidingNavState,
                                scope = scope,
                                scrollBehavior = scrollBehavior
                            )
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

