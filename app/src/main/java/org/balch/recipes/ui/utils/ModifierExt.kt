package org.balch.recipes.ui.utils

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope.PlaceholderSize
import androidx.compose.animation.SharedTransitionScope.PlaceholderSize.Companion.ContentSize
import androidx.compose.animation.SharedTransitionScope.ResizeMode
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.RemeasureToBounds
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import org.balch.recipes.core.navigation.LocalSharedTransition
import org.balch.recipes.core.navigation.isCompact

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

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.sharedBounds(
    key: String,
    resizeMode: ResizeMode = RemeasureToBounds,
    placeholderSize: PlaceholderSize = ContentSize,
): Modifier {
    val sharedTransitionScope = LocalSharedTransition.current.sharedTransitionScope
    val animatedVisibilityScope = LocalSharedTransition.current.animatedVisibilityScope
    return if (
        currentWindowAdaptiveInfo().isCompact()
            && sharedTransitionScope != null
            && animatedVisibilityScope != null
    ) {
        this then with(sharedTransitionScope) {
            this@sharedBounds.sharedBounds(
                resizeMode = resizeMode,
                placeholderSize = placeholderSize,
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = animatedVisibilityScope,
                renderInOverlayDuringTransition = false,
            )
        }
    } else this
}
