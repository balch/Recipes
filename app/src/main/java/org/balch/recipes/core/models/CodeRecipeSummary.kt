package org.balch.recipes.core.models

import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.Serializable

@LLMDescription("Represent the summary of a code recipe")
@Serializable
data class CodeRecipeSummary(
    @property:LLMDescription("The development area or category this code recipe belongs to (Theme, Navigation, Architecture, Testing, or Compose)")
    val area: CodeArea,

    @property:LLMDescription("The title of the code recipe")
    val title: String,

    @property:LLMDescription("The Asset name used to lookup a code recipe")
    val markdownAsset: String,

    @property:LLMDescription("Optional file name associated with this code recipe")
    val exampleFileName: String? = null,
) {
    fun toCodeRecipe(
        index: Int,
        description: String,
        codeSnippet: String,
    ) = CodeRecipe(
        index = index,
        area = area,
        title = title,
        description = description,
        fileName = exampleFileName,
        codeSnippet = codeSnippet,
    )
}
