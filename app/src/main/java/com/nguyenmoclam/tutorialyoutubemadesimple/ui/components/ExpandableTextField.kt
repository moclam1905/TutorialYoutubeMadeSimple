package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components


import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nguyenmoclam.tutorialyoutubemadesimple.R

const val DEFAULT_COLLAPSED_MAX_LINES = 4 // Define a default max lines

@Composable
fun ExpandableOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    collapsedMaxLines: Int = DEFAULT_COLLAPSED_MAX_LINES
) {
    var isExpanded by remember { mutableStateOf(false) }
    // Removed textLayoutResult state
    // Determine if button should be shown based on line count and expanded state
    val showSeeMoreButton = remember(value, collapsedMaxLines, isExpanded) {
        (value.lines().size > collapsedMaxLines) || isExpanded
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(), // Animate size changes
            enabled = enabled,
            maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLines
            // Removed onTextLayout = { ... } as it's not supported and not needed anymore
        )

        if (showSeeMoreButton) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(if (isExpanded) R.string.see_less else R.string.see_more),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable(enabled = enabled) { isExpanded = !isExpanded }
                    .padding(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp) // Add padding for easier clicking
            )
        }
    }
}
