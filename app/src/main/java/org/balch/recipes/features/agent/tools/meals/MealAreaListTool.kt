package org.balch.recipes.features.agent.tools.meals

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.balch.recipes.core.models.Area
import org.balch.recipes.core.repository.RecipeRepository
import javax.inject.Inject

/**
 * Tool for returning all cuisine areas from TheMealDB via the repository
 */
class MealAreaListTool @Inject internal constructor(
    private val recipeRepository: RecipeRepository,
) : Tool<MealAreaListTool.Args, MealAreaListTool.Result>() {

    @Serializable
    data class Args(
        @property:LLMDescription("Optional contextual information from the calling agent.")
        val callingAgentContext: String?,
    )

    @Serializable
    data class Result(
        @property:LLMDescription("List of cuisine areas available for filtering/searching")
        val areas: List<Area>,
    )

    override val argsSerializer: KSerializer<Args> = Args.serializer()
    override val resultSerializer: KSerializer<Result> = Result.serializer()

    override val name: String = "meal_list_areas"
    override val description: String = "Returns the list of cuisine Areas (e.g., Italian, American)."

    override suspend fun execute(args: Args): Result =
        Result(
            areas = recipeRepository.getAreas().getOrThrow()
        )
}
