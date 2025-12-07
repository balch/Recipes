package org.balch.recipes.ui.utils

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionDefaults
import androidx.compose.animation.SharedTransitionScope.OverlayClip
import androidx.compose.animation.SharedTransitionScope.PlaceholderSize
import androidx.compose.animation.SharedTransitionScope.PlaceholderSize.Companion.ContentSize
import androidx.compose.animation.SharedTransitionScope.ResizeMode
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.scaleToBounds
import androidx.compose.animation.SharedTransitionScope.SharedContentState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import org.balch.recipes.core.navigation.LocalSharedTransition

/**
 * Ext function to support shared bounds transitions that uses the
 * defaults but provides automatic access to
 * [androidx.compose.animation.SharedTransitionScope] and
 * [AnimatedVisibilityScope] via [LocalSharedTransition]
 */
@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.sharedBounds(
    key: String,
    enter: EnterTransition = fadeIn(),
    exit: ExitTransition = fadeOut(),
    boundsTransform: BoundsTransform = SharedTransitionDefaults.BoundsTransform,
    resizeMode: ResizeMode = scaleToBounds(ContentScale.FillWidth, Center),
    placeholderSize: PlaceholderSize = ContentSize,
    renderInOverlayDuringTransition: Boolean = true,
    zIndexInOverlay: Float = 2f,
    clipInOverlayDuringTransition: OverlayClip = ParentClip,
): Modifier {
    val sharedTransitionScope = LocalSharedTransition.current.sharedTransitionScope
    val animatedVisibilityScope = LocalSharedTransition.current.animatedVisibilityScope
    return if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        this then with(sharedTransitionScope) {
            this@sharedBounds.sharedBounds(
                enter = enter,
                exit = exit,
                boundsTransform = boundsTransform,
                resizeMode = resizeMode,
                placeholderSize = placeholderSize,
                renderInOverlayDuringTransition = renderInOverlayDuringTransition,
                zIndexInOverlay = zIndexInOverlay,
                clipInOverlayDuringTransition = clipInOverlayDuringTransition,
                sharedContentState = rememberSharedContentState(key),
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    } else this
}

private val ParentClip: OverlayClip =
    object : OverlayClip {
        override fun getClipPath(
            sharedContentState: SharedContentState,
            bounds: Rect,
            layoutDirection: LayoutDirection,
            density: Density,
        ): Path? {
            return sharedContentState.parentSharedContentState?.clipPathInOverlay
        }
    }

