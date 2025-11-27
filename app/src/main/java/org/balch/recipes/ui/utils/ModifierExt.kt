package org.balch.recipes.ui.utils

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope.PlaceholderSize
import androidx.compose.animation.SharedTransitionScope.PlaceholderSize.Companion.ContentSize
import androidx.compose.animation.SharedTransitionScope.ResizeMode
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.RemeasureToBounds
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.balch.recipes.core.navigation.LocalSharedTransition
import org.balch.recipes.ui.nav.isCompact

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
