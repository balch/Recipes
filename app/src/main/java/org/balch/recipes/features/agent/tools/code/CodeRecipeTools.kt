package org.balch.recipes.features.agent.tools.code

import ai.koog.agents.core.tools.Tool
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Provides access to the CodeRecipe tools.
 */
@SingleIn(AppScope::class)
class CodeRecipeTools @Inject constructor(
    codeRecipeListTool: CodeRecipeListTool,
    codeRecipeSearchTool: CodeRecipeSearchTool,
    codeRecipeLookupTool: CodeRecipeLookupTool,
    codeRecipeDetailTool: CodeRecipeDetailTool,
    codeRecipeCreateTool: CodeRecipeCreateTool,
) {
    val tools: List<Tool<*, *>> = listOf(
        codeRecipeListTool,
        codeRecipeLookupTool,
        codeRecipeSearchTool,
        codeRecipeDetailTool,
        codeRecipeCreateTool,
    )
}
