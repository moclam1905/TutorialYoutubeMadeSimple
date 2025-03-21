package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Data class representing a navigation item in the bottom navigation bar
 */
@Immutable
data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

/**
 * A custom curved bottom navigation bar with smooth animations
 *
 * @param items List of navigation items to display
 * @param selectedItemIndex Index of the currently selected item
 * @param onItemSelected Callback when an item is selected
 */

/**
 * Simple data class to represent a point with x and y coordinates
 */
@Stable
data class PointF(val x: Float, val y: Float)

@Composable
fun CurvedBottomNavigation(
    items: List<NavItem>,
    selectedItemIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    // Constants for dimensions defined as constants to avoid recreation
    val totalHeight = remember { 80.dp }
    val navBarBaseHeight = remember { 60.dp }
    val fabSize = remember { 56.dp }

    // Convert dimensions to pixels once using LocalDensity and remember the results
    val density = LocalDensity.current
    val totalHeightPx = remember(density) { with(density) { totalHeight.toPx() } }
    val navBarBaseHeightPx = remember(density) { with(density) { navBarBaseHeight.toPx() } }
    val fabRadiusPx = remember(density) { with(density) { fabSize.toPx() } / 2f }
    val curveBottomOffsetPx = remember(density) { with(density) { 10.dp.toPx() } }

    // Remember container width to avoid recalculations
    var containerWidth by remember { mutableFloatStateOf(0f) }

    // Use stable keys for transition to prevent unnecessary recreations
    val transition = updateTransition(
        targetState = selectedItemIndex,
        label = "CurvedNavTransition"
    )

    // Use derivedStateOf to calculate item width only when dependencies change
    val itemWidth by remember(containerWidth, items.size) {
        derivedStateOf {
            if (containerWidth == 0f) 0f else containerWidth / items.size
        }
    }

    // Animate center X position with optimized spring animation
    val currentCenterX by transition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,  // Increased stiffness for faster animation
                visibilityThreshold = 0.01f  // Add threshold to terminate animation earlier
            )
        },
        label = "CenterXAnimation"
    ) { index ->
        if (containerWidth == 0f) 0f
        else (index * itemWidth) + (itemWidth / 2f)
    }
    val surfaceColor = MaterialTheme.colorScheme.surface

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5
    val shadowColor = if (isDark) {
        android.graphics.Color.argb(70, 255, 255, 255)
    } else {
        android.graphics.Color.argb(70, 0, 0, 0)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
    ) {
        // Use remember with key to avoid recreating the modifier on each recomposition
        val canvasModifier = remember {
            Modifier
                .matchParentSize()
                .onGloballyPositioned {
                    // Only update containerWidth if it has changed to avoid unnecessary recompositions
                    val newWidth = it.size.width.toFloat()
                    if (containerWidth != newWidth) {
                        containerWidth = newWidth
                    }
                }
        }

        // Pre-calculate values that don't depend on animation state
        val bottomNavOffsetY = remember(totalHeightPx, navBarBaseHeightPx) {
            totalHeightPx - navBarBaseHeightPx
        }

        val curveHalfWidth = remember(density, fabRadiusPx) {
            fabRadiusPx * 2 + with(density) { 20.dp.toPx() }
        }

        val fabCenterY = remember(totalHeightPx, navBarBaseHeightPx, fabRadiusPx) {
            fabRadiusPx + (totalHeightPx - navBarBaseHeightPx) / 2f
        }

        // Pre-calculate control point offsets
        val controlPointXOffset1 = remember(fabRadiusPx) { fabRadiusPx * 1.5f }
        val controlPointYOffset1 = remember(fabRadiusPx) { fabRadiusPx / 6f }
        val controlPointXOffset2 = remember(fabRadiusPx) { fabRadiusPx * 1.5f }
        val controlPointYOffset2 = remember(fabRadiusPx) { fabRadiusPx / 4f }

        // Create a single Path instance and reuse it for all drawing operations
        val path = remember { Path() }
        
        // Create reusable Paint objects outside the drawing scope
        val shadowPaint = remember {
            Paint().apply { 
                isAntiAlias = true 
                asFrameworkPaint().apply {
                    maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
                }
            }
        }
        
        Canvas(modifier = canvasModifier) {
            if (containerWidth == 0f) return@Canvas

            // Calculate curve points more efficiently
            // First curve (left side)
            val firstCurveStart = PointF(currentCenterX - curveHalfWidth, bottomNavOffsetY)
            val firstCurveEnd = PointF(currentCenterX, totalHeightPx - curveBottomOffsetPx)

            val firstCurveControlPoint1 = PointF(
                x = firstCurveStart.x + controlPointXOffset1,
                y = bottomNavOffsetY + controlPointYOffset1
            )
            val firstCurveControlPoint2 = PointF(
                x = firstCurveEnd.x - controlPointXOffset2,
                y = firstCurveEnd.y - controlPointYOffset2
            )

            // Second curve (right side)
            val secondCurveEnd = PointF(currentCenterX + curveHalfWidth, bottomNavOffsetY)
            val secondCurveControlPoint1 = PointF(
                x = firstCurveEnd.x + controlPointXOffset2,
                y = firstCurveEnd.y - controlPointYOffset2
            )
            val secondCurveControlPoint2 = PointF(
                x = secondCurveEnd.x - controlPointXOffset1,
                y = bottomNavOffsetY + controlPointYOffset1
            )

            path.reset() // Clear the path before redefining it
            path.apply {
                moveTo(0f, bottomNavOffsetY)
                lineTo(firstCurveStart.x, firstCurveStart.y)
                cubicTo(
                    firstCurveControlPoint1.x, firstCurveControlPoint1.y,
                    firstCurveControlPoint2.x, firstCurveControlPoint2.y,
                    firstCurveEnd.x, firstCurveEnd.y
                )
                cubicTo(
                    secondCurveControlPoint1.x, secondCurveControlPoint1.y,
                    secondCurveControlPoint2.x, secondCurveControlPoint2.y,
                    secondCurveEnd.x, secondCurveEnd.y
                )
                lineTo(size.width, bottomNavOffsetY)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            
            // -----------------------
            //     DRAW SHADOW
            // -----------------------
            // Draw the same path but with a blur mask filter to create the shadow.
            drawIntoCanvas { canvas ->
                // Just update the color without creating new Paint objects
                shadowPaint.asFrameworkPaint().color = shadowColor
                canvas.drawPath(path, shadowPaint)
            }

            drawPath(path = path, color = surfaceColor)

            // -----------------------
            //  DRAW FAB SHADOW
            // -----------------------
            drawIntoCanvas { canvas ->
                // Reuse the same paint object for the circle shadow
                canvas.drawCircle(
                    Offset(currentCenterX, fabCenterY),
                    fabRadiusPx,
                    shadowPaint
                )
            }

            drawCircle(
                color = surfaceColor,
                radius = fabRadiusPx,
                center = Offset(currentCenterX, fabCenterY)
            )
        }

        // Only render the selected item icon if we have valid data
        val selectedItem = items.getOrNull(selectedItemIndex)
        if (selectedItem != null && containerWidth > 0f) {
            // Use remember for the offset calculation to prevent recalculations during animations
            val iconSize = remember { 36.dp }

            // Calculate offset only when dependencies change
            val fabOffset = remember(currentCenterX, fabCenterY, fabRadiusPx) {
                IntOffset(
                    (currentCenterX - fabRadiusPx).roundToInt(),
                    (fabCenterY - fabRadiusPx).roundToInt()
                )
            }

            Box(
                modifier = Modifier
                    .size(fabSize)
                    .offset { fabOffset },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = selectedItem.selectedIcon,
                    contentDescription = selectedItem.title,
                    modifier = Modifier.size(iconSize),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Remember row modifiers to prevent recreation during recomposition
        val rowModifier = remember {
            Modifier
                .fillMaxWidth()
                .height(navBarBaseHeight)
                .align(Alignment.BottomCenter)
        }

        Row(
            modifier = rowModifier,
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Use key for each item to ensure proper recomposition isolation
            items.forEachIndexed { index, item ->
                // Calculate selection state once
                val isSelected = index == selectedItemIndex

                // Use a key to ensure animations are properly tracked per item
                androidx.compose.runtime.key(item.route) {
                    // Remember animation specs to avoid recreation
                    val animSpec = remember { tween<Float>(durationMillis = 100) }
                    val iconSize = remember { 28.dp }
                    val spacerHeight = remember { 4.dp }
                    val emptySpacerHeight = remember { 28.dp }

                    // Animate scale with optimized tween animation and visibility threshold
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.5f else 1f,
                        animationSpec = tween(
                            durationMillis = 100,
                            delayMillis = 0,
                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                        ),
                        label = "IconScaleAnimation",
                        visibilityThreshold = 0.01f
                    )

                    // Create a stable interaction source that won't be recreated on recomposition
                    val interactionSource = remember { MutableInteractionSource() }

                    // Remember the column modifier to prevent recreation
                    val columnModifier = remember(interactionSource, index) {
                        Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { onItemSelected(index) }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = columnModifier
                    ) {
                        if (!isSelected) {
                            // Remember the icon modifier to prevent recreation during animation
                            val iconModifier = remember(iconScale) {
                                Modifier
                                    .size(iconSize)
                                    .graphicsLayer {
                                        // Apply scale transformation efficiently
                                        scaleX = iconScale
                                        scaleY = iconScale
                                    }
                            }

                            // Only render unselected icon when needed
                            Icon(
                                imageVector = item.unselectedIcon,
                                contentDescription = item.title,
                                modifier = iconModifier,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(spacerHeight))
                        } else {
                            // Combine spacers to reduce composable count
                            Spacer(modifier = Modifier.height(emptySpacerHeight))
                        }
                    }
                }
            }
        }
    }
}


/**
 * Preview of the curved bottom navigation with sample items
 */
@Preview
@Composable
fun CurvedBottomNavigationPreview() {
    var selectedIndex by remember { mutableIntStateOf(0) }

    val items = listOf(
        NavItem(
            title = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            route = "home"
        ),
        NavItem(
            title = "History",
            selectedIcon = Icons.Filled.List,
            unselectedIcon = Icons.Outlined.List,
            route = "history"
        ),
        NavItem(
            title = "Profile",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
            route = "profile"
        )
    )

    CurvedBottomNavigation(
        items = items,
        selectedItemIndex = selectedIndex,
        onItemSelected = { selectedIndex = it }
    )
}