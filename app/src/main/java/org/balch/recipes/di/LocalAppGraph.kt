package org.balch.recipes.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import org.balch.recipes.RecipesApplication

/**
 * CompositionLocal providing access to the Metro AppGraph.
 */
val LocalAppGraph = staticCompositionLocalOf<AppGraph> {
    error("No AppGraph provided! Did you forget to wrap your content with AppGraphProvider?")
}

/**
 * Remembers and returns the AppGraph from the application context.
 */
@Composable
fun rememberAppGraph(): AppGraph {
    val context = LocalContext.current
    return remember { (context.applicationContext as RecipesApplication).graph }
}
