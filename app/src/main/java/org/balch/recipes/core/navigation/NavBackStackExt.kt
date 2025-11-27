package org.balch.recipes.core.navigation

import androidx.navigation3.runtime.NavBackStack
import com.diamondedge.logging.logging
import org.balch.recipes.RecipeRoute


private val backStackLogger = logging("NavBackStackExt")

/**
 * Returns `true` if there is only one screen in the backstack
 * and the app will close when the back button is pressed
 */
fun NavBackStack<RecipeRoute>.isLastScreen() = size == 1

fun NavBackStack<RecipeRoute>.push(destination: RecipeRoute){
    backStackLogger.d { "push: $destination" }
    add(destination)
}

fun NavBackStack<RecipeRoute>.pop(): RecipeRoute? =
    removeLastOrNull()
        .also { backStackLogger.d { "pop: $it" } }

fun NavBackStack<RecipeRoute>.popTo(navKey: RecipeRoute) {
    while (peek() != navKey) pop()
}

fun NavBackStack<RecipeRoute>.peek(): RecipeRoute? = lastOrNull()
