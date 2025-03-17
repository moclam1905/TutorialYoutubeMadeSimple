package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.transform

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.util.evaluate

/**
 * A transformation that translates the root content vertically based on the drag progress.
 */
class YTranslationTransformation(
    private val endTranslation: Float
) : RootTransformation {
    private val startTranslation = 0f

    override fun transform(dragProgress: Float): Modifier {
        val translation = evaluate(dragProgress, startTranslation, endTranslation)
        return Modifier.graphicsLayer(
            translationY = translation
        )
    }
}