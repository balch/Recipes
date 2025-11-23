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

