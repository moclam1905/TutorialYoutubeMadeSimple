package com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.navigation.AppScreens
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.DeleteConfirmationDialog
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.FilterTabs
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.HomeTopAppBar
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.QuizListContent
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.ScreenTitle
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.SearchBar
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    navController: NavHostController,
    lazyListState: LazyListState // Accept LazyListState as parameter
) {
    val state by viewModel.state.collectAsState()
    // Trigger refresh when screen becomes active
    LaunchedEffect(Unit) {
        viewModel.refreshQuizzes()
    }

    // Delete Confirmation Dialog
    state.showDeleteConfirmDialog?.let { quizId ->
        DeleteConfirmationDialog(
            onConfirm = { viewModel.deleteQuiz(quizId) },
            onDismiss = { viewModel.hideDeleteQuizDialog() }
        )
    }

    Scaffold(
        topBar = { HomeTopAppBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 8.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp) // Consider removing if bottom nav padding is handled by Scaffold
        ) {

            ScreenTitle(titleRes = R.string.explore_challenges)

            Spacer(modifier = Modifier.height(16.dp))

            SearchBar(
                searchQuery = state.searchQuery,
                onQueryChange = viewModel::updateSearchQuery, // Use method reference
                placeholderRes = R.string.search_challenges
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Tabs - State managed locally for simplicity, extracted to FilterTabs composable
            var selectedTabIndex by remember { mutableIntStateOf(0) }
            FilterTabs(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { index -> selectedTabIndex = index }
            )

            Spacer(modifier = Modifier.height(16.dp))

            QuizListContent(
                isLoading = state.isLoading,
                isNetworkRestricted = state.networkRestricted,
                quizzes = state.quizzes,
                lazyListState = lazyListState,
                expandedStatsMap = state.expandedStatsMap,
                quizStatsCache = state.quizStatsCache,
                searchQuery = state.searchQuery,
                onToggleStats = viewModel::toggleStatsExpanded,
                onDeleteQuiz = viewModel::showDeleteQuizDialog,
                getDaysSinceLastUpdate = viewModel::getDaysSinceLastUpdate,
                onQuizClick = { quizId ->
                    navController.navigate(AppScreens.QuizDetail.withArgs(quizId.toString()))
                }
            )
        }
    }
}