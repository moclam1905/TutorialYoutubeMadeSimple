package com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nguyenmoclam.tutorialyoutubemadesimple.MainActivity
import com.nguyenmoclam.tutorialyoutubemadesimple.navigation.AppScreens
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.ErrorMessage
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.Step1Content
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.Step2Content
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.Step3Content
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.StepIndicator
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizCreationViewModel
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.QuizViewModel

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun CreateQuizScreen(
    viewModel: QuizViewModel,
    navController: NavHostController,
    quizViewModel: QuizCreationViewModel
) {
    // State for tracking the current step in the quiz creation process
    var currentStep by remember { mutableIntStateOf(1) }
    
    // State for language selection dropdown
    var showLanguageDropdown by remember { mutableStateOf(false) }
    val languages = listOf("English", "Tiếng Việt", "Français", "Español", "Deutsch")
    
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
                        numberOfQuestions = viewModel.numberOfQuestions
                    )
                    
                    // Navigate to QuizDetailScreen if no error
                    if (quizViewModel.state.errorMessage == null) {
                        navController.navigate(AppScreens.QuizDetail.withArgs("-1"))
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
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Step indicator
        StepIndicator(currentStep = currentStep, totalSteps = 3)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Error message display
        ErrorMessage(viewModel)
        
        // Content based on current step
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
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back button (hidden on first step)
            if (currentStep > 1) {
                Button(
                    onClick = { moveToPreviousStep() },
                    modifier = Modifier.weight(1f),
                    enabled = !viewModel.isLoading
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Back")
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            // Next/Create button
            Button(
                onClick = { moveToNextStep() },
                modifier = Modifier.weight(1f),
                enabled = !viewModel.isLoading && when (currentStep) {
                    1 -> viewModel.youtubeUrl.isNotBlank()
                    2 -> viewModel.questionCountMode != "manual" || 
                         (viewModel.manualQuestionCount.toIntOrNull() != null && 
                          viewModel.manualQuestionCount.toIntOrNull()!! in 1..20)
                    3 -> viewModel.generateSummary || viewModel.generateQuestions
                    else -> false
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (currentStep < 3) "Next" else "Create Quiz")
                    if (currentStep < 3) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.ArrowForward, contentDescription = "Next")
                    }
                }
            }
        }
    }
}
