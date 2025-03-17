package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.transform

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.util.evaluate

/**
 * A transformation that applies elevation to the root content based on the drag progress.
 */
class ElevationTransformation(
    private val endElevation: Float
) : RootTransformation {
    private val startElevation = 0f

    override fun transform(dragProgress: Float): Modifier {
        val elevation = evaluate(dragProgress, startElevation, endElevation)
        return Modifier.shadow(elevation.dp)
    }
}