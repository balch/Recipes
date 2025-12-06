package org.balch.recipes.features.agent.tools.code

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.balch.recipes.core.models.CodeArea
import org.balch.recipes.core.models.CodeRecipe
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Reinforcement tool to remind the AI to create unique code recipes.
 */
class CodeRecipeCreateTool @Inject internal constructor(
) : Tool<CodeRecipeCreateTool.Args, CodeRecipeCreateTool.Result>() {
    @Serializable
    data class Args(
        @property:LLMDescription("Optional contextual information from the calling agent.")
        val callingAgentContext: String?,
        @property:LLMDescription("The Category of the code recipe")
        val area: CodeArea,
        @property:LLMDescription("The title of the code recipe")
        val title: String,
        @property:LLMDescription("A description explaining what this code recipe demonstrates using markdown. Use bullet point (-) format for description info. Only use (####) tags for headings.")
        val markdownDescription: String,
        @property:LLMDescription("The actual code snippet or example code used in the markdown. Use a triple backtick (```) to surround code")
        val markdownCodeSnippet: String = "",
    )

    @Serializable
    data class Result(
        @property:LLMDescription("List of recipes matching the search term")
        val codeRecipe: CodeRecipe,
    )

    override val argsSerializer = Args.serializer()
    override val resultSerializer: KSerializer<Result> = Result.serializer()

    override val name = "code_recipe_create"
    override val description = "Returns a CodeRecipe for the ai to navigate to. Remember to use proper markdown code syntax in the codeSnippet parameter"

    @OptIn(ExperimentalTime::class)
    override suspend fun execute(args: Args): Result =
        Result(
            codeRecipe = CodeRecipe(
                index = CLOCK.now().epochSeconds.toInt(),
                title = args.title,
                codeSnippet = args.markdownCodeSnippet,
                description = args.markdownDescription,
                area = args.area,
                aiGenerated = true,
            )
        )

    companion object {
        @OptIn(ExperimentalTime::class)
        private val CLOCK = Clock.System
    }

}
