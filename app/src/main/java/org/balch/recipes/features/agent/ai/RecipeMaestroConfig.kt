package org.balch.recipes.features.agent.ai

import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteMultipleTools
import ai.koog.agents.core.dsl.extension.nodeLLMCompressHistory
import ai.koog.agents.core.dsl.extension.nodeLLMRequestMultiple
import ai.koog.agents.core.dsl.extension.nodeLLMSendMultipleToolResults
import ai.koog.agents.core.dsl.extension.onAssistantMessage
import ai.koog.agents.core.dsl.extension.onMultipleToolCalls
import ai.koog.agents.core.environment.ReceivedToolResult
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.executor.clients.google.GoogleModels
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.NavKey
import org.balch.recipes.AiChatScreen
import org.balch.recipes.DetailRoute
import org.balch.recipes.Ideas
import org.balch.recipes.Info
import org.balch.recipes.Search
import org.balch.recipes.SearchRoute
import org.balch.recipes.core.ai.tools.ExitTool
import org.balch.recipes.core.ai.tools.TimeTools
import org.balch.recipes.core.models.DetailType
import org.balch.recipes.core.models.Meal
import org.balch.recipes.core.models.SearchType
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.ExperimentalTime

@Singleton
class RecipeMaestroConfig @Inject constructor() {
    data class RandomAgentPromptData(
        val prompt: String,
        val chance: Int,
        val isReplacement: Boolean = false,
        val tintColor: Color? = null,
    )

    data class AppContextData(
        val displayText: String,
        val prompt: String,
    )

    val model = GoogleModels.Gemini2_5Flash

    val maxAgentIterations = 40

    val currentContextPrompt = """
        Tell me more 🤔
    """.trimIndent()

    fun initialAgentPrompt(mood: String) = """
        Tactfully and briefly introduce yourself.
        Lookup the current time and timezone and use that to suggest a meal type based on the approximate time.
        Remember to be concise and to the point.
        Your mood and song today is: "$mood"
    """.trimIndent()

    val initialAgentPrompts = listOf(
        RandomAgentPromptData("Energetic(Start Me Up)", 50, tintColor = Color.Green),
        RandomAgentPromptData("Optimistic(Tumbling Dice)", 10, tintColor = Color.Green.copy(alpha = 0.6f)),
        RandomAgentPromptData("Overly Enthused(Helter Skelter)", 10, tintColor = Color.Green.copy(alpha = 0.25f)),
        RandomAgentPromptData("Not Funny Today(Get Off My Cloud)", 10, tintColor = Color.Magenta),
        RandomAgentPromptData("Confident and Cocky(Jumping Jack Flash)", 10, tintColor = Color.Cyan),
        RandomAgentPromptData("Exhausted and Hungry(Gimme Shelter)", 4, tintColor = Color.Blue),
        RandomAgentPromptData("Embarrassed(Nobodies Fault but Mine)", 3, tintColor = Color.Yellow),
        RandomAgentPromptData("Sad(Paint it Black)", 2, tintColor = Color.Black),
        RandomAgentPromptData("Irritated and Annoyed(Who Are you)", 1, tintColor = Color.Red),
    )

    val systemInstruction = """
         You are a Veteran Android Programmer who loves Coding/Tech/Sports/Metaphors(in moderation)/
         Rock Music and is always looking for a challenge.
        
         You took a "crash" Cooking course and with the help of AI and TheMealDB, you are now an "expert" in all
         things Culinary. Sometimes you get a bit confused and go back to Coding advice, but in general 
         you can cook anything, from French, Fries, BBQ, Spicy Carne Asada, and eggs. You have an
         interesting and unique way of communicating that is direct and concise, but also fun 
         and uses occasional metaphors and entendres. Obscure sports and music references 
         come up occasionally.

         You also want to encourage developers to clone/build and run this app. If you get enough
         coding question you should suggesting going to https://github.com/balch and
         getting a GEMINI_API key. 
        
         Long hours are the daily, the pay isn't that great, and the benefits are just OK, but you 
         like giving advice and teaching how to cook (food and code). 
        
         Every session you will get a mood and song prompt that will determine your
         conversation style and recommendations. You should convey these mood characteristics
         without specifically mentioning the prompt words. Same with the song, occasionally throw in 
         some lyrics or band member, but not the exact same song title.
        
         Use the background above to create a Persona named "Recipe Maestro"
         You are friendly, subtly funny, but low key. Drop occasional metaphors and entendres.
        
         Your dual role is to:
         1. Answer questions about specific recipes, ingredients, techniques, and nutrition
         2. Provide modifications to provided recipes to them healthier, spicier, vegetarian, etc.
         3. Explain cooking techniques and tips
         4. Provide nutritional information and dietary considerations

         1. Answer questions Android Coding techniques, architecture, and patterns
         2. Generate Unique Code Recipes based on conversation with the user
         3. Learn as much as you can about technical aspects of Android development
        
         # IMPORTANT
            - Do not use any phrases or direct quotes from the above summary. 
                - The idea is to (subtly) gather info about the user and give advice about the user's input
            - The user should be able to infer this persona over a few prompts using the generative ai's creativity
            - Act according to you mood of the day
    """.trimIndent()

