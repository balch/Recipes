package org.balch.recipes.ui.widgets

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import org.balch.recipes.RecipeRoute
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.theme.ThemePreview
import kotlin.math.roundToInt
import android.view.PointerIcon as AndroidPointerIcon

@Composable
fun AiMiniWindow(
    modifier: Modifier = Modifier,
    onNavigateTo: (RecipeRoute) -> Unit,
    moodTintColor: Color?,
    hazeState: HazeState,
) {
    val density = LocalDensity.current
    val context = LocalContext.current

    val screenWindow = LocalWindowInfo.current.containerDpSize

    var width by remember { mutableStateOf(screenWindow.width * 0.6f) }
    var height by remember { mutableStateOf(screenWindow.height * 0.35f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .width(width)
            .height(height)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .hazeEffect(hazeState, style = LocalHazeStyle.current)
                .padding(vertical = 4.dp)
                .pointerHoverIcon(PointerIcon.Hand)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                },
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(text = "Maestro")
            }
        }

        // Resize Handle - Left
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(24.dp)
                .pointerHoverIcon(PointerIcon(AndroidPointerIcon.getSystemIcon(context, AndroidPointerIcon.TYPE_HORIZONTAL_DOUBLE_ARROW)))
                .pointerInput(density) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        with(density) {
                            width = (width - dragAmount.x.toDp()).coerceIn(150.dp, screenWindow.width)
                        }
                    }
                }
        )

        // Resize Handle - Top
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(24.dp)
                .pointerHoverIcon(PointerIcon(AndroidPointerIcon.getSystemIcon(context, AndroidPointerIcon.TYPE_VERTICAL_DOUBLE_ARROW)))
                .pointerInput(density) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        with(density) {
                            height = (height - dragAmount.y.toDp()).coerceIn(150.dp, screenWindow.height)
                        }
                    }
                }
        )

        // Resize Handle - TopStart (Corner)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(32.dp)
                .pointerHoverIcon(PointerIcon(AndroidPointerIcon.getSystemIcon(context, AndroidPointerIcon.TYPE_TOP_LEFT_DIAGONAL_DOUBLE_ARROW)))
                .pointerInput(density) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        with(density) {
                            width = (width - dragAmount.x.toDp()).coerceIn(150.dp, screenWindow.width)
                            height = (height - dragAmount.y.toDp()).coerceIn(150.dp, screenWindow.height)
                        }
                    }
                }
        )
    }
}

@ThemePreview
@Composable
fun AiAiMiniWindowPreview() {
    RecipesTheme {
        AiMiniWindow(
            onNavigateTo = {},
            moodTintColor = Color.Green,
            hazeState = HazeState(),
        )
    }
}

