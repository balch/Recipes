package org.balch.recipes.features.agent.ai.meals

import ai.koog.agents.core.tools.Tool
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Provides access to the CodeRecipe tools.
 */
@Singleton
class MealRecipeTools @Inject internal constructor(
    mealRecipeCreateTool: MealRecipeCreateTool,
    mealRecipeDetailTool: MealRecipeDetailTool,
) {
    val tools: List<Tool<*, *>> = listOf(
        mealRecipeDetailTool,
        mealRecipeCreateTool,
    )
}

