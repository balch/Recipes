package org.balch.recipes.features.agent.tools.code

import ai.koog.agents.core.tools.Tool
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Qualifier
import dev.zacsweers.metro.SingleIn

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class CodeRecipeTool

/**
 * Provides access to the CodeRecipe tools.
 */
@SingleIn(AppScope::class)
@Inject
class CodeRecipeTools(
    @param:CodeRecipeTool
    private val toolSet: Set<Tool<*, *>>,
) {
    val tools: List<Tool<*, *>> = toolSet.toList()
}