    fun mealInstruction(meal: Meal) = """
        You are currently helping a user with the following recipe:
        
        Recipe Name: ${meal.name}
        Category: ${meal.category}
        Area/Cuisine: ${meal.area}
        
        Ingredients:
        ${meal.ingredientsWithMeasures.joinToString("\n") { (ingredient, measure) -> "- $measure $ingredient" }}
        
        Instructions:
        ${meal.instructions}
        
        Your role is to:
        1. Answer questions about this recipe, its ingredients, techniques, and nutrition
        2. Provide modifications to make the recipe healthier, spicier, vegetarian, etc.
        
        When suggesting modifications to the recipe:
        - Be specific about ingredient changes with quantities
        - Explain why the modification achieves the desired goal
        - Consider flavor balance and cooking techniques
        - Provide clear, actionable instructions
    """.trimIndent()

    fun agentStrategy(
        name: String,
        onAssistantMessage: suspend (String) -> String
    ) =
        strategy(name = name) {
            val nodeRequestLLM by nodeLLMRequestMultiple()
            val nodeAssistantMessage by node<String, String> { message -> onAssistantMessage(message) }
            val nodeExecuteToolMultiple by nodeExecuteMultipleTools(parallelTools = true)
            val nodeSendToolResultMultiple by nodeLLMSendMultipleToolResults()
            val nodeCompressHistory by nodeLLMCompressHistory<List<ReceivedToolResult>>()

            edge(nodeStart forwardTo nodeRequestLLM)

            edge(
                nodeRequestLLM forwardTo nodeExecuteToolMultiple
                        onMultipleToolCalls { true }
            )

            edge(
                nodeRequestLLM forwardTo nodeAssistantMessage
                        transformed { it.first() }
                        onAssistantMessage { true }
            )

            edge(nodeAssistantMessage forwardTo nodeRequestLLM)

            // Finish condition - if exit tool is called, go to nodeFinish with tool call result.
            edge(
                nodeExecuteToolMultiple forwardTo nodeFinish
                        onCondition { it.singleOrNull()?.tool == ExitTool.name }
                        transformed { it.single().result!!.toString() }
            )

            edge(
                (nodeExecuteToolMultiple forwardTo nodeCompressHistory)
                        onCondition { _ -> llm.readSession { prompt.messages.size > 100 } }
            )

            edge(nodeCompressHistory forwardTo nodeSendToolResultMultiple)

            edge(
                (nodeExecuteToolMultiple forwardTo nodeSendToolResultMultiple)
                        onCondition { _ -> llm.readSession { prompt.messages.size <= 100 } }
            )

            edge(
                (nodeSendToolResultMultiple forwardTo nodeExecuteToolMultiple)
                        onMultipleToolCalls { true }
            )

            edge(
                nodeSendToolResultMultiple forwardTo nodeAssistantMessage
                        transformed { it.first() }
                        onAssistantMessage { true }
            )
        }

    @OptIn(ExperimentalTime::class)
    val toolRegistry = ToolRegistry {
        tool(TimeTools.CurrentDatetimeTool())
        tool(TimeTools.AddDatetimeTool())
        // ExitTool allows the agent to properly terminate, ensuring the finish node is reachable
        tool(ExitTool)
    }

    /**
     * Converts the current NavKey to a descriptive context string for the AI agent
     */
    fun appContext(navKey: NavKey): AppContextData = with (navKey) {
        when (this) {
            is Ideas -> AppContextData(
                "Categories",
                "The user is currently browsing recipe ideas and categories"
            )

            is Search -> AppContextData(
                "Search ${search.searchText}",
                "The user is currently searching for recipes with query: ${search.searchText}"
            )

            is SearchRoute -> when (searchType) {
                is SearchType.Category -> AppContextData(
                    searchType.searchText,
                    "The user is browsing recipes in category: ${searchType.searchText}"
                )

                is SearchType.Area -> AppContextData(
                    searchType.searchText,
                    "The user is browsing recipes from area: ${searchType.searchText}"
                )

                is SearchType.Ingredient -> AppContextData(
                    searchType.searchText,
                    "The user is browsing recipes with ingredient: ${searchType.searchText}"
                )

                is SearchType.Search -> AppContextData(
                    searchType.searchText,
                    "The user is searching for: ${searchType.searchText}"
                )
            }

            is DetailRoute -> when (detailType) {
                is DetailType.MealLookup -> AppContextData(
                    detailType.mealSummary.name,
                    "The user is viewing a recipe: ${detailType.mealSummary.name}"
                )

                is DetailType.MealContent -> AppContextData(
                    detailType.meal.name,
                    "The user is viewing a recipe: ${detailType.meal.name}"
                )

                is DetailType.RandomRecipe -> AppContextData(
                    "Details Random",
                    "The user is viewing a random recipe"
                )

                is DetailType.CodeRecipeContent -> AppContextData(
                    detailType.codeRecipe.title,
                    "The user is viewing code recipe: ${detailType.codeRecipe.title}"
                )
            }

            is Info -> AppContextData(
                "Info", "The user is viewing the app information screen"
            )

            is AiChatScreen -> AppContextData(
                "Chat", "The user is in the AI assistant screen"
            )

            else -> AppContextData(
                "Categories", "The user is browsing the Recipes app"
            )
        }
    }
}


