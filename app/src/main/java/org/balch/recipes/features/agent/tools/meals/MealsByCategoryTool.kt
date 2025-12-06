package org.balch.recipes.features.agent.tools.meals

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.balch.recipes.core.models.MealSummary
import org.balch.recipes.core.repository.RecipeRepository
import javax.inject.Inject

/**
 * Tool for listing meals by category using RecipeRepository
 */
class MealsByCategoryTool @Inject internal constructor(
    private val recipeRepository: RecipeRepository,
) : Tool<MealsByCategoryTool.Args, MealsByCategoryTool.Result>() {

    @Serializable
    data class Args(
        @property:LLMDescription("Optional contextual information from the calling agent.")
        val callingAgentContext: String?,

        @property:LLMDescription("Category name to filter meals by (e.g., 'Dessert', 'Seafood')")
        val category: String,
    )

    @Serializable
    data class Result(
        @property:LLMDescription("List of meal summaries that belong to the specified category")
        val meals: List<MealSummary>,

        @property:LLMDescription("The category used for filtering")
        val category: String,
    )

    override val argsSerializer: KSerializer<Args> = Args.serializer()
    override val resultSerializer: KSerializer<Result> = Result.serializer()

    override val name: String = "meal_list_by_category"
    override val description: String = "Returns meals that belong to the given Category."

    override suspend fun execute(args: Args): Result =
        Result(
            meals = recipeRepository.getMealsByCategory(args.category).getOrThrow(),
            category = args.category,
        )
}
