package org.balch.recipes.features.agent.tools.meals

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.balch.recipes.core.models.Meal
import org.balch.recipes.core.models.MealSummary
import org.balch.recipes.core.repository.RecipeRepository

/**
 * Tool for returning a full Meal from a MealSummary (by id)
 */
@MealTool
@ContributesIntoSet(AppScope::class, binding<Tool<*, *>>())
@Inject
class MealLookupTool(
    private val recipeRepository: RecipeRepository,
) : Tool<MealLookupTool.Args, MealLookupTool.Result>() {

    @Serializable
    data class Args(
        @property:LLMDescription("Optional contextual information from the calling agent.")
        val callingAgentContext: String?,

        @property:LLMDescription("MealSummary to lookup the full Meal from")
        val mealSummary: MealSummary,
    )

    @Serializable
    data class Result(
        @property:LLMDescription("Full Meal that matches the provided summary")
        val meal: Meal,
    )

    override val argsSerializer: KSerializer<Args> = Args.serializer()
    override val resultSerializer: KSerializer<Result> = Result.serializer()

    override val name: String = "meal_lookup"
    override val description: String = "Returns a full Meal corresponding to the provided MealSummary."

    override suspend fun execute(args: Args): Result =
        Result(
            meal = recipeRepository.getMealById(args.mealSummary.id).getOrThrow()
        )
}
