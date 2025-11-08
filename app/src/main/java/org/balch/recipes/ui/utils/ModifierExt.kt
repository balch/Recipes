package org.balch.recipes.ui.utils

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.PlaceholderSize
import androidx.compose.animation.SharedTransitionScope.PlaceholderSize.Companion.AnimatedSize
import androidx.compose.animation.SharedTransitionScope.PlaceholderSize.Companion.ContentSize
import androidx.compose.animation.SharedTransitionScope.ResizeMode
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.scaleToBounds
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale


@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.sharedElement(
    key: String,
    placeholderSize: PlaceholderSize = ContentSize,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
) = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        this then with(sharedTransitionScope) {
            this@sharedElement.sharedElement(
                placeholderSize = placeholderSize,
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = animatedVisibilityScope
            )
        }
    } else this

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.sharedBounds(
    key: String,
    resizeMode: ResizeMode = scaleToBounds(ContentScale.FillWidth, Center),
    placeholderSize: PlaceholderSize = AnimatedSize,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
) = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
    this then with(sharedTransitionScope) {
        this@sharedBounds.sharedBounds(
            resizeMode = resizeMode,
            placeholderSize = placeholderSize,
            sharedContentState = rememberSharedContentState(key = key),
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
} else this

