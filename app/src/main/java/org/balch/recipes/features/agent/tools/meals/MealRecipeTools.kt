package org.balch.recipes.features.agent.tools.meals

import ai.koog.agents.core.tools.Tool
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn


/**
 * Provides access to the CodeRecipe tools.
 */
@SingleIn(AppScope::class)
class MealRecipeTools @Inject internal constructor(
    mealRecipeCreateTool: MealRecipeCreateTool,
    mealRecipeDetailTool: MealRecipeDetailTool,
    mealAreaListTool: MealAreaListTool,
    mealCategoryListTool: MealCategoryListTool,
    mealIngredientListTool: MealIngredientListTool,
    mealSearchTool: MealSearchTool,
    mealsByCategoryTool: MealsByCategoryTool,
    mealsByAreaTool: MealsByAreaTool,
    mealsByIngredientTool: MealsByIngredientTool,
    mealByIdTool: MealByIdTool,
    mealRandomTool: MealRandomTool,
    mealLookupTool: MealLookupTool,
) {
    val tools: List<Tool<*, *>> = listOf(
        mealAreaListTool,
        mealCategoryListTool,
        mealIngredientListTool,
        mealSearchTool,
        mealsByCategoryTool,
        mealsByAreaTool,
        mealsByIngredientTool,
        mealByIdTool,
        mealRandomTool,
        mealLookupTool,
        mealRecipeDetailTool,
        mealRecipeCreateTool,
    )
}

