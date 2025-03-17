package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * State holder for the SlidingRootNav Compose implementation.
 * Manages the drawer state and provides methods to control it.
 */
@Stable
class SlidingRootNavState(
    initialDragProgress: Float = 0f,
    initialIsMenuLocked: Boolean = false,
    initialIsContentClickableWhenMenuOpened: Boolean = true
) {
    /**
     * Current drag progress of the drawer.
     * 0f means fully closed, 1f means fully opened.
     */
    var dragProgress by mutableStateOf(initialDragProgress)
        internal set

    /**
     * Whether the menu is locked (cannot be dragged).
     */
    var isMenuLocked by mutableStateOf(initialIsMenuLocked)
        internal set

    /**
     * Whether the content is clickable when the menu is opened.
     */
    var isContentClickableWhenMenuOpened by mutableStateOf(initialIsContentClickableWhenMenuOpened)
        internal set

    /**
     * Whether the menu is currently closed.
     */
    val isMenuClosed: Boolean
        get() = dragProgress == 0f

    /**
     * Whether the menu is currently opened.
     */
    val isMenuOpened: Boolean
        get() = dragProgress == 1f

    /**
     * Opens the menu with animation.
     */
    fun openMenu() {
        dragProgress = 1f
    }

    /**
     * Closes the menu with animation.
     */
    fun closeMenu() {
        dragProgress = 0f
    }

    /**
     * Sets whether the menu is locked.
     */
    fun setMenuLocked(locked: Boolean) {
        isMenuLocked = locked
    }
}

/**
 * Creates and remembers a [SlidingRootNavState].
 */
@Composable
fun rememberSlidingRootNavState(
    initialDragProgress: Float = 0f,
    initialIsMenuLocked: Boolean = false,
    initialIsContentClickableWhenMenuOpened: Boolean = true
): SlidingRootNavState {
    return remember {
        SlidingRootNavState(
            initialDragProgress = initialDragProgress,
            initialIsMenuLocked = initialIsMenuLocked,
            initialIsContentClickableWhenMenuOpened = initialIsContentClickableWhenMenuOpened
        )
    }
}