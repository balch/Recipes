package org.balch.recipes.features.agent.ai

import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.Serializable
import org.balch.recipes.core.models.CodeRecipe
import org.balch.recipes.core.models.Meal

data class AppContextData(
    val displayText: String,
    val prompt: String,
)

@LLMDescription("Prompt structure sent to the AI agent")
@Serializable
data class RecipeMaestroPrompt(
    @property:LLMDescription("The input provided by the user")
    val userInput: String,
    @property:LLMDescription("Optional application context data describing what the user is looking at in the app")
    val appContextData: String? = null,
    @property:LLMDescription("Optional code recipe the user wants to discuss")
    val codeRecipe: CodeRecipe? = null,
    @property:LLMDescription("Optional meal  the user wants to discuss")
    val meal: Meal? = null,
)

@LLMDescription("Represents a the temperament of the agent's response (e.g. HAPPY, SAD, etc. )")
enum class AgentTemperament {
    SAD, HAPPY, NEUTRAL, TIRED, EXUBERANT, ANGRY, NONCHALANT
}

@LLMDescription("Response structure received from the AI agent")
@Serializable
data class RecipeMaestroResponse(
    @property:LLMDescription("The textual response from the agent")
    val agentResponse: String,
    @property:LLMDescription("The temperament or tone of the agent's response")
    val agentTemperament: AgentTemperament,
    @property:LLMDescription("Optional code snippet returned by the agent")
    val codeSnippet: String? = null,
    @property:LLMDescription("Optional code recipe object returned by the agent")
    val codeRecipe: CodeRecipe? = null,
    @property:LLMDescription("Optional meal information returned by the agent")
    val meal: Meal? = null,
)
