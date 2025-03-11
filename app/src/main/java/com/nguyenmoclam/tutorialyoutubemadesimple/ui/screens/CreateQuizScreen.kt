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
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.ErrorMessage
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.Step1Content
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.Step2Content
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.Step3Content
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.StepIndicator
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.ProcessingStep
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.SummaryViewModel

/**
 * CreateQuizScreen composable that implements a 3-step quiz creation process.
 *
 * Features:
 * - Step 1: YouTube URL input and language selection
 * - Step 2: Quiz configuration (question type and count)
 * - Step 3: Output options (summary and/or questions)
 * - Navigation between steps
 * - Progress tracking
 *
 * @param viewModel SummaryViewModel instance for managing video data and processing
 * @param navController NavHostController for handling screen navigation
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun CreateQuizScreen(
    viewModel: SummaryViewModel,
    navController: NavHostController
) {
    // State for tracking the current step in the quiz creation process
    var currentStep by remember { mutableIntStateOf(1) }
    
    // State for YouTube URL input
    var youtubeUrlValue by remember { mutableStateOf(TextFieldValue("")) }
    
    // State for language selection
    var selectedLanguage by remember { mutableStateOf("English") }
    var showLanguageDropdown by remember { mutableStateOf(false) }
    val languages = listOf("English", "Tiếng Việt", "Français", "Español", "Deutsch")
    
    // State for quiz configuration
    var questionType by remember { mutableStateOf("multiple-choice") }
    var questionCountMode by remember { mutableStateOf("auto") }
    var questionLevel by remember { mutableStateOf("medium") } // low, medium, high
    var manualQuestionCount by remember { mutableStateOf("10") }
    
    // State for output options
    var generateSummary by remember { mutableStateOf(true) }
    var generateQuestions by remember { mutableStateOf(true) }
    
    // Function to validate the current step and move to the next
    fun moveToNextStep() {
        when (currentStep) {
            1 -> {
                // Validate YouTube URL
                if (youtubeUrlValue.text.isNotBlank()) {
                    currentStep = 2
                }
            }
            2 -> {
                // Validate question count if in manual mode
                if (questionCountMode == "manual") {
                    val count = manualQuestionCount.toIntOrNull()
                    if (count != null && count in 1..50) {
                        currentStep = 3
                    }
                } else {
                    currentStep = 3
                }
            }
            3 -> {
                // Start quiz generation process
                if (generateSummary || generateQuestions) {
                    // TODO: Implement quiz generation logic
                    // For now, just navigate to result screen
                    viewModel.startSummarization(
                        youtubeUrlValue.text,
                        MainActivity.YOUTUBE_API_KEY
                    )
                    if (viewModel.errorMessage == null) {
                        navController.navigate("result")
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
                youtubeUrlValue = youtubeUrlValue,
                onYoutubeUrlChange = { youtubeUrlValue = it },
                selectedLanguage = selectedLanguage,
                onLanguageSelected = { selectedLanguage = it },
                showLanguageDropdown = showLanguageDropdown,
                onShowLanguageDropdownChange = { showLanguageDropdown = it },
                languages = languages
            )
            2 -> Step2Content(
                questionType = questionType,
                onQuestionTypeChange = { questionType = it },
                questionCountMode = questionCountMode,
                onQuestionCountModeChange = { questionCountMode = it },
                questionLevel = questionLevel,
                onQuestionLevelChange = { questionLevel = it },
                manualQuestionCount = manualQuestionCount,
                onManualQuestionCountChange = { manualQuestionCount = it }
            )
            3 -> Step3Content(
                generateSummary = generateSummary,
                onGenerateSummaryChange = { generateSummary = it },
                generateQuestions = generateQuestions,
                onGenerateQuestionsChange = { generateQuestions = it },
                isLoading = viewModel.isLoading,
                currentStep = viewModel.currentStep
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
                    1 -> youtubeUrlValue.text.isNotBlank()
                    2 -> questionCountMode != "manual" || 
                         (manualQuestionCount.toIntOrNull() != null && 
                          manualQuestionCount.toIntOrNull()!! in 1..50)
                    3 -> generateSummary || generateQuestions
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

// StepIndicator component has been moved to a separate file in the components package

// ErrorMessage component has been moved to a separate file in the components package

// Step1Content component has been moved to a separate file in the components package

// Step2Content component has been moved to a separate file in the components package

// Step3Content component has been moved to a separate file in the components package

// ActionButton component is no longer needed as the functionality is handled in the moveToNextStep function