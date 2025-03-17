package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.transform

import androidx.compose.ui.Modifier

/**
 * A transformation that combines multiple transformations and applies them in sequence.
 */
class CompositeTransformation(
    private val transformations: List<RootTransformation>
) : RootTransformation {

    override fun transform(dragProgress: Float): Modifier {
        var modifier: Modifier = Modifier
        transformations.forEach { transformation ->
            modifier = modifier.then(transformation.transform(dragProgress))
        }
        return modifier
    }
}