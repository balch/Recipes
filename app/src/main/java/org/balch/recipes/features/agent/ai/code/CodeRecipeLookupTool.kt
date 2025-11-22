package org.balch.recipes.features.agent.ai.code

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.balch.recipes.core.assets.CodeRecipeAssetLoader
import org.balch.recipes.core.models.CodeArea
import org.balch.recipes.core.models.CodeRecipe
import org.balch.recipes.core.models.CodeRecipeSummary
import javax.inject.Inject

/**
 * Tool for returning a code recipe from a CodeRecipeSummary
 */
class CodeRecipeLookupTool @Inject internal constructor(
    private val codeAssetLoader: CodeRecipeAssetLoader
) : Tool<CodeRecipeLookupTool.Args, CodeRecipeLookupTool.Result>() {
    @Serializable
    data class Args(
        @property:LLMDescription("Used to to return a CodeRecipe from a CodeRecipeSummary")
        val codeRecipeSummary: CodeRecipeSummary,
    )

    @Serializable
    data class Result(
        @property:LLMDescription("Code Recipe that matches the CodeRecipeSummary")
        val codeRecipe: CodeRecipe,
    )

    override val argsSerializer = Args.serializer()
    override val resultSerializer: KSerializer<Result> = Result.serializer()

    override val name = "code_recipe_lookup"
    override val description = """
        Returns a CodeRecipe object based on a CodeRecipeSummary.
        Use this tool to get a CodeRecipe from the results of the
         code_recipe_list tool
    """.trimIndent()

    override suspend fun execute(args: Args): Result =
        Result(
            codeRecipe = codeAssetLoader.getCodeRecipe(args.codeRecipeSummary)
        )
}
