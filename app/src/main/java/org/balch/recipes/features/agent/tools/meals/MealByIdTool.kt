package org.balch.recipes.features.agent.tools.meals

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import dev.zacsweers.metro.Inject
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.balch.recipes.core.models.Meal
import org.balch.recipes.core.repository.RecipeRepository

/**
 * Tool for retrieving a full Meal by its id using RecipeRepository
 */
class MealByIdTool @Inject constructor(
    private val recipeRepository: RecipeRepository,
) : Tool<MealByIdTool.Args, MealByIdTool.Result>() {

    @Serializable
    data class Args(
        @property:LLMDescription("Optional contextual information from the calling agent.")
        val callingAgentContext: String?,

        @property:LLMDescription("The id of the meal to retrieve")
        val id: String,
    )

    @Serializable
    data class Result(
        @property:LLMDescription("The Meal that matches the id")
        val meal: Meal,
    )

    override val argsSerializer: KSerializer<Args> = Args.serializer()
    override val resultSerializer: KSerializer<Result> = Result.serializer()

    override val name: String = "meal_get_by_id"
    override val description: String = "Fetch a full Meal by its id."

    override suspend fun execute(args: Args): Result =
        Result(
            meal = recipeRepository.getMealById(args.id).getOrThrow()
        )
}
