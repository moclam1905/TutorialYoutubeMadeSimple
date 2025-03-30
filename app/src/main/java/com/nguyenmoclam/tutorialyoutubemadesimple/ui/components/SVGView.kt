package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.Gravity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.VelocityTracker
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withMatrix
import com.caverock.androidsvg.SVG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * Display and interact with SVG in Android applications.
 * Supports operations such as zoom, pan, and saving SVG as bitmap.
 */
class SVGView(context: Context) : FrameLayout(context) {
    // SVG and display state
    private var svg: SVG? = null
    private val matrix = Matrix() // Manages transformations
    private var cachedBitmap: Bitmap? = null
    private var lastSvgString: String? = null
    private var isDrawing = false
    private var isProcessing = false

    // Constants for zoom
    private companion object {
        private const val MIN_SCALE = 0.1f
        private const val MAX_SCALE = 5f
        private const val FLING_COOLDOWN = 500L // Wait time between fling events
        private const val FLING_VELOCITY_THRESHOLD =
            1000f // Minimum velocity threshold to activate fling
        private const val DRAG_THRESHOLD = 10f // Minimum distance threshold to start dragging
        private const val MAX_VELOCITY = 5000f // Maximum velocity limit
        private const val DAMPING_FACTOR = 0.15f // Damping factor for smooth motion
        private const val DISTANCE_TIME_FACTOR = 0.03f // Factor to limit excessive movement
        private const val FLING_DURATION = 300L // Duration of fling animation
        private const val PROGRESS_DELAY = 100L // Delay before hiding ProgressBar
    }

    // Touch and drag state
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var startDragX = 0f
    private var startDragY = 0f
    private var isDragging = false
    private var currentScale = 1f
    private var lastFlingTime = 0L

