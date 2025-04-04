package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Component to display SVG content in SVGView in offline mode.
 * Uses custom SVGView to display stored SVG content.
 */
@Composable
fun AndroidSVGViewWithOfflineContent(
    modifier: Modifier = Modifier,
    svgContent: String
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Create SVGView
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { SVGView(context) },
            update = { view ->
                // Set SVG content
                view.setSVG(svgContent)
                isLoading = false
            }
        )
        
        // Display loading state
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
    
    // Cleanup when component is destroyed
    DisposableEffect(Unit) {
        onDispose {
            // No need to call destroy() as AndroidView will handle it automatically
        }
    }
}