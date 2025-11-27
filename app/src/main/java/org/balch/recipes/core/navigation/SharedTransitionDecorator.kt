package org.balch.recipes.core.navigation

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope

/**
 * Decorates a NavEntry with shared transition objects used by
 * [org.balch.recipes.ui.utils.sharedBounds] to provide animation
 * between ui elements
 */
@Composable
fun <T : Any> SharedTransitionScope.rememberSharedTransitionDecorator(): SharedTransitionNavEntryDecorator<T> {
    return remember {
        SharedTransitionNavEntryDecorator(this)
    }
}

class SharedTransitionNavEntryDecorator<T : Any>(
    val sharedTransitionScope: SharedTransitionScope
): NavEntryDecorator<T>(
        decorate = { entry ->
            CompositionLocalProvider(
                LocalSharedTransition provides
                        SharedTransitionInfo(
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current
                        )
            ) {
                entry.Content()
            }
        },
    )
