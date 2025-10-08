package org.balch.recipes.ui.nav

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.diamondedge.logging.logging


private val backStackLogger = logging("NavBackStackExt")

/**
 * Returns `true` if there is only one screen in the backstack
 * and the app will close when the back button is pressed
 */
fun NavBackStack<NavKey>.isLastScreen() = size == 1

fun NavBackStack<NavKey>.push(destination: NavKey){
    backStackLogger.d { "push: $destination" }
    add(destination)
}

fun NavBackStack<NavKey>.pop(): NavKey? =
    removeLastOrNull()
        .also { backStackLogger.d { "pop: $it" } }

fun NavBackStack<NavKey>.peek(): NavKey? = lastOrNull()
