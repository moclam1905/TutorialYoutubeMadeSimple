package com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.SubFilterChips
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

            // --- Filter Logic ---
            // Read filter state directly from ViewModel's state
            FilterTabs(
                selectedTabIndex = state.selectedMainFilterIndex,
                onTabSelected = { index, key ->
                    // Notify ViewModel about filter change, passing index and key
                    // ViewModel will handle resetting sub-filter state internally
                    viewModel.updateFilter(mainFilterKey = key, mainFilterIndex = index)
                }
            )

            // Conditionally display Sub-Filters for "Question" with animation
            AnimatedVisibility(
                visible = state.selectedMainFilter == "Question",
                enter = fadeIn(animationSpec = tween(200)) + expandVertically(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(animationSpec = tween(300))
            ) {
                SubFilterChips(
                    selectedSubFilterIndex = state.selectedSubFilterIndex,
                    onSubFilterSelected = { index, subKey ->
                        // Notify ViewModel about filter change, passing main filter key and sub-filter details
                        viewModel.updateFilter(
                            mainFilterKey = state.selectedMainFilter, // Pass current main filter key
                            mainFilterIndex = state.selectedMainFilterIndex, // Pass current main filter index
                            subFilterKey = subKey,
                            subFilterIndex = index
                        )
                    }
                )
            }
            // --- End Filter Logic ---

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
