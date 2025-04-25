package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.tag.TagWithCount

/**
 * A component that displays a filter icon button with a badge when tags are selected.
 * When clicked, it triggers showing the tag filter bottom sheet.
 */
@Composable
fun TagFilterButton(
    selectedTagIds: Set<Long>,
    onFilterClick: () -> Unit, // Renamed for clarity: triggers showing the sheet
    modifier: Modifier = Modifier
) {
    BadgedBox(
        badge = {
            if (selectedTagIds.isNotEmpty()) {
                Badge(modifier = Modifier.offset(x = (-4).dp, y = 4.dp))
            }
        },
        modifier = modifier
    ) {
        // IconButton itself doesn't have a background, but ensure its container (BadgedBox) doesn't add one
        // We might need to adjust padding or size if alignment is off after removing SearchBar background
        IconButton(onClick = onFilterClick, modifier = Modifier.size(40.dp)) { // Adjust size if needed for alignment
            Icon(
                imageVector = Icons.Outlined.FilterAlt,
                contentDescription = stringResource(R.string.filter_by_tags),
                // Keep the tint logic, but ensure the 'inactive' state matches the SearchBar icon
                tint = if (selectedTagIds.isNotEmpty()) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant // Match SearchBar icon tint
                }
            )
        }
    }
}

/**
 * A composable function representing the content of the Tag Filter Bottom Sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagFilterSheetContent(
    allTagsWithCount: List<TagWithCount>,
    selectedTagIds: Set<Long>,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onTagSelected: (Long) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit // Added to allow closing the sheet from within
) {
    // Filter tags based on search query
    val filteredTags = remember(allTagsWithCount, searchQuery) {
        if (searchQuery.isBlank()) {
            allTagsWithCount
        } else {
            allTagsWithCount.filter { it.tag.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding() // Add padding for navigation bar
            .imePadding() // Add padding for keyboard
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.tag_filter_dialog_title),
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close_menu))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_tags_placeholder)) }, // Use new placeholder string
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) { // Clear search
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.clear_search)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tag List
        if (filteredTags.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (searchQuery.isBlank()) stringResource(R.string.no_tags_available)
                    else stringResource(R.string.tag_filter_no_results),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(
                    1f,
                    fill = false
                )
            ) { // Allow list to scroll, but don't take all space initially
                items(items = filteredTags, key = { it.tag.id }) { tagWithCount ->
                    val isSelected = selectedTagIds.contains(tagWithCount.tag.id)
                    ListItem(
                        headlineContent = { Text(tagWithCount.tag.name) },
                        supportingContent = { Text("(${tagWithCount.quizCount})") },
                        leadingContent = {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onTagSelected(tagWithCount.tag.id) }
                            )
                        },
                        modifier = Modifier.clickable { onTagSelected(tagWithCount.tag.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Clear Filters Button (only show if filters are active)
        if (selectedTagIds.isNotEmpty()) {
            Button(
                onClick = {
                    onClearFilters()
                    // Optionally dismiss after clearing, or keep it open
                    // onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.clear_tag_filters))
            }
        }
    }
}
