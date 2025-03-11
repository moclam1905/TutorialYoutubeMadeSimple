package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
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

data class PointF(val x: Float, val y: Float)

@Composable
fun CurvedBottomNavigation(
    items: List<NavItem>,
    selectedItemIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    val totalHeight = 80.dp
    val navBarBaseHeight = 60.dp
    val fabSize = 56.dp

    val totalHeightPx = with(LocalDensity.current) { totalHeight.toPx() }
    val navBarBaseHeightPx = with(LocalDensity.current) { navBarBaseHeight.toPx() }
    val fabRadiusPx = with(LocalDensity.current) { fabSize.toPx() } / 2f

    var containerWidth by remember { mutableFloatStateOf(0f) }
    val transition =
        updateTransition(targetState = selectedItemIndex, label = "CurvedNavTransition")

    val currentCenterX by transition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "CenterXAnimation"
    ) { index ->
        if (containerWidth == 0f) 0f
        else {
            val itemWidth = containerWidth / items.size
            (index * itemWidth) + (itemWidth / 2f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .onGloballyPositioned {
                    containerWidth = it.size.width.toFloat()
                }
        ) {
            if (containerWidth == 0f) return@Canvas

            val bottomNavOffsetY = totalHeightPx - navBarBaseHeightPx
            val curveBottomOffset = 10.dp.toPx()
            val curveHalfWidth = fabRadiusPx * 2 + 20.dp.toPx()

            val firstCurveStart = PointF(currentCenterX - curveHalfWidth, bottomNavOffsetY)
            val firstCurveEnd = PointF(currentCenterX, totalHeightPx - curveBottomOffset)
            val firstCurveControlPoint1 = PointF(
                x = firstCurveStart.x + (fabRadiusPx + fabRadiusPx / 2f),
                y = bottomNavOffsetY + (fabRadiusPx / 6f)
            )
            val firstCurveControlPoint2 = PointF(
                x = firstCurveEnd.x - (fabRadiusPx + (fabRadiusPx / 2f)),
                y = firstCurveEnd.y - (fabRadiusPx / 4f)
            )
            val secondCurveEnd = PointF(currentCenterX + curveHalfWidth, bottomNavOffsetY)
            val secondCurveControlPoint1 = PointF(
                x = firstCurveEnd.x + (fabRadiusPx + (fabRadiusPx / 2f)),
                y = firstCurveEnd.y - (fabRadiusPx / 4f)
            )
            val secondCurveControlPoint2 = PointF(
                x = secondCurveEnd.x - (fabRadiusPx + fabRadiusPx / 2f),
                y = bottomNavOffsetY + (fabRadiusPx / 6f)
            )

            val path = Path().apply {
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
            drawPath(path = path, color = Color.White)

            val fabCenterY = fabRadiusPx + (totalHeightPx - navBarBaseHeightPx) / 2f
            drawCircle(
                color = Color.White,
                radius = fabRadiusPx,
                center = Offset(currentCenterX, fabCenterY)
            )
        }

        val selectedItem = items.getOrNull(selectedItemIndex)
        if (selectedItem != null && containerWidth > 0f) {
            val fabCenterY = fabRadiusPx + (totalHeightPx - navBarBaseHeightPx) / 2f
            Box(
                modifier = Modifier
                    .size(fabSize)
                    .offset {
                        IntOffset(
                            (currentCenterX - fabRadiusPx).roundToInt(),
                            (fabCenterY - fabRadiusPx).roundToInt()
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = selectedItem.selectedIcon,
                    contentDescription = selectedItem.title,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(navBarBaseHeight)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = (index == selectedItemIndex)
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.5f else 1f,
                    animationSpec = tween(durationMillis = 100),
                    label = "IconScaleAnimation"
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onItemSelected(index) }
                ) {
                    if (!isSelected) {
                        Icon(
                            imageVector = item.unselectedIcon,
                            contentDescription = item.title,
                            modifier = Modifier
                                .size(28.dp)
                                .graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                },
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    } else {
                        Spacer(modifier = Modifier.height(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
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