package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.LocalNetworkUtils
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * JavaScript interface to handle saving mind map images
 */
class MindMapJavaScriptInterface(private val context: Context) {

    @JavascriptInterface
    fun saveMindMapImage(base64Image: String) {
        try {
            // Remove the data:image/png;base64, prefix
            val base64Data = base64Image.substring(base64Image.indexOf(",") + 1)

            // Convert base64 to bitmap
            val decodedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
            val bitmap =
                android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

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
            val activity = context as? androidx.activity.ComponentActivity
            activity?.runOnUiThread {
                // Request immersive fullscreen mode
                activity.window?.decorView?.systemUiVisibility = (
                        android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                                android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
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
            val activity = context as? androidx.activity.ComponentActivity
            activity?.runOnUiThread {
                // Restore normal UI visibility
                activity.window?.decorView?.systemUiVisibility = (
                        android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveBitmapToStorage(bitmap: Bitmap) {
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
fun MindMapContent(code: String) {
    val context = LocalContext.current
    val networkUtils = LocalNetworkUtils.current
    var isFullscreen by remember { mutableStateOf(false) }
    var currentZoomLevel by remember { mutableFloatStateOf(1.0f) }
    val webViewRef = remember { mutableStateOf<WebView?>(null) }

    // Generate the HTML content with Mermaid diagram
    val htmlContent = remember(code) { generateMermaidHtml(code) }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        MindMapJavaScriptInterface(context),
                        "AndroidInterface"
                    )
                }
            }
        )

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
                // Zoom out button
                FloatingActionButton(
                    onClick = {
                        webViewRef.value?.let { webView ->
                            currentZoomLevel = (currentZoomLevel - 0.25f).coerceAtLeast(0.5f)
                            webView.evaluateJavascript(
                                "javascript:setZoomLevel($currentZoomLevel);",
                                null
                            )
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        Icons.Default.ZoomOut,
                        contentDescription = stringResource(R.string.zoom_out_mindmap)
                    )
                }

                // Zoom in button
                FloatingActionButton(
                    onClick = {
                        webViewRef.value?.let { webView ->
                            currentZoomLevel = (currentZoomLevel + 0.25f).coerceAtMost(3.0f)
                            webView.evaluateJavascript(
                                "javascript:setZoomLevel($currentZoomLevel);",
                                null
                            )
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        Icons.Default.ZoomIn,
                        contentDescription = stringResource(R.string.zoom_in_mindmap)
                    )
                }

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
                    onClick = { captureAndSaveMindMap(context) }
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = stringResource(R.string.save_mindmap)
                    )
                }
            }
        }
    }
}

// Helper function to wrap Mermaid code into a full HTML page
fun generateMermaidHtml(code: String): String {
    // Embeds Mermaid.js from CDN and the mindmap code in a div
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
                    overflow: hidden; /* Prevent scrolling */
                }
                #capture-container {
                    width: 100%;
                    height: 100%;
                    padding: 10px; 
                    box-sizing: border-box;
                    overflow: visible; /* Allow content to be visible */
                    position: relative; /* Create positioning context */
                }
                .mermaid {
                    width: 100%;
                    height: 100%;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    position: absolute; /* Position absolutely */
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                }
                
                /* Ensure SVG takes full available space */
                .mermaid svg {
                    width: 100% !important;
                    height: 100% !important; /* Force full height */
                    min-height: 100vh !important; /* Use viewport height */
                    max-height: none !important; /* Remove any max height restriction */
                    display: block;
                    object-fit: contain; /* Maintain aspect ratio */
                }
               
                /* Styles for fullscreen mode */
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
              // Initialize Mermaid to render the diagram
              mermaid.initialize({ 
                startOnLoad: true,
                theme: 'default',
                securityLevel: 'loose',
                // Set diagram to fit container
                flowchart: {
                  useMaxWidth: true,
                  htmlLabels: true
                },
                mindmap: {
                  useMaxWidth: true,
                  htmlLabels: true
                }
              });
              
              // Force height recalculation after rendering
              document.addEventListener('DOMContentLoaded', function() {
                setTimeout(function() {
                  const container = document.getElementById('capture-container');
                  const mermaidDiv = container.querySelector('.mermaid');
                  const svgElement = mermaidDiv.querySelector('svg');
                  if (svgElement) {
                    svgElement.style.height = '100%';
                    svgElement.style.minHeight = '100vh';
                  }
                }, 500);
              });
              
              // Current zoom level
              let currentZoom = 1.0;
              
              // Function to set zoom level
              function setZoomLevel(zoomLevel) {
                currentZoom = zoomLevel;
                const container = document.getElementById('capture-container');
                const mermaidDiv = container.querySelector('.mermaid');
                if (mermaidDiv) {
                  mermaidDiv.style.transform = 'scale(' + zoomLevel + ')';
                  mermaidDiv.style.transformOrigin = 'top left';
                }
              }
              
              // Function to toggle fullscreen mode
              function toggleFullscreen(isFullscreen) {
                const container = document.getElementById('capture-container');
                if (isFullscreen) {
                  container.classList.add('fullscreen');
                  // Notify Android that we're in fullscreen mode
                  if (window.AndroidInterface) {
                    try {
                      window.AndroidInterface.onEnterFullscreen();
                    } catch(e) {
                      console.error('Error calling onEnterFullscreen:', e);
                    }
                  }
                } else {
                  container.classList.remove('fullscreen');
                  // Notify Android that we're exiting fullscreen mode
                  if (window.AndroidInterface) {
                    try {
                      window.AndroidInterface.onExitFullscreen();
                    } catch(e) {
                      console.error('Error calling onExitFullscreen:', e);
                    }
                  }
                }
              }
              
              // Function to capture the mind map as an image
              function captureMindMap() {
                try {
                  const container = document.getElementById('capture-container');
                  const svgElement = container.querySelector('svg');
                  
                  if (!svgElement) {
                    console.error('SVG element not found');
                    return;
                  }
                  
                  // Create a canvas with proper dimensions
                  const canvas = document.createElement('canvas');
                  const bbox = svgElement.getBBox();
                  const width = Math.max(bbox.width + 40, svgElement.width.baseVal.value);
                  const height = Math.max(bbox.height + 40, svgElement.height.baseVal.value);
                  
                  canvas.width = width;
                  canvas.height = height;
                  const ctx = canvas.getContext('2d');
                  
                  // Set white background
                  ctx.fillStyle = 'white';
                  ctx.fillRect(0, 0, width, height);
                  
                  // Convert SVG to data URL
                  const data = new XMLSerializer().serializeToString(svgElement);
                  const svgBlob = new Blob([data], {type: 'image/svg+xml;charset=utf-8'});
                  const url = URL.createObjectURL(svgBlob);
                  
                  // Create image from SVG
                  const img = new Image();
                  img.onload = function() {
                    // Draw image to canvas
                    ctx.drawImage(img, 0, 0);
                    URL.revokeObjectURL(url);
                    
                    // Convert canvas to PNG
                    const imgData = canvas.toDataURL('image/png');
                    
                    // Send to Android
                    if (window.AndroidInterface) {
                      window.AndroidInterface.saveMindMapImage(imgData);
                    }
                  };
                  img.src = url;
                } catch (e) {
                  console.error('Error capturing mind map:', e);
                }
              }
            </script>
          </body>
        </html>
    """.trimIndent()
}

// Function to trigger the JavaScript capture function
fun captureAndSaveMindMap(context: Context) {
    try {
        // Find the WebView in the current activity
        val activity = context as? androidx.activity.ComponentActivity
        val rootView = activity?.window?.decorView?.rootView
        val webView = findWebViewInView(rootView)

        if (webView != null) {
            // Execute the JavaScript function to capture the mind map
            webView.evaluateJavascript("javascript:captureMindMap();", null)
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.error_webview_not_found),
                Toast.LENGTH_SHORT
            ).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(
            context,
            context.getString(R.string.error_saving_mind_map, e.message),
            Toast.LENGTH_SHORT
        ).show()
    }
}

// Helper function to find WebView in the view hierarchy
fun findWebViewInView(view: android.view.View?): WebView? {
    if (view == null) return null
    if (view is WebView) return view
    if (view is android.view.ViewGroup) {
        for (i in 0 until view.childCount) {
            val webView = findWebViewInView(view.getChildAt(i))
            if (webView != null) return webView
        }
    }
    return null
}
