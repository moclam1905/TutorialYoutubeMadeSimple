package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.callback.DragListener
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.callback.DragStateListener
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.model.SlideGravity
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.model.rememberSlidingRootNavState
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.transform.ElevationTransformation
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.transform.RootTransformation
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.transform.ScaleTransformation
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.transform.YTranslationTransformation
import kotlin.math.roundToInt

/**
 * Builder class for creating a SlidingRootNav composable.
 */
class SlidingRootNavBuilder {
    private var dragDistance: Int? = null
    private var gravity: SlideGravity = SlideGravity.LEFT
    private var isMenuOpened: Boolean = false
    private var isMenuLocked: Boolean = false
    private var isContentClickableWhenMenuOpened: Boolean = true
    private val transformations: MutableList<RootTransformation> = mutableListOf()
    private val dragListeners: MutableList<DragListener> = mutableListOf()
    private val dragStateListeners: MutableList<DragStateListener> = mutableListOf()

    /**
     * Sets the gravity of the sliding menu.
     */
    fun withGravity(gravity: SlideGravity): SlidingRootNavBuilder {
        this.gravity = gravity
        return this
    }

    /**
     * Sets whether the menu is locked (cannot be dragged).
     */
    fun withMenuLocked(locked: Boolean): SlidingRootNavBuilder {
        isMenuLocked = locked
        return this
    }

    /**
     * Sets whether the menu is opened initially.
     */
    fun withMenuOpened(opened: Boolean): SlidingRootNavBuilder {
        isMenuOpened = opened
        return this
    }

    /**
     * Sets whether the content is clickable when the menu is opened.
     */
    fun withContentClickableWhenMenuOpened(clickable: Boolean): SlidingRootNavBuilder {
        isContentClickableWhenMenuOpened = clickable
        return this
    }

    /**
     * Sets the drag distance in dp.
     */
    fun withDragDistance(dp: Int): SlidingRootNavBuilder {
        dragDistance = dp
        return this
    }

    /**
     * Adds a scale transformation to the root view.
     */
    fun withRootViewScale(scale: Float): SlidingRootNavBuilder {
        transformations.add(ScaleTransformation(scale))
        return this
    }

    /**
     * Adds an elevation transformation to the root view.
     */
    fun withRootViewElevation(elevation: Int): SlidingRootNavBuilder {
        transformations.add(ElevationTransformation(elevation.dp.value))
        return this
    }

    /**
     * Adds a Y-translation transformation to the root view.
     */
    fun withRootViewYTranslation(translation: Int): SlidingRootNavBuilder {
        transformations.add(YTranslationTransformation(translation.dp.value))
        return this
    }

    /**
     * Adds a custom transformation to the root view.
     */
    fun addRootTransformation(transformation: RootTransformation): SlidingRootNavBuilder {
        transformations.add(transformation)
        return this
    }

    /**
     * Adds a drag listener.
     */
    fun addDragListener(dragListener: DragListener): SlidingRootNavBuilder {
        dragListeners.add(dragListener)
        return this
    }

    /**
     * Adds a drag state listener.
     */
    fun addDragStateListener(dragStateListener: DragStateListener): SlidingRootNavBuilder {
        dragStateListeners.add(dragStateListener)
        return this
    }

    /**
     * Creates a SlidingRootNav composable with the configured parameters.
     */
    @Composable
    fun build(
        menuContent: @Composable () -> Unit,
        contentContent: @Composable () -> Unit
    ) {
        val state = rememberSlidingRootNavState(
            initialDragProgress = if (isMenuOpened) 1f else 0f,
            initialIsMenuLocked = isMenuLocked,
            initialIsContentClickableWhenMenuOpened = isContentClickableWhenMenuOpened
        )

        val effectiveDragDistance = dragDistance?.let { dp ->
            with(LocalDensity.current) { dp.dp.toPx().roundToInt() }
        }

        SlidingRootNav(
            state = state,
            menuContent = menuContent,
            contentContent = contentContent,
            dragDistance = effectiveDragDistance ?: with(LocalDensity.current) {
                180.dp.toPx().roundToInt()
            },
            gravity = gravity,
            transformations = transformations,
            dragListeners = dragListeners,
            dragStateListeners = dragStateListeners
        )
    }
}