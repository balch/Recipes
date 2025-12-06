package org.balch.recipes.features.agent.tools.meals

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.balch.recipes.core.models.Meal
import org.balch.recipes.core.repository.RecipeRepository
import javax.inject.Inject

/**
 * Tool for retrieving a random Meal using RecipeRepository
 */
class MealRandomTool @Inject internal constructor(
    private val recipeRepository: RecipeRepository,
) : Tool<MealRandomTool.Args, MealRandomTool.Result>() {

    @Serializable
    data class Args(
        @property:LLMDescription("Optional contextual information from the calling agent.")
        val callingAgentContext: String?,
    )

    @Serializable
    data class Result(
        @property:LLMDescription("A randomly selected Meal")
        val meal: Meal,
    )

    override val argsSerializer: KSerializer<Args> = Args.serializer()
    override val resultSerializer: KSerializer<Result> = Result.serializer()

    override val name: String = "meal_random"
    override val description: String = "Fetch a random Meal from TheMealDB via the repository."

    override suspend fun execute(args: Args): Result =
        Result(
            meal = recipeRepository.getRandomMeal().getOrThrow()
        )
}
