package org.balch.recipes.features.agent.tools.meals

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.balch.recipes.core.models.Ingredient
import org.balch.recipes.core.repository.RecipeRepository
import javax.inject.Inject

/**
 * Tool for returning all ingredients from TheMealDB via the repository
 */
class MealIngredientListTool @Inject internal constructor(
    private val recipeRepository: RecipeRepository,
) : Tool<MealIngredientListTool.Args, MealIngredientListTool.Result>() {

    @Serializable
    data class Args(
        @property:LLMDescription("Optional contextual information from the calling agent.")
        val callingAgentContext: String?,
    )

    @Serializable
    data class Result(
        @property:LLMDescription("List of ingredients available for filtering/searching")
        val ingredients: List<Ingredient>,
    )

    override val argsSerializer: KSerializer<Args> = Args.serializer()
    override val resultSerializer: KSerializer<Result> = Result.serializer()

    override val name: String = "meal_list_ingredients"
    override val description: String = "Returns the list of Ingredients available in TheMealDB."

    override suspend fun execute(args: Args): Result =
        Result(
            ingredients = recipeRepository.getIngredients().getOrThrow()
        )
}
