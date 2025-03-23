package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.transform

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.evaluate

/**
 * A transformation that scales the root content based on the drag progress.
 */
class ScaleTransformation(
    private val endScale: Float
) : RootTransformation {
    private val startScale = 1f

    override fun transform(dragProgress: Float): Modifier {
        val scale = evaluate(dragProgress, startScale, endScale)
        return Modifier.graphicsLayer(
            scaleX = scale,
            scaleY = scale
        )
    }
}