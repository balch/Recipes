package org.balch.recipes.features.agent.tools.meals

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.balch.recipes.core.models.MealSummary
import org.balch.recipes.core.repository.RecipeRepository

/**
 * Tool for listing meals by area (cuisine) using RecipeRepository
 */
@MealTool
@ContributesIntoSet(AppScope::class, binding<Tool<*, *>>())
@Inject
class MealsByAreaTool(
    private val recipeRepository: RecipeRepository,
) : Tool<MealsByAreaTool.Args, MealsByAreaTool.Result>() {

    @Serializable
    data class Args(
        @property:LLMDescription("Optional contextual information from the calling agent.")
        val callingAgentContext: String?,

        @property:LLMDescription("Area/cuisine name to filter meals by (e.g., 'Italian', 'American')")
        val area: String,
    )

    @Serializable
    data class Result(
        @property:LLMDescription("List of meal summaries that belong to the specified area/cuisine")
        val meals: List<MealSummary>,

        @property:LLMDescription("The area used for filtering")
        val area: String,
    )

    override val argsSerializer: KSerializer<Args> = Args.serializer()
    override val resultSerializer: KSerializer<Result> = Result.serializer()

    override val name: String = "meal_list_by_area"
    override val description: String = "Returns meals from the specified cuisine/Area."

    override suspend fun execute(args: Args): Result =
        Result(
            meals = recipeRepository.getMealsByArea(args.area).getOrThrow(),
            area = args.area,
        )
}
