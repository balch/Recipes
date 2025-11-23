package org.balch.recipes.features.agent.ai.meals

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.balch.recipes.DetailRoute
import org.balch.recipes.core.models.DetailType
import org.balch.recipes.core.models.Meal
import org.balch.recipes.core.navigation.NavigationRouter
import javax.inject.Inject

/**
 * Tool for Routine to a Meal Recipe
 */
class MealRecipeDetailTool @Inject internal constructor(
    private val navigationRouter: NavigationRouter,
) : Tool<MealRecipeDetailTool.Args, MealRecipeDetailTool.Result>() {

    @Serializable
    @LLMDescription("Navigate to the DetailScreen for the specified Meal")
    data class Args(
        @property:LLMDescription("Optional contextual information from the calling agent.")
        val callingAgentContext: String?,

        @property:LLMDescription("The Meal meal_recipe_create tools.\n")
        val meal: Meal,
    )

    @Serializable
    data class Result(
        @property:LLMDescription("Contains status of the navigation operation")
        val success: Boolean,

        @property:LLMDescription("Message describing the result of the navigation operation")
        val message: String
    )

    override val argsSerializer = Args.serializer()
    override val resultSerializer: KSerializer<Result> = Result.serializer()

    override val name = "navigation_meal_recipe_detail"
    override val description = """
        Navigates the application to the specified Meal.
        Used to display Meal Recipes from user requests.
        The input comes from the meal_recipe_create tools.
    """.trimIndent()

    override suspend fun execute(args: Args): Result {
        val detailRoute = DetailRoute(DetailType.MealContent(args.meal))
        val success = navigationRouter.navigateTo(detailRoute)
        return Result(
            success = success,
            message = if (success) "Success" else "Failed to navigate to $detailRoute"
        )
    }
}
