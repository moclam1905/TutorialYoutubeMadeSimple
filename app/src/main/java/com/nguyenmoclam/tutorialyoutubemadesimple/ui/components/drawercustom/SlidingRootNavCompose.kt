package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.callback.DragListener
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.callback.DragStateListener
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.model.SlideGravity
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.model.SlidingRootNavState
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.model.rememberSlidingRootNavState
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.transform.CompositeTransformation
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.transform.ElevationTransformation
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.transform.RootTransformation
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.transform.ScaleTransformation
import kotlin.math.roundToInt

/**
 * Default values for SlidingRootNav
 */
private const val DEFAULT_END_SCALE = 0.65f
private val DEFAULT_END_ELEVATION = 8.dp
private val DEFAULT_DRAG_DISTANCE = 180.dp

/**
 * A composable that provides a sliding root navigation drawer.
 *
 * @param modifier Modifier to be applied to the layout.
 * @param state The state of the sliding root navigation drawer.
 * @param menuContent The content of the menu drawer.
 * @param contentContent The main content of the screen.
 * @param dragDistance The maximum distance the content can be dragged.
 * @param gravity The gravity of the sliding menu.
 * @param transformations The transformations to apply to the content when the menu is opened.
 * @param dragListeners The listeners for drag progress changes.
 * @param dragStateListeners The listeners for drag state changes.
 */
@Composable
fun SlidingRootNav(
    modifier: Modifier = Modifier,
    state: SlidingRootNavState = rememberSlidingRootNavState(),
    menuContent: @Composable () -> Unit,
    contentContent: @Composable () -> Unit,
    dragDistance: Int = with(LocalDensity.current) { DEFAULT_DRAG_DISTANCE.toPx().roundToInt() },
    gravity: SlideGravity = SlideGravity.LEFT,
    transformations: List<RootTransformation> = emptyList(),
    dragListeners: List<DragListener> = emptyList(),
    dragStateListeners: List<DragStateListener> = emptyList()
) {
    val density = LocalDensity.current
    
    // Create default transformations if none provided
    val effectiveTransformations = remember(transformations) {
        if (transformations.isEmpty()) {
            listOf(
                ScaleTransformation(DEFAULT_END_SCALE),
                ElevationTransformation(with(density) { DEFAULT_END_ELEVATION.toPx() })
            )
        } else {
            transformations
        }
    }
    
    val compositeTransformation = remember(effectiveTransformations) {
        CompositeTransformation(effectiveTransformations)
    }
    
    // Animate drag progress changes
    val animatedDragProgress by animateFloatAsState(
        targetValue = state.dragProgress,
        animationSpec = tween(durationMillis = 300),
        label = "dragProgress"
    )
    
    // Notify drag listeners of progress changes
    LaunchedEffect(animatedDragProgress) {
        dragListeners.forEach { it.onDrag(animatedDragProgress) }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Menu content
        Box(modifier = Modifier.fillMaxSize()) {
            menuContent()
        }
        
        // Main content with transformations
        var isDragging by remember { mutableStateOf(false) }
        var initialOffset by remember { mutableStateOf(0f) }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { 
                    IntOffset(
                        x = gravity.getContentOffset(animatedDragProgress, dragDistance).roundToInt(),
                        y = 0
                    ) 
                }
                .then(compositeTransformation.transform(animatedDragProgress))
                .pointerInput(state.isMenuLocked) {
                    if (!state.isMenuLocked) {
                        detectHorizontalDragGestures(
                            onDragStart = { 
                                isDragging = true
                                initialOffset = gravity.getContentOffset(state.dragProgress, dragDistance)
                                dragStateListeners.forEach { it.onDragStart() }
                            },
                            onDragEnd = {
                                isDragging = false
                                // Snap to nearest position
                                if (state.dragProgress > 0.5f) {
                                    state.openMenu()
                                } else {
                                    state.closeMenu()
                                }
                                dragStateListeners.forEach { it.onDragEnd(state.isMenuOpened) }
                            },
                            onDragCancel = {
                                isDragging = false
                                dragStateListeners.forEach { it.onDragEnd(state.isMenuOpened) }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                if (isDragging) {
                                    // disable dragging to the right when menu is closed
                                    if (state.dragProgress == 0f && dragAmount > 0) {
                                        return@detectHorizontalDragGestures
                                    }

                                    val scaledDragAmount = dragAmount * 7f // Increase sensitivity
                                    val newOffset = initialOffset + scaledDragAmount
                                    val newProgress = gravity.calculateDragProgress(newOffset, dragDistance)
                                    state.dragProgress = newProgress.coerceIn(0f, 1f)
                                }
                            }
                        )
                    }
                }
        ) {
            contentContent()
        }
    }
}

/**
 * Extension function to open the menu with animation.
 */
fun SlidingRootNavState.openMenu(animated: Boolean = true) {
    dragProgress = 1f
}

/**
 * Extension function to close the menu with animation.
 */
fun SlidingRootNavState.closeMenu(animated: Boolean = true) {
    dragProgress = 0f
}