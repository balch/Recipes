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

    data class NavInfo(
        val recipeRoute: RecipeRoute,
        val isFromAgent: Boolean,
    )

    private val _navigationRoute = MutableSharedFlow<NavInfo>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val logger = logging("NavigationRouter")

    val navigationRoute: SharedFlow<NavInfo> = _navigationRoute

    fun navigateTo(
        recipeRoute: RecipeRoute,
        isFromAgent: Boolean = false,
    ): Boolean =
        NavInfo(
            recipeRoute = recipeRoute,
            isFromAgent = isFromAgent
        ).let { navInfo ->
            _navigationRoute.tryEmit(navInfo).also {
                logger.d { "Navigation to $navInfo was ${if (it) "successful" else "unsuccessful"}" }
            }
        }
}