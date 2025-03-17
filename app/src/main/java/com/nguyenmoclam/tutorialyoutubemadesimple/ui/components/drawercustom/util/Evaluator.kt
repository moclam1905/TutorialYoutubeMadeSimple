package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components.drawercustom.util

/**
 * Evaluates a value between start and end based on the fraction.
 * 
 * @param fraction The fraction to evaluate (0f to 1f).
 * @param start The start value.
 * @param end The end value.
 * @return The interpolated value.
 */
fun evaluate(fraction: Float, start: Float, end: Float): Float {
    return start + (end - start) * fraction
}