    // Gesture handling components
    private val velocityTracker = VelocityTracker.obtain()
    private val gestureDetector = GestureDetector(context, GestureListener())
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())

    // Multi-threading handling
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    // ProgressBar displayed during processing
    private val progressBar: ProgressBar = ProgressBar(context).apply {
        val size = context.resources.getDimensionPixelSize(android.R.dimen.app_icon_size)
        val layoutParams = FrameLayout.LayoutParams(size, size).apply {
            gravity = Gravity.CENTER
        }
        this.layoutParams = layoutParams
        elevation = 8f
        visibility = GONE
    }

    init {
        addView(progressBar)
    }

    /**
     * Set SVG from SVG string
     * @param svgString SVG string to display
     */
    fun setSVG(svgString: String) {
        try {
            // Check if new SVG is the same as old SVG
            if (svgString == lastSvgString) return
            lastSvgString = svgString

            // Show progress bar
            isProcessing = true
            showProgressBar()

            // Process SVG in background thread
            processSvgInBackground(svgString)
        } catch (e: Exception) {
            handleException(e)
        }
    }

    /**
     * Process SVG in background thread
     */
    private fun processSvgInBackground(svgString: String) {
        ioScope.launch {
            try {
                // Release old bitmap
                cachedBitmap?.recycle()
                cachedBitmap = null

                // Parse SVG in background thread
                val newSvg = SVG.getFromString(svgString)

                // Update UI in main thread
                updateSvgOnMainThread(newSvg)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    handleException(e)
                }
            }
        }
    }

    /**
     * Update SVG in main thread
     */
    private suspend fun updateSvgOnMainThread(newSvg: SVG) {
        withContext(Dispatchers.Main) {
            svg = newSvg
            resetTransformation()
            isProcessing = false
            hideProgressBar()
            invalidate()
        }
    }

    /**
     * Reset transformation parameters
     */
    private fun resetTransformation() {
        matrix.reset()
        currentScale = 1f
    }

    /**
     * Handle zoom events
     */
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            val newScale = currentScale * scaleFactor

            // Limit zoom ratio
            if (newScale in MIN_SCALE..MAX_SCALE) {
                currentScale = newScale
                // Zoom at focus point
                matrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
                invalidate()
            }
            return true
        }
    }

    /**
     * Handle complex gestures
     */
    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            // Check wait time between fling events
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastFlingTime < FLING_COOLDOWN) return false

            // Check minimum velocity threshold
            if (abs(velocityX) < FLING_VELOCITY_THRESHOLD * 0.5f &&
                abs(velocityY) < FLING_VELOCITY_THRESHOLD * 0.5f
            ) return false

            lastFlingTime = currentTime

            // Check if fling velocity is too fast
            if (abs(velocityX) > MAX_VELOCITY || abs(velocityY) > MAX_VELOCITY) {
                return false
            }

            // Calculate maximum movement distance
            return performFlingAnimation(e2, velocityX, velocityY)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            // Reset to initial state
            resetTransformation()
            invalidate()
            return true
        }
    }

    /**
     * Perform fling animation
     */
    private fun performFlingAnimation(
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        // Calculate maximum movement distance
        val maxDistance = width.coerceAtLeast(height) * 0.2f
        val rawDistanceX = velocityX * DISTANCE_TIME_FACTOR
        val rawDistanceY = velocityY * DISTANCE_TIME_FACTOR

        // Apply stricter movement distance limits
        val limitedDistanceX = rawDistanceX.coerceIn(-maxDistance, maxDistance)
        val limitedDistanceY = rawDistanceY.coerceIn(-maxDistance, maxDistance)

        val startX = e2.x
        val startY = e2.y
        val endX = startX + limitedDistanceX
        val endY = startY + limitedDistanceY

        post {
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = FLING_DURATION
                interpolator = DecelerateInterpolator()
                addUpdateListener { animation ->
                    val progress = animation.animatedValue as Float
                    val currentX = startX + (endX - startX) * progress
                    val currentY = startY + (endY - startY) * progress
                    matrix.postTranslate(currentX - startX, currentY - startY)
                    invalidate()
                }
                start()
            }
        }
        return true
    }

    /**
     * Handle touch events
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        velocityTracker.addMovement(event) // Track movement velocity
        gestureDetector.onTouchEvent(event)
        scaleDetector.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> handleActionDown(event)
            MotionEvent.ACTION_MOVE -> handleActionMove(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> handleActionUpOrCancel()
        }
        return true
    }

    /**
     * Handle ACTION_DOWN event
     */
    private fun handleActionDown(event: MotionEvent) {
        parent?.requestDisallowInterceptTouchEvent(true)
        lastTouchX = event.x
        lastTouchY = event.y
        startDragX = event.x
        startDragY = event.y
        isDragging = false
    }

    /**
     * Handle ACTION_MOVE event
     */
    private fun handleActionMove(event: MotionEvent) {
        if (scaleDetector.isInProgress) return

        val dx = event.x - lastTouchX
        val dy = event.y - lastTouchY

        // Check drag threshold
        if (!isDragging) {
            val totalDragX = event.x - startDragX
            val totalDragY = event.y - startDragY
            if (abs(totalDragX) > DRAG_THRESHOLD || abs(totalDragY) > DRAG_THRESHOLD) {
                isDragging = true
            }
        }

        if (isDragging) {
            // Calculate movement velocity
            velocityTracker.computeCurrentVelocity(1000)
            val velocityX = velocityTracker.xVelocity
            val velocityY = velocityTracker.yVelocity

            // Check if fling velocity is too fast
            if (abs(velocityX) > MAX_VELOCITY || abs(velocityY) > MAX_VELOCITY) {
                return
            }

            // Apply smooth motion based on velocity with stronger damping factor
            val smoothDx = dx * (1 + abs(velocityX) / 12000) * DAMPING_FACTOR
            val smoothDy = dy * (1 + abs(velocityY) / 12000) * DAMPING_FACTOR

            matrix.postTranslate(smoothDx, smoothDy)
            lastTouchX = event.x
            lastTouchY = event.y
            invalidate()
        }
    }

    /**
     * Handle ACTION_UP or ACTION_CANCEL event
     */
    private fun handleActionUpOrCancel() {
        isDragging = false
        parent?.requestDisallowInterceptTouchEvent(false)
        velocityTracker.clear() // Reset velocity tracker
    }

    /**
     * Draw SVG with Matrix
     */
    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (isDrawing || svg == null) return

        try {
            isDrawing = true

            // Use bitmap cache if available
            if (cachedBitmap == null) {
                // If bitmap cache doesn't exist, create it in background thread
                if (!isProcessing) {
                    createBitmapInBackground()
                }
            } else {
                // Draw bitmap with matrix
                drawCachedBitmap(canvas)
            }
        } finally {
            isDrawing = false
        }
    }

    /**
     * Create bitmap in background thread
     */
    private fun createBitmapInBackground() {
        isProcessing = true
        showProgressBar()

        ioScope.launch {
            try {
                val bitmap = createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1))
                val bitmapCanvas = Canvas(bitmap)
                svg?.renderToCanvas(bitmapCanvas)

                // Update UI on the main thread
                withContext(Dispatchers.Main) {
                    cachedBitmap = bitmap
                    isProcessing = false
                    hideProgressBar()
                    invalidate() // Redraw with created bitmap
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    handleException(e)
                }
            }
        }
    }

    /**
     * Draw cached bitmap
     */
    private fun drawCachedBitmap(canvas: Canvas) {
        canvas.withMatrix(matrix) {
            cachedBitmap?.let { drawBitmap(it, 0f, 0f, null) }
        }
    }

    /**
     * Save SVG as bitmap
     * @return Bitmap or null if error occurs
     */
    fun saveAsBitmap(): Bitmap? {
        val svg = this.svg ?: return null
        try {
            // Reuse bitmap cache if available
            if (cachedBitmap != null) {
                return createBitmapFromCached()
            }

            // Create new bitmap if no cache exists
            return createBitmapFromSvg(svg)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Create bitmap from cached bitmap
     */
    private fun createBitmapFromCached(): Bitmap {
        val resultBitmap = createBitmap(width, height)
        val canvas = Canvas(resultBitmap)
        canvas.concat(matrix)
        canvas.drawBitmap(cachedBitmap!!, 0f, 0f, null)
        return resultBitmap
    }

    /**
     * Create bitmap directly from SVG
     */
    private fun createBitmapFromSvg(svg: SVG): Bitmap {
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        canvas.concat(matrix)
        svg.renderToCanvas(canvas)
        return bitmap
    }

    /**
     * Clean up resources when view is destroyed
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cleanupResources()
    }

    /**
     * Clean up resources
     */
    private fun cleanupResources() {
        velocityTracker.recycle()
        cachedBitmap?.recycle()
        cachedBitmap = null
        svg = null
        lastSvgString = null
        coroutineScope.cancel() // Cancel all coroutines in main scope
        ioScope.cancel() // Cancel all coroutines in IO scope
        System.gc() // Suggest GC to clean up
    }

    /**
     * Handle exception
     */
    private fun handleException(e: Exception) {
        isProcessing = false
        hideProgressBar()
        e.printStackTrace()
    }

    /**
     * Show progress bar
     */
    private fun showProgressBar() {
        // Sử dụng coroutineScope đã có sẵn Dispatchers.Main
        coroutineScope.launch {
            progressBar.visibility = VISIBLE
            progressBar.bringToFront()
        }
    }

    /**
     * Hide progress bar
     */
    private fun hideProgressBar() {
        // Add small delay to ensure ProgressBar is displayed long enough
        coroutineScope.launch {
            kotlinx.coroutines.delay(PROGRESS_DELAY)
            if (!isProcessing) {
                progressBar.visibility = GONE
            }
        }
    }
}