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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
                .navigationBarsPadding()
                .padding(bottom = 16.dp) // Consider removing if bottom nav padding is handled by Scaffold
        ) {

            ScreenTitle(titleRes = R.string.explore_challenges)

            Spacer(modifier = Modifier.height(16.dp))

            // Search and Tag Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), // Add horizontal padding to the Row
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Bar takes most of the space
                SearchBar(
                    searchQuery = state.searchQuery,
                    onQueryChange = viewModel::updateSearchQuery,
                    placeholderRes = R.string.search_challenges,
                    modifier = Modifier.weight(1f) // Re-apply weight modifier
                )

                Spacer(modifier = Modifier.width(8.dp)) // Add space between search and button

                // Tag Filter Button
                TagFilterButton(
                    selectedTagIds = state.selectedTagIds,
                    onFilterClick = {
                        scope.launch {
                            // Ensure sheet state is managed correctly even if already visible
                            if (modalSheetState.isVisible) {
                                modalSheetState.hide() // Hide first if somehow stuck open
                            }
                            viewModel.toggleTagSheet() // Update VM state *before* showing
                        }
                    }
                    // No modifier needed here unless specific alignment/padding required
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
