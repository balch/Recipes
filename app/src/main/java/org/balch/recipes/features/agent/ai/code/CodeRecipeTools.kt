package org.balch.recipes.features.agent.ai.code

import ai.koog.agents.core.tools.Tool
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Provides access to the CodeRecipe tools.
 */
@Singleton
class CodeRecipeTools @Inject internal constructor(
    codeRecipeListTool: CodeRecipeListTool,
    codeRecipeSearchTool: CodeRecipeSearchTool,
    codeRecipeLookupTool: CodeRecipeLookupTool,
    codeRecipeDetailTool: CodeRecipeDetailTool,
) {
    val tools: List<Tool<*, *>> = listOf(
        codeRecipeListTool,
        codeRecipeLookupTool,
        codeRecipeSearchTool,
        codeRecipeDetailTool
    )
}

