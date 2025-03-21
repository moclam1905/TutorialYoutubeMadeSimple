package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.nguyenmoclam.tutorialyoutubemadesimple.R

/**
 * Content for Step 1: YouTube URL input and language selection.
 */
@Composable
fun Step1Content(
    youtubeUrlValue: TextFieldValue,
    onYoutubeUrlChange: (TextFieldValue) -> Unit,
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    showLanguageDropdown: Boolean,
    onShowLanguageDropdownChange: (Boolean) -> Unit,
    languages: List<String>
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // YouTube URL input
            OutlinedTextField(
                value = youtubeUrlValue,
                onValueChange = onYoutubeUrlChange,
                label = { Text(context.getString(R.string.enter_youtube_url)) },
                placeholder = { Text(context.getString(R.string.youtube_url_example)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (youtubeUrlValue.text.isNotEmpty()) {
                        IconButton(onClick = { onYoutubeUrlChange(TextFieldValue("")) }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear text")
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Language selection
            Text(context.getString(R.string.select_language), fontWeight = FontWeight.Medium)
            
            Box {
                OutlinedTextField(
                    value = selectedLanguage,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { onShowLanguageDropdownChange(!showLanguageDropdown) }) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Select language")
                        }
                    }
                )
                
                DropdownMenu(
                    expanded = showLanguageDropdown,
                    onDismissRequest = { onShowLanguageDropdownChange(false) },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    languages.forEach { language ->
                        DropdownMenuItem(
                            text = { Text(language) },
                            onClick = {
                                onLanguageSelected(language)
                                onShowLanguageDropdownChange(false)
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Language description
            Text(
                text = "Choose the language for generating content based on user selection",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}