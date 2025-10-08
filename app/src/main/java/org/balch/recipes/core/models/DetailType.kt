package org.balch.recipes.core.models

import kotlinx.serialization.Serializable

@Serializable
sealed interface DetailType {
    @Serializable
    data class MealLookup(val mealSummary: MealSummary) : DetailType
    @Serializable
    data class MealContent(val meal: Meal) : DetailType
    @Serializable
    data object RandomRecipe : DetailType
    @Serializable
    data class CodeRecipeContent(val codeRecipe: CodeRecipe) : DetailType
}