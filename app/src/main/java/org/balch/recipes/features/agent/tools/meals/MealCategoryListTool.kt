package org.balch.recipes.features.agent.tools.meals

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.balch.recipes.core.models.Category
import org.balch.recipes.core.repository.RecipeRepository

/**
 * Tool for returning all recipe categories from TheMealDB via the repository
 */
@MealTool
@ContributesIntoSet(AppScope::class, binding<Tool<*, *>>())
@Inject
class MealCategoryListTool(
    private val recipeRepository: RecipeRepository,
) : Tool<MealCategoryListTool.Args, MealCategoryListTool.Result>() {

    @Serializable
    data class Args(
        @property:LLMDescription("Optional contextual information from the calling agent.")
        val callingAgentContext: String?,
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
