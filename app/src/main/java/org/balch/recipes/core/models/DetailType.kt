package org.balch.recipes.core.models

import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.Serializable

@LLMDescription("Represents different types of detail screens that can be displayed in the app")
@Serializable
sealed interface DetailType {
    @LLMDescription("Detail view for a meal that needs to be loaded using a meal summary")
    @Serializable
    data class MealLookup(@property:LLMDescription("The meal summary containing basic meal information") val mealSummary: MealSummary) : DetailType
    @LLMDescription("Detail view for a meal with full content already loaded")
    @Serializable
    data class MealContent(@property:LLMDescription("The complete meal object with all recipe details") val meal: Meal) : DetailType
    @LLMDescription("Detail view requesting a random recipe to be loaded and displayed")
    @Serializable
    data object RandomRecipe : DetailType
    @LLMDescription("Detail view for a code recipe tutorial")
    @Serializable
    data class CodeRecipeContent(@property:LLMDescription("The code recipe object containing tutorial information") val codeRecipe: CodeRecipe) : DetailType
}