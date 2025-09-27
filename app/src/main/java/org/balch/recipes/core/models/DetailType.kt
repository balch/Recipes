package org.balch.recipes.core.models

sealed interface DetailType {
    data class MealLookup(val mealId: String) : DetailType
    data class MealContent(val meal: Meal) : DetailType
    data object MealRandom : DetailType
    data class CodeRecipeContent(val codeRecipe: CodeRecipe) : DetailType
}