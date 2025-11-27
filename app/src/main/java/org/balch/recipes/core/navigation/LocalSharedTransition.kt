package org.balch.recipes.core.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf

/**
 * Contains information about the current shared transition.
 */
data class SharedTransitionInfo(
    val sharedTransitionScope: SharedTransitionScope? = null,
    val animatedVisibilityScope: AnimatedVisibilityScope? = null,
)

/** The CompositionLocal containing the current [SharedTransitionInfo]. */
val LocalSharedTransition = compositionLocalOf { SharedTransitionInfo() }
