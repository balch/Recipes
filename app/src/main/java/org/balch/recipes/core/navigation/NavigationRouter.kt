package org.balch.recipes.core.navigation

import com.diamondedge.logging.logging
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.balch.recipes.RecipeRoute
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationRouter @Inject constructor() {

    private val _navigationRoute = MutableSharedFlow<RecipeRoute>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val logger = logging("NavigationRouter")

    val navigationRoute: SharedFlow<RecipeRoute> = _navigationRoute

    fun navigateTo(recipeRoute: RecipeRoute): Boolean =
        _navigationRoute.tryEmit(recipeRoute).also {
            logger.d { "Navigation to $recipeRoute was ${if (it) "successful" else "unsuccessful"}" }
        }
}