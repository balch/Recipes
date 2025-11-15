package org.balch.recipes.core.ai.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.balch.recipes.RecipeRoute

@LLMDescription("Navigate to an App Screen")
class NavigationTools : ToolSet {

    private val _navigationRoute = MutableSharedFlow<RecipeRoute>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val navigationRoute: SharedFlow<RecipeRoute> = _navigationRoute

    @Tool
    @LLMDescription("Navigate to the passed in RecipeRoute")
    fun navigateTo(route: RecipeRoute) {
        _navigationRoute.tryEmit(route)
    }
}