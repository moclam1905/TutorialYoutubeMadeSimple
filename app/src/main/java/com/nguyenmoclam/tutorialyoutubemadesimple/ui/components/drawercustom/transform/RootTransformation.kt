package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.transform

import androidx.compose.ui.Modifier

/**
 * Interface for transformations that can be applied to the root content
 * based on the drag progress of the sliding drawer.
 */
fun interface RootTransformation {
    /**
     * Applies a transformation to the root content based on the drag progress.
     *
     * @param dragProgress The current drag progress (0f to 1f).
     * @return A modifier with the applied transformation.
     */
    fun transform(dragProgress: Float): Modifier
}