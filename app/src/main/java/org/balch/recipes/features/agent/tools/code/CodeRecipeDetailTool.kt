package org.balch.recipes.features.agent.tools.code

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.balch.recipes.DetailRoute
import org.balch.recipes.core.models.CodeRecipe
import org.balch.recipes.core.models.DetailType
import org.balch.recipes.core.navigation.NavigationRouter
import javax.inject.Inject

/**
 * Tool for Routine to a CodeRecipe or CodeRecipeSummary
 */
class CodeRecipeDetailTool @Inject internal constructor(
    private val navigationRouter: NavigationRouter,
) : Tool<CodeRecipeDetailTool.Args, CodeRecipeDetailTool.Result>() {

    @Serializable
    @LLMDescription("Navigate to the DetailScreen for the specified CodeRecipe")
    data class Args(
        @property:LLMDescription("Optional contextual information from the calling agent.")
        val callingAgentContext: String?,
        @property:LLMDescription("The codeRecipe from the code_recipe_create, code_recipe_lookup and code_recipe_search tools.\n")
        val codeRecipe: CodeRecipe,
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

    override val name = "navigation_code_recipe_detail"
    override val description = """
        Navigates the application to the specified CodeRecipe.
        Used to display Code Recipes from user requests.
        The input comes from the code_recipe_create, code_recipe_lookup and code_recipe_search tools.
    """.trimIndent()

    override suspend fun execute(args: Args): Result {
        val detailRoute = DetailRoute(DetailType.CodeRecipeContent(args.codeRecipe))
        val success = navigationRouter.navigateTo(detailRoute, true)
        return Result(
            success = success,
            message = if (success) "Success" else "Failed to navigate to $detailRoute"
        )
    }
}
