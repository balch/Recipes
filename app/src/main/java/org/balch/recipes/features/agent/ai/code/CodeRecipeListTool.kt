package org.balch.recipes.features.agent.ai.code

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.balch.recipes.core.assets.CodeRecipeAssetLoader
import org.balch.recipes.core.models.CodeArea
import org.balch.recipes.core.models.CodeRecipeSummary
import javax.inject.Inject

/**
 * Tool for returning all code recipes in the app
 */
class CodeRecipeListTool @Inject internal constructor(
    private val codeAssetLoader: CodeRecipeAssetLoader
) : Tool<CodeRecipeListTool.Args, CodeRecipeListTool.Result>() {
    @Serializable
    data class Args(
        @property:LLMDescription("Not used as it is not needed")
        val notUsed: String
    )

    @Serializable
    data class Result(
        @property:LLMDescription("A map of all CodeRecipeSummary objects defined in the app")
        val recipesMap: Map<CodeArea,List<CodeRecipeSummary>>,
    )

    override val argsSerializer = Args.serializer()
    override val resultSerializer: KSerializer<Result> = Result.serializer()

    override val name = "code_recipe_list"
    override val description = """
        Returns a map of all CodeRecipe objects defined in the app.
        The map is organized by CodeArea and maps to a list of code recipe titles.
        User this tool to get a sense of all the recipes in the app when suggesting
        topics.
    """.trimIndent()

    override suspend fun execute(args: Args): Result =
        Result(
            recipesMap = codeAssetLoader.loadRecipesAreaMap(),
        )
}
