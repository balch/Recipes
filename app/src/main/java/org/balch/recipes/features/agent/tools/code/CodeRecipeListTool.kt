package org.balch.recipes.features.agent.tools.code

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import dev.zacsweers.metro.Inject
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.balch.recipes.core.assets.CodeRecipeAssetLoader
import org.balch.recipes.core.models.CodeArea
import org.balch.recipes.core.models.CodeRecipeSummary

/**
 * Tool for returning all code recipes in the app
 */
class CodeRecipeListTool @Inject constructor(
    private val codeAssetLoader: CodeRecipeAssetLoader
) : Tool<CodeRecipeListTool.Args, CodeRecipeListTool.Result>() {
    @Serializable
    data class Args(
        @property:LLMDescription("Optional contextual information from the calling agent.")
        val callingAgentContext: String?
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
