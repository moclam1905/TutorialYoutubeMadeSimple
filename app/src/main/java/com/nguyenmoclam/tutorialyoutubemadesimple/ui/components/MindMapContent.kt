package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.LocalNetworkUtils
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * JavaScript interface to handle saving mind map images
 */
@Suppress("unused")
class MindMapJavaScriptInterface(
    private val context: Context,
    private val onParseResult: (Boolean, String?) -> Unit,
    private val onSVGReadyShow: (String) -> Unit
) {

    @JavascriptInterface
    fun saveMindMapImage(base64Image: String) {
        try {
            // Remove the data:image/png;base64, prefix
            val base64Data = base64Image.substring(base64Image.indexOf(",") + 1)

            // Convert base64 to bitmap
            val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
            val bitmap =
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            // Save the bitmap to storage
            saveBitmapToStorage(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                context.getString(R.string.error_saving_mind_map),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @JavascriptInterface
    fun onEnterFullscreen() {
        // This method is called from JavaScript when entering fullscreen mode
        try {
            val activity = context as? ComponentActivity
            activity?.runOnUiThread {
                // Request immersive fullscreen mode
                activity.window?.decorView?.systemUiVisibility = (
                        SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                                SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                                SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                SYSTEM_UI_FLAG_FULLSCREEN
                        )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JavascriptInterface
    fun onExitFullscreen() {
        // This method is called from JavaScript when exiting fullscreen mode
        try {
            val activity = context as? ComponentActivity
            activity?.runOnUiThread {
                // Restore normal UI visibility
                activity.window?.decorView?.systemUiVisibility = (
                        SYSTEM_UI_FLAG_LAYOUT_STABLE
                        )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JavascriptInterface
    fun onParseError(errorMsg: String) {
        try {
            (context as? ComponentActivity)?.runOnUiThread {
                onParseResult(false, errorMsg)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JavascriptInterface
    fun onParseSuccess() {
        try {
            (context as? ComponentActivity)?.runOnUiThread {
                onParseResult(true, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JavascriptInterface
    fun onSVGReady(svgString: String) {
        try {
            (context as? ComponentActivity)?.runOnUiThread {
                onSVGReadyShow(svgString)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    internal fun saveBitmapToStorage(bitmap: Bitmap) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "MindMap_$timestamp.png"
        var fos: OutputStream? = null
        var imageUri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10 and above, use MediaStore
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val contentResolver = context.contentResolver
                imageUri = contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                if (imageUri != null) {
                    fos = contentResolver.openOutputStream(imageUri)
                }
            } else {
                // For older versions, use legacy storage
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
                imageUri = Uri.fromFile(image)
            }

            if (fos != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
                fos.close()

                // Show success message
                Toast.makeText(
                    context,
                    context.getString(R.string.mind_map_saved),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_creating_file),
                    Toast.LENGTH_SHORT
                ).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                context.getString(R.string.error_saving_image, e.message),
                Toast.LENGTH_LONG
            ).show()
        } finally {
            try {
                fos?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

@Composable
fun MindMapContent(
    code: String,
    onFixCodeRequested: suspend (originalCode: String, errorMessage: String) -> String
) {
    val context = LocalContext.current
    val networkUtils = LocalNetworkUtils.current
    var isFullscreen by remember { mutableStateOf(false) }
    val webViewRef = remember { mutableStateOf<WebView?>(null) }

    var parseErrorMessage by remember { mutableStateOf<String?>(null) }
    var currentCode by remember { mutableStateOf(code) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Field for SVG
    var svgString by remember { mutableStateOf<String?>(null) }
    var isUsingSVG by remember { mutableStateOf(false) }

    LaunchedEffect(code) {
        if (code != currentCode) {
            currentCode = code
        }
    }

    // Generate the HTML content with Mermaid diagram
    val htmlContent = remember(currentCode) { generateMermaidHtml(currentCode) }

    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(16.dp)
        ) { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                containerColor = if (parseErrorMessage != null) {
                    Color(0xFFBA1A1A) // Error color
                } else {
                    Color(0xFF386A20) // Success color
                }
            )
        }
        if (isUsingSVG && svgString != null) {
            AndroidView(
                factory = { context ->
                    SVGView(context).apply {
                        tag = "svg_view"
                    }
                },
                update = { view -> view.setSVG(svgString!!) },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            NetworkAwareWebView(
                url = "",  // Empty URL since we're using HTML content directly
                html = htmlContent,
                networkUtils = networkUtils,
                isJavaScriptEnabled = true,
                onPageFinished = {},  // no-op or handle if needed
                jsInterface = { webView ->
                    webView.apply {
                        // Enable zoom support
                        settings.setSupportZoom(true)
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false // Hide default zoom controls

                        // Save reference to WebView for later use
                        webViewRef.value = this

                        // Add JavaScript interface
                        addJavascriptInterface(
                            MindMapJavaScriptInterface(
                                context,
                                { isSuccess, errorMsg ->
                                    parseErrorMessage = if (isSuccess) null else errorMsg

                                    // Show Snackbar notification
                                    scope.launch {
                                        if (isSuccess) {
                                            snackbarHostState.showSnackbar(
                                                message = context.getString(R.string.mind_map_created_successfully),
                                                duration = SnackbarDuration.Short
                                            )
                                            webView.evaluateJavascript(
                                                "javascript:getSVGContent();",
                                                null
                                            )
                                        } else {
                                            snackbarHostState.showSnackbar(
                                                message = errorMsg
                                                    ?: context.getString(R.string.error_occurred),
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    }
                                },
                                { svg ->
                                    svgString = svg
                                    isUsingSVG = true
                                }
                            ),
                            "AndroidInterface"
                        )
                    }
                }
            )
        }

        // Action buttons
        if (isFullscreen) {
            // Exit fullscreen button when in fullscreen mode
            FloatingActionButton(
                onClick = {
                    isFullscreen = false
                    webViewRef.value?.evaluateJavascript(
                        "javascript:toggleFullscreen(false);",
                        null
                    )
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.FullscreenExit,
                    contentDescription = stringResource(R.string.exit_fullscreen_mindmap)
                )
            }
        } else {
            // Button row when not in fullscreen mode
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                // Fullscreen button
                FloatingActionButton(
                    onClick = {
                        isFullscreen = true
                        webViewRef.value?.evaluateJavascript(
                            "javascript:toggleFullscreen(true);",
                            null
                        )
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Fullscreen,
                        contentDescription = stringResource(R.string.fullscreen_mindmap)
                    )
                }

                // Save button
                FloatingActionButton(
                    onClick = {
                        if (isUsingSVG) {
                            // Get SVGView reference directly from composable
                            val svgView =
                                (context as? ComponentActivity)?.window?.decorView?.rootView
                                    ?.findViewWithTag<SVGView>("svg_view")
                            svgView?.saveAsBitmap()?.let { bitmap ->
                                MindMapJavaScriptInterface(
                                    context,
                                    { _, _ -> },
                                    { _ -> }).saveBitmapToStorage(bitmap)
                            }
                        } else {
                            // Use existing JavaScript method for WebView
                            webViewRef.value?.evaluateJavascript(
                                "javascript:captureMindMap();",
                                null
                            )
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = stringResource(R.string.save_mindmap)
                    )
                }

                val infiniteTransition =
                    rememberInfiniteTransition(label = "RefreshButtonAnimation")
                val scale = if (parseErrorMessage != null) {
                    // Animation for the refresh button when there is an error
                    infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 800),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "ScaleAnimation"
                    ).value
                } else {
                    1f
                }

                FloatingActionButton(
                    onClick = {
                        val error = parseErrorMessage
                        if (error != null) {
                            scope.launch {
                                val fixedCode = onFixCodeRequested(currentCode, error)
                                currentCode = fixedCode
                            }
                        }
                    },

                    // Material3 FAB don't have enabled,
                    // so we block onClick and/or change color/alpha for "disable" shadow
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .let {
                            if (parseErrorMessage == null) {
                                it.alpha(0.3f)
                            } else {
                                it
                            }
                        }
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Fix code with LLM"
                    )
                }
            }
        }
    }
}

// Helper function to wrap Mermaid code into a full HTML page
fun generateMermaidHtml(code: String): String {
    return """
        <html>
          <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <script src="https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js"></script>
            <style>
                html, body {
                    margin: 0;
                    padding: 0;
                    width: 100%;
                    height: 100%;
                    box-sizing: border-box;
                    overflow: hidden;
                }
                #capture-container {
                    width: 100%;
                    height: 100%;
                    padding: 10px;
                    box-sizing: border-box;
                    overflow: visible;
                    position: relative;
                    cursor: grab;
                }
                #capture-container:active {
                    cursor: grabbing;
                }
                .mermaid {
                    width: 100%;
                    height: 100%;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                }
                .mermaid svg {
                    width: 100% !important;
                    height: 100% !important;
                    min-height: 100vh !important;
                    max-height: none !important;
                    display: block;
                    object-fit: contain;
                }
                .fullscreen {
                    position: fixed;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    z-index: 9999;
                    background-color: white;
                    padding: 20px;
                    box-sizing: border-box;
                }
            </style>
          </head>
          <body>
            <div id="capture-container">
              <div class="mermaid">
${code.trimIndent()}
              </div>
            </div>
            <script>
              // Initialize Mermaid with startOnLoad: false
              mermaid.initialize({
                startOnLoad: false,
                theme: 'default',
                securityLevel: 'loose',
                flowchart: {
                  useMaxWidth: true,
                  htmlLabels: true
                },
                mindmap: {
                  useMaxWidth: true,
                  htmlLabels: true
                }
              });

              // Wait until Mermaid is ready
              function waitUntilMermaidReady() {
                return new Promise(resolve => {
                  if (window.mermaid) {
                    resolve();
                  } else {
                    setTimeout(() => waitUntilMermaidReady().then(() => resolve()), 100);
                  }
                });
              }

              // Parsing and rendering logic
              document.addEventListener('DOMContentLoaded', async function() {
                await waitUntilMermaidReady();
                const mindMapDiv = document.querySelector('.mermaid');
                const code = mindMapDiv.innerHTML.trim();
                try {
                  try {
                    const parseResult = await Promise.resolve(mermaid.parse(code));
                    
                    if (parseResult && typeof parseResult === 'object' && parseResult.hasOwnProperty('error')) {
                      throw new Error(parseResult.error || 'Invalid Mermaid syntax');
                    }
                   
                    try {
                      window.mermaid.init();
                      window.AndroidInterface.onParseSuccess();
                      
                      getSVGContent();
                      
                    } catch (renderError) {
                      window.AndroidInterface.onParseError(renderError.message || 'Error rendering diagram');
                      throw renderError;
                    }
                  } catch (parseError) {
                    window.AndroidInterface.onParseError(parseError.message || 'Invalid Mermaid syntax');
                    throw parseError;
                  }
                } catch (error) {
                  window.AndroidInterface.onParseError(error.message);
                }
              });

              // Function to toggle fullscreen mode
              function toggleFullscreen(isFullscreen) {
                const container = document.getElementById('capture-container');
                if (isFullscreen) {
                  container.classList.add('fullscreen');
                  if (window.AndroidInterface) {
                    try {
                      window.AndroidInterface.onEnterFullscreen();
                    } catch(e) {
                      console.error('Error calling onEnterFullscreen:', e);
                    }
                  }
                } else {
                  container.classList.remove('fullscreen');
                  if (window.AndroidInterface) {
                    try {
                      window.AndroidInterface.onExitFullscreen();
                    } catch(e) {
                      console.error('Error calling onExitFullscreen:', e);
                    }
                  }
                }
              }
            
              function getSVGContent() {
                const svgElement = document.querySelector('.mermaid svg');
                if (svgElement) {
                  const serializer = new XMLSerializer();
                  const svgString = serializer.serializeToString(svgElement);
                  window.AndroidInterface.onSVGReady(svgString);
                } else {
                  console.error('SVG element not found');
                }
              }
            </script>
          </body>
        </html>
    """.trimIndent()
}
