package org.balch.recipes.ui.utils

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.PlaceHolderSize
import androidx.compose.animation.SharedTransitionScope.PlaceHolderSize.Companion.animatedSize
import androidx.compose.animation.SharedTransitionScope.PlaceHolderSize.Companion.contentSize
import androidx.compose.animation.SharedTransitionScope.ResizeMode
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.sharedElement(
    key: String,
    placeHolderSize: PlaceHolderSize = contentSize,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
) = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        this then with(sharedTransitionScope) {
            this@sharedElement.sharedElement(
                placeHolderSize = placeHolderSize,
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = animatedVisibilityScope
            )
        }
    } else this

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.sharedBounds(
    key: String,
    resizeMode: ResizeMode = ResizeMode.ScaleToBounds(),
    placeHolderSize: PlaceHolderSize = animatedSize,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
) = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
    this then with(sharedTransitionScope) {
        this@sharedBounds.sharedBounds(
            resizeMode = resizeMode,
            placeHolderSize = placeHolderSize,
            sharedContentState = rememberSharedContentState(key = key),
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
} else this

