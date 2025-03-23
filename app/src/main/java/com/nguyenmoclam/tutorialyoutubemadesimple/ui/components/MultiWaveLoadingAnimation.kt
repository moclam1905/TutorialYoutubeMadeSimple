package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

@Composable
fun MultiWaveLoadingAnimation(
    progress: Float, // From 0 -> 100
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "Progress Animation"
    )
    
    // We use a "baseOffset" animation running from 0..2π
    val infiniteTransition = rememberInfiniteTransition()
    val baseOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 7000, // 8 seconds for one cycle
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    // Inside Canvas, we draw 3 wave layers + border + text
    Canvas(modifier = modifier.clip(CircleShape)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val radius = min(canvasWidth, canvasHeight) / 2f

        // Draw a light brown circular background
        drawCircle(
            color = Color(0xFF8B7355).copy(alpha = 0.1f), // Reduce alpha for lighter color
            center = center,
            radius = radius
        )

        // Draw a faded circular border
        drawCircle(
            color = Color.White.copy(alpha = 0.2f), // Reduce alpha for transparency
            center = center,
            radius = radius,
            style = Stroke(width = 1.5f) // Reduce border thickness
        )

        // Calculate water level height based on progress
        val waterHeight = canvasHeight * (animatedProgress / 100f)
        // Waves will oscillate around this point (less water => higher waveStartY)
        val waveStartY = canvasHeight - waterHeight

        // Increase amplitude & frequency
        val amplitude1 = 80f
        val amplitude2 = 30f
        val amplitude3 = 55f
        val frequency = 0.035f
        val step = 4f  // scan x in 4px increments

        // Draw 3 wave layers
        drawSinWave(
            offsetRad = baseOffset,
            frequency = frequency * 0.1f,
            amplitude = amplitude1,
            centerHeight = waveStartY,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF4EBAAA).copy(alpha = 0.7f), // Light green with alpha
                    Color(0xFF26A69A).copy(alpha = 0.8f)  // Dark green with alpha
                ),
                startY = waveStartY - 10f,
                endY = canvasHeight
            ),
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight + 15f,
            step = step * 0.1f
        )
        drawSinWave(
            offsetRad = baseOffset + (2f * PI.toFloat() / 3f),
            frequency = frequency * 0.2f,
            amplitude = amplitude2,
            centerHeight = waveStartY,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF26C6DA), // Cyan 400
                    Color(0xFF00ACC1)  // Cyan 600
                ),
                startY = waveStartY - 10f,
                endY = canvasHeight
            ),
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            step = step
        )
        drawSinWave(
            offsetRad = baseOffset + (3f * PI.toFloat() / 2f),
            frequency = frequency * 0.15f,
            amplitude = amplitude3,
            centerHeight = waveStartY,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF80DEEA).copy(alpha = 0.8f),
                    Color(0xFF4DD0E1).copy(alpha = 0.9f)
                ),
                startY = waveStartY - 10f,
                endY = canvasHeight
            ),
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight + 5f,
            step = step * 0.9f
        )

        drawIntoCanvas { canvas ->
            val text = "${progress.toInt()}%"
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = 50f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
            val xPos = center.x
            val yPos = center.y - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.nativeCanvas.drawText(text, xPos, yPos, textPaint)
        }
    }
}

/**
 * Draws a single sine wave layer, filling from the wave line to the bottom (canvasHeight).
 *
 * [offsetRad]    - initial phase (0..2π) to simulate motion.
 * [frequency]    - frequency, higher value => denser waves.
 * [amplitude]    - wave amplitude.
 * [centerHeight] - waves oscillate around this Y coordinate (usually = canvasHeight - waterHeight).
 * [color]        - wave color.
 * [canvasWidth], [canvasHeight] - canvas dimensions.
 * [step]         - X increment for each lineTo call (for "scanning" across).
 */
fun DrawScope.drawSinWave(
    offsetRad: Float,
    frequency: Float,
    amplitude: Float,
    centerHeight: Float,
    brush: Brush,
    canvasWidth: Float,
    canvasHeight: Float,
    step: Float
) {
    val path = Path()
    // Start from (x=0, y=centerHeight)
    path.moveTo(0f, centerHeight)

    var x = 0f
    while (x <= canvasWidth + step) {
        // sin(f*x + offset) oscillates around 0, we add centerHeight to shift up/down
        val y = centerHeight + amplitude * sin(frequency * x + offsetRad)
        path.lineTo(x, y)
        x += step
    }

    // Fill from wave end to bottom
    path.lineTo(canvasWidth, canvasHeight)
    path.lineTo(0f, canvasHeight)
    path.close()

    drawPath(path, brush)
}

@Preview(showBackground = true)
@Composable
fun MultiWaveLoadingAnimation() {
    MultiWaveLoadingAnimation(
        progress = 50f, // 50% water level
        modifier = Modifier.size(250.dp)
    )
}
