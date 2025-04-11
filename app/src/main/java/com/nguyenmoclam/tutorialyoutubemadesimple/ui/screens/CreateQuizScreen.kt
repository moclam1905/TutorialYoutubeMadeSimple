package com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nguyenmoclam.tutorialyoutubemadesimple.MainActivity
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.navigation.AppScreens
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.ErrorMessage
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.NavigationButtons
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.Step1Content
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.Step2Content
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.Step3Content
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.StepIndicator
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizCreationViewModel
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizViewModel
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuizScreen(
    viewModel: QuizViewModel,
    navController: NavHostController,
    quizViewModel: QuizCreationViewModel,
    settingsViewModel: SettingsViewModel
) {
    val settingsState = settingsViewModel.settingsState
    // State for tracking the current step in the quiz creation process
    var currentStep by remember { mutableIntStateOf(1) }

    // State for language selection dropdown
    var showLanguageDropdown by remember { mutableStateOf(false) }
    val languages = listOf("English", "Tiếng Việt", "Français", "Español", "Deutsch")

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val errorString = stringResource(R.string.error_occurred)

    // Function to validate the current step and move to the next
    fun moveToNextStep() {
        when (currentStep) {
            1 -> {
                // Validate YouTube URL
                if (viewModel.youtubeUrl.isNotBlank()) {
                    currentStep = 2
                }
            }

            2 -> {
                // Validate question count based on mode
                if (viewModel.questionCountMode == "manual") {
                    val count = viewModel.manualQuestionCount.toIntOrNull()
                    if (count != null && count in 1..20) {
                        currentStep = 3
                    }
                } else {
                    // Auto mode - validate based on selected level
                    currentStep = 3
                }
            }

            3 -> {
                // Start quiz generation process
                if (viewModel.generateSummary || viewModel.generateQuestions) {
                    // Call createQuiz in QuizCreationViewModel
                    quizViewModel.createQuiz(
                        videoUrlOrId = viewModel.youtubeUrl,
                        youtubeApiKey = MainActivity.YOUTUBE_API_KEY,
                        generateSummary = viewModel.generateSummary,
                        generateQuestions = viewModel.generateQuestions,
                        selectedLanguage = viewModel.selectedLanguage,
                        questionType = viewModel.questionType,
                        numberOfQuestions = viewModel.numberOfQuestions,
                        transcriptMode = settingsState.transcriptMode
                    )

                    // Navigate to QuizDetailScreen if no error
                    if (quizViewModel.state.errorMessage == null) {
                        navController.navigate(AppScreens.QuizDetail.withArgs("-1"))
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                quizViewModel.state.errorMessage ?: errorString
                            )
                        }
                    }
                }
            }
        }
    }

    // Function to move to the previous step
    fun moveToPreviousStep() {
        if (currentStep > 1) {
            currentStep--
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_quiz_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },

        ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                StepIndicator(currentStep = currentStep, totalSteps = 3)
                Spacer(modifier = Modifier.height(16.dp))
                ErrorMessage(
                    errorMessage = viewModel.errorMessage,
                    isLoading = viewModel.isLoading
                )
            }

            // Content based on current step
            item {
                when (currentStep) {
                    1 -> Step1Content(
                        youtubeUrlValue = TextFieldValue(viewModel.youtubeUrl),
                        onYoutubeUrlChange = { viewModel.updateYoutubeUrl(it.text) },
                        selectedLanguage = viewModel.selectedLanguage,
                        onLanguageSelected = { viewModel.updateSelectedLanguage(it) },
                        showLanguageDropdown = showLanguageDropdown,
                        onShowLanguageDropdownChange = { showLanguageDropdown = it },
                        languages = languages
                    )

                    2 -> Step2Content(
                        questionType = viewModel.questionType,
                        onQuestionTypeChange = { viewModel.updateQuestionType(it) },
                        questionCountMode = viewModel.questionCountMode,
                        onQuestionCountModeChange = { viewModel.updateQuestionCountMode(it) },
                        questionLevel = viewModel.questionLevel,
                        onQuestionLevelChange = { viewModel.updateQuestionLevel(it) },
                        manualQuestionCount = viewModel.manualQuestionCount,
                        onManualQuestionCountChange = { viewModel.updateManualQuestionCount(it) }
                    )

                    3 -> Step3Content(
                        generateSummary = viewModel.generateSummary,
                        onGenerateSummaryChange = { viewModel.updateGenerateSummary(it) },
                        generateQuestions = viewModel.generateQuestions,
                        onGenerateQuestionsChange = { viewModel.updateGenerateQuestions(it) },
                        isLoading = viewModel.isLoading
                    )
                }
            }
            item {
                NavigationButtons(
                    currentStep = currentStep,
                    onBack = { moveToPreviousStep() },
                    onNext = { moveToNextStep() },
                    viewModel = viewModel
                )
                Spacer(
                    modifier = Modifier
                        .padding(bottom = 64.dp)
                        .navigationBarsPadding()
                )
            }
        }
    }
}

