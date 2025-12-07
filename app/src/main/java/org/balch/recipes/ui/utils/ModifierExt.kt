package org.balch.recipes.ui.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun Modifier.parallaxLayoutModifier(scrollState: LazyListState, rate: Int) =
    graphicsLayer {
        val scrollOffset = scrollState.firstVisibleItemScrollOffset
        val firstVisibleItemIndex = scrollState.firstVisibleItemIndex

        if (firstVisibleItemIndex == 0) {
            translationY = if (rate > 0) scrollOffset.toFloat() / rate else scrollOffset.toFloat()

            // Fade out as it scrolls up
            alpha = (1f - (scrollOffset.toFloat() / 500f)).coerceIn(0f, 1f)

            // Scale down slightly
            val scale = (1f - (scrollOffset.toFloat() / 1500f)).coerceIn(0.9f, 1f)
            scaleX = scale
            scaleY = scale
        } else {
            alpha = 0f
        }
    }
