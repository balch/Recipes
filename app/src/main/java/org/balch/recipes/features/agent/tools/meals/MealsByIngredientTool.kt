package org.balch.recipes.features.agent.tools.meals

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import dev.zacsweers.metro.Inject
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.balch.recipes.core.models.MealSummary
import org.balch.recipes.core.repository.RecipeRepository

/**
 * Tool for listing meals by ingredient using RecipeRepository
 */
class MealsByIngredientTool @Inject internal constructor(
    private val recipeRepository: RecipeRepository,
) : Tool<MealsByIngredientTool.Args, MealsByIngredientTool.Result>() {

    @Serializable
    data class Args(
        @property:LLMDescription("Optional contextual information from the calling agent.")
        val callingAgentContext: String?,

        @property:LLMDescription("Ingredient name to filter meals by (e.g., 'Chicken', 'Tomato')")
        val ingredient: String,
    )

    @Serializable
    data class Result(
        @property:LLMDescription("List of meal summaries that include the specified ingredient")
        val meals: List<MealSummary>,

        @property:LLMDescription("The ingredient used for filtering")
        val ingredient: String,
    )

    override val argsSerializer: KSerializer<Args> = Args.serializer()
    override val resultSerializer: KSerializer<Result> = Result.serializer()

    override val name: String = "meal_list_by_ingredient"
    override val description: String = "Returns meals that include the given ingredient."

    override suspend fun execute(args: Args): Result =
        Result(
            meals = recipeRepository.getMealsByIngredient(args.ingredient).getOrThrow(),
            ingredient = args.ingredient,
        )
}
