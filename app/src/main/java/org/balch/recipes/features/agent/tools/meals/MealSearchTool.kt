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
import org.balch.recipes.core.repository.RecipeRepository

/**
 * Tool for searching meals by name/query using RecipeRepository
 */
@MealTool
@ContributesIntoSet(AppScope::class, binding<Tool<*, *>>())
@Inject
class MealSearchTool(
    private val recipeRepository: RecipeRepository,
) : Tool<MealSearchTool.Args, MealSearchTool.Result>() {

    @Serializable
    data class Args(
        @property:LLMDescription("Optional contextual information from the calling agent.")
        val callingAgentContext: String?,

        @property:LLMDescription(
            """
                Search term used to find meals by name. Use a short phrase or a single word
                that appears in the meal name.
            """
        )
        val searchTerm: String,
    )

    @Serializable
    data class Result(
        @property:LLMDescription("List of full Meal objects matching the query")
        val meals: List<Meal>,

        @property:LLMDescription("The query used for searching")
        val searchTerm: String,
    )

    override val argsSerializer: KSerializer<Args> = Args.serializer()
    override val resultSerializer: KSerializer<Result> = Result.serializer()

    override val name: String = "meal_search"
    override val description: String = "Search for meals by name using TheMealDB via the repository."

    override suspend fun execute(args: Args): Result =
        Result(
            meals = recipeRepository.searchMeals(args.searchTerm).getOrDefault(emptyList()),
            searchTerm = args.searchTerm,
        )
}
