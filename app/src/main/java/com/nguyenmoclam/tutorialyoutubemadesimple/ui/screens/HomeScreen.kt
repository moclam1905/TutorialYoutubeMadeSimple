package com.nguyenmoclam.tutorialyoutubemadesimple.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.waterfall
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
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
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.TagFilterButton
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.TagFilterSheetContent
import com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    navController: NavHostController,
    lazyListState: LazyListState // Accept LazyListState as parameter
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val modalSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true // Typically better for filter sheets
    )

    // --- Bottom Sheet Logic ---
    if (state.isTagSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleTagSheet() },
            sheetState = modalSheetState,
            windowInsets = WindowInsets.waterfall, // Resolved: Modal Bottom Sheet scrim color is not shown in status bar
            dragHandle = { null } // Remove the drag handle explicitly
        ) {
            // Content of the bottom sheet
            TagFilterSheetContent(
                allTagsWithCount = state.allTagsWithCount,
                selectedTagIds = state.selectedTagIds,
                searchQuery = state.tagSearchQuery,
                onQueryChange = viewModel::updateTagSearchQuery,
                onTagSelected = viewModel::selectTagFilter,
                onClearFilters = viewModel::clearTagFilters,
                onDismiss = { // Allow content to dismiss the sheet
                    scope.launch { modalSheetState.hide() }.invokeOnCompletion {
                        if (!modalSheetState.isVisible) {
                            viewModel.toggleTagSheet()
                        }
                    }
                }
            )
        }
    }
    // --- End Bottom Sheet Logic ---


    // Delete Confirmation Dialog
    state.showDeleteConfirmDialog?.let { quizId ->
        DeleteConfirmationDialog(
            onConfirm = { viewModel.deleteQuiz(quizId) },
            onDismiss = { viewModel.hideDeleteQuizDialog() }
        )
    }

    // Main Screen Scaffold
    Scaffold(
        topBar = { HomeTopAppBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 8.dp)
                .padding(bottom = 16.dp)
        ) {

            ScreenTitle(titleRes = R.string.explore_challenges)

            Spacer(modifier = Modifier.height(16.dp))

            // Search and Tag Filter Row with Rounded Background
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Bar - takes most space, no background
                SearchBar(
                    searchQuery = state.searchQuery,
                    onQueryChange = viewModel::updateSearchQuery,
                    placeholderRes = R.string.search_challenges,
                    modifier = Modifier.weight(1f)
                )

                // Tag Filter Button - no background
                TagFilterButton(
                    selectedTagIds = state.selectedTagIds,
                    onFilterClick = {
                        scope.launch {
                            if (modalSheetState.isVisible) {
                                modalSheetState.hide()
                            }
                            viewModel.toggleTagSheet()
                        }
                    },
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Filter Logic ---
            FilterTabs(
                selectedTabIndex = state.selectedMainFilterIndex,
                onTabSelected = { index, key ->
                    viewModel.updateFilter(mainFilterKey = key, mainFilterIndex = index)
                }
            )

            AnimatedVisibility(
                visible = state.selectedMainFilter == "Question",
                enter = fadeIn(animationSpec = tween(200)) + expandVertically(
                    animationSpec = tween(
                        300
                    )
                ),
                exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(
                    animationSpec = tween(
                        300
                    )
                )
            ) {
                SubFilterChips(
                    selectedSubFilterIndex = state.selectedSubFilterIndex,
                    onSubFilterSelected = { index, subKey ->
                        viewModel.updateFilter(
                            mainFilterKey = state.selectedMainFilter,
                            mainFilterIndex = state.selectedMainFilterIndex,
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
