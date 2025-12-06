package org.balch.recipes.features.agent.tools.code

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.balch.recipes.core.models.CodeRecipe
import org.balch.recipes.features.CodeRecipeRepository
import javax.inject.Inject

/**
 * Tool for searching for app code recipes.
 */
class CodeRecipeSearchTool @Inject internal constructor(
    private val codeRecipeRepository: CodeRecipeRepository
) : Tool<CodeRecipeSearchTool.Args, CodeRecipeSearchTool.Result>() {
    @Serializable
    data class Args(
        @property:LLMDescription("Optional contextual information from the calling agent.")
        val callingAgentContext: String?,

        @property:LLMDescription(
            """
                Query input term to search for app code recipes for. The 
                search term can only be a single word and will be used to
                search CodeRecipe objects that are defined in the title,
                description and code.
                """
        )
        val searchTerm: String
    )

    @Serializable
    data class Result(
        @property:LLMDescription("List of recipes matching the search term")
        val recipes: List<CodeRecipe>,

        @property:LLMDescription("The search term used to find recipes")
        val searchTerm: String
    )

    override val argsSerializer = Args.serializer()
    override val resultSerializer: KSerializer<Result> = Result.serializer()

    override val name = "code_recipe_search"
    override val description = "Search the CodeRecipe objects defined in the app"

    override suspend fun execute(args: Args): Result =
        Result(
            recipes = codeRecipeRepository.searchRecipes((args.searchTerm)),
            searchTerm = args.searchTerm
        )
}
