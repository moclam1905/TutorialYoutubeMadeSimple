package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.model

/**
 * Defines the gravity of the sliding menu.
 */
enum class SlideGravity {
    /**
     * Menu slides from the left side of the screen.
     */
    LEFT,

    /**
     * Menu slides from the right side of the screen.
     */
    RIGHT;

    /**
     * Calculates the offset for the content based on the drag progress and maximum drag distance.
     *
     * @param dragProgress The current drag progress (0f to 1f).
     * @param maxDragDistance The maximum distance the content can be dragged.
     * @return The calculated offset for the content.
     */
    fun getContentOffset(dragProgress: Float, maxDragDistance: Int): Float {
        return when (this) {
            LEFT -> dragProgress * maxDragDistance
            RIGHT -> -dragProgress * maxDragDistance
        }
    }

    /**
     * Calculates the drag progress based on the current offset and maximum drag distance.
     *
     * @param currentOffset The current offset of the content.
     * @param maxDragDistance The maximum distance the content can be dragged.
     * @return The calculated drag progress (0f to 1f).
     */
    fun calculateDragProgress(currentOffset: Float, maxDragDistance: Int): Float {
        return when (this) {
            LEFT -> (currentOffset / maxDragDistance).coerceIn(0f, 1f)
            RIGHT -> (-currentOffset / maxDragDistance).coerceIn(0f, 1f)
        }
    }
}