package org.balch.recipes.features.agent.ai.meals

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.balch.recipes.core.models.Category
import org.balch.recipes.core.repository.RecipeRepository
import javax.inject.Inject

/**
 * Tool for returning all recipe categories from TheMealDB via the repository
 */
class MealCategoryListTool @Inject internal constructor(
    private val recipeRepository: RecipeRepository,
) : Tool<MealCategoryListTool.Args, MealCategoryListTool.Result>() {

    @Serializable
    data class Args(
        @property:LLMDescription("Not used; included for consistency with tool interface")
        val notUsed: String = "",
    )

    @Serializable
    data class Result(
        @property:LLMDescription("List of recipe categories available for filtering/searching")
        val categories: List<Category>,
    )

    override val argsSerializer: KSerializer<Args> = Args.serializer()
    override val resultSerializer: KSerializer<Result> = Result.serializer()

        override val name: String = "meal_list_categories"
    override val description: String = "Returns the list of meal Categories (e.g., Dessert, Seafood)."

    override suspend fun execute(args: Args): Result =
        Result(
            categories = recipeRepository.getCategories().getOrThrow()
        )
}
