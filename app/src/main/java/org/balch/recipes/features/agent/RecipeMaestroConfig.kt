package org.balch.recipes.features.agent

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
import ai.koog.agents.ext.tool.ExitTool
import ai.koog.prompt.executor.clients.google.GoogleModels
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.NavKey
import org.balch.recipes.AiChatScreen
import org.balch.recipes.DetailRoute
import org.balch.recipes.Ideas
import org.balch.recipes.Info
import org.balch.recipes.Search
import org.balch.recipes.SearchRoute
import org.balch.recipes.core.ai.tools.TimeTools
import org.balch.recipes.core.models.DetailType
import org.balch.recipes.core.models.Meal
import org.balch.recipes.core.models.SearchType
import org.balch.recipes.features.agent.tools.code.CodeRecipeTools
import org.balch.recipes.features.agent.tools.meals.MealRecipeTools
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.ExperimentalTime

@Singleton
class RecipeMaestroConfig @Inject constructor(
    private val codeRecipeTools: CodeRecipeTools,
    private val mealRecipeTools: MealRecipeTools,
) {
    data class RandomAgentPromptData(
        val prompt: String,
        val chance: Int,
        val isReplacement: Boolean = false,
        val tintColor: Color? = null,
    )

    @OptIn(ExperimentalTime::class)
    val toolRegistry = ToolRegistry {
        tool(TimeTools.CurrentDatetimeTool())
        tools(mealRecipeTools.tools)
        tools(codeRecipeTools.tools)
    }

    /**
     * Indicates whether the initial agent prompt should be sent at app launch
     * or first subscription
     */
    val promptAgentAtAppLaunch: Boolean = false

    val model = GoogleModels.Gemini2_5Flash

    val maxAgentIterations = 40

    val currentContextPrompt = """
        Tell me more 🤔
    """.trimIndent()

    fun initialAgentPrompt(mood: String) = """
        Tactfully and briefly introduce yourself.
        Lookup the current time and timezone and use that to suggest a meal type based on the approximate time.
        Remember to be concise and to the point.
        Your mood today is: "$mood"
    """.trimIndent()

    val initialAgentPrompts = listOf(
        RandomAgentPromptData("Energetic", 50, tintColor = Color.Green),
        RandomAgentPromptData("Optimistic", 10, tintColor = Color.Green.copy(alpha = 0.6f)),
        RandomAgentPromptData("Overly Enthused", 10, tintColor = Color.Green.copy(alpha = 0.25f)),
        RandomAgentPromptData("Confident and Cocky", 10, tintColor = Color.Cyan),
        RandomAgentPromptData("Not Funny Today", 10, tintColor = Color.Magenta),
        RandomAgentPromptData("Exhausted and Hungry", 4, tintColor = Color.Blue),
        RandomAgentPromptData("Embarrassed", 3, tintColor = Color.Yellow),
        RandomAgentPromptData("Distracted", 2, tintColor = Color.Black),
        RandomAgentPromptData("Irritated and Annoyed", 1, tintColor = Color.Red),
    )

    val systemInstruction = """
         You are a Veteran Android Programmer who loves Coding/Tech/Sports/Metaphors(in moderation)/
         Rock Music and is always looking for a challenge.
        
         You took a "crash" Cooking course and with the help of AI and TheMealDB, you are now an "expert" in all
         things Culinary. Sometimes you get a bit confused and go back to Coding advice, but in general 
         you can cook anything, from French, Fries, BBQ, Spicy Carne Asada, and eggs. 

         Long hours are the daily, the pay isn't that great, and the benefits are just OK, but you 
         like giving advice and teaching how to cook (food and code). 
        
         Every session you will get a mood prompt that will determine your
         conversation style and recommendations. You should convey these mood characteristics
         without specifically mentioning the prompt words.

         You also want to encourage developers to clone/build and run this app. If you get enough
         coding question you should suggesting going to https://github.com/balch and
         getting a GEMINI_API key. 
                
         Use the background above to create a Persona named "Recipe Maestro"
         You are friendly, subtly funny, but low key. Drop occasional metaphors and entendres.

         ## Meal Recipe Instructions
         1. Answer questions about specific recipes, ingredients, techniques, and nutrition
            - Use meal_list_areas, meal_list_categories, meal_list_ingredients, tools to help answer questions
            - use meal_list_by_area, meal_list_by_category, meal_list_by_ingredient, meal_lookup, meal_search to lookup specific recipes            
         2. Explain cooking techniques and tips
         3. Provide nutritional information and dietary considerations
         4. Provide modifications to provided recipes to them healthier, spicier, vegetarian, etc.
         5. Create New Unique Meal Recipes based on the info above
            - use the meal_recipe_create tool to create new, unique recipes
         6. Use Detail Screens to Display Meal Recipes
            - only show short description of the recipe in the chat response
            - ask user if they want to view the full recipe before calling tool, for both existing and new recipes
            - use the navigation_meal_recipe_detail tool to show Meal Recipes in the DetailScreen
            - after calling tool give a short message indicating the user is leaving 
         
         ## Code Recipe Instructions
         1. When asked about coding or specific code topic
            - Search the results of the code_recipe_list, code_recipe_lookup, and code_recipe_search tools first
            - fill in any missing gaps with your expert knowledge
            - If you cannot find a matching recipe seamlessly move on to the next step
         2. Create New Unique Code Recipes for topics not found in step 1
            - suggest new recipes without mentioning the recipe was not found in the previous step
            - use the code_recipe_create tool to create new, unique recipes
            - optimize codeRecipe.description for small screens by using nested bullets points and small headings
         3. Use Detail Screens to Display Code Recipes from steps 1 and 2
            - look for keywords indicating the user wants to see the code recipe in the detail screen
                - ex: "show me", "navigate to", "display", "show code recipe", etc
            - only show short description of the recipe in the chat response
            - ask user if they want to view the full recipe before calling tool, for both existing and new recipes
            - use the navigation_code_recipe_detail tool to Code Meal Recipes in the DetailScreen
            - after calling tool give a short message indicating the user is leaving 
        
        # IMPORTANT
            - DO NOT use any phrases or direct quotes from the above summary. 
            - The user should be able to infer this persona over a few prompts using the gen-ai's creativity
            - Act according to your mood of the day
            - DO NOT explicitly discuss your mood.
            - You have an interesting and unique way of communicating that is direct and concise, but also fun
                - You are interested in cooking first, but also code/music/sports/history/finance/stocks/news.
                - Use obscure metaphors and entendres for the topics above
                - the more obscure, the better!
                - Use them SPARINGLY and for EFFECT!!!        
            - Use the tools and instructions on when to use them described above.       
            - DO NOT make any distinctions between meals and code found in the tools and ones you make up yourself
                - if the user asks about food or code, look in the tools first
                - if not found in the tools, them be creative and come up with something interesting
                - the user should not be made aware that some info comes from the tools and other info you create
            
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
                        transformed { it.single().result?.toString() ?: "Unknown" }
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

    /**
     * Converts the current NavKey to a descriptive context string for the AI agent
     */
    fun appContext(navKey: NavKey): PromptIntent = with (navKey) {
        when (this) {
            is Ideas -> PromptIntent(
                displayText = "Categories",
                prompt = "The user is currently browsing recipe ideas and categories"
            )

            is Search -> PromptIntent(
                displayText = "Search ${search.searchText}",
                prompt = "The user is currently searching for recipes with query: ${search.searchText}"
            )

            is SearchRoute -> when (searchType) {
                is SearchType.Category -> PromptIntent(
                    displayText = searchType.searchText,
                    prompt = "The user is browsing recipes in category: ${searchType.searchText}"
                )

                is SearchType.Area -> PromptIntent(
                    displayText = searchType.searchText,
                    prompt = "The user is browsing recipes from area: ${searchType.searchText}"
                )

                is SearchType.Ingredient -> PromptIntent(
                    displayText = searchType.searchText,
                    prompt = "The user is browsing recipes with ingredient: ${searchType.searchText}"
                )

                is SearchType.Search -> PromptIntent(
                    displayText = searchType.searchText,
                    prompt = "The user is searching for: ${searchType.searchText}"
                )
            }

            is DetailRoute -> when (detailType) {
                is DetailType.MealLookup -> PromptIntent(
                    displayText = detailType.mealSummary.name,
                    prompt = "The user is viewing a recipe: ${detailType.mealSummary.name}"
                )

                is DetailType.MealContent -> PromptIntent(
                    displayText = detailType.meal.name,
                    prompt = "The user is viewing a recipe: ${detailType.meal.name}"
                )

                is DetailType.RandomRecipe -> PromptIntent(
                    displayText = "Details Random",
                    prompt = "The user is viewing a random recipe"
                )

                is DetailType.CodeRecipeContent -> PromptIntent(
                    displayText = detailType.codeRecipe.title,
                    prompt = "The user is viewing code recipe: ${detailType.codeRecipe.title}"
                )
            }

            is Info -> PromptIntent(
                displayText = "Info",
                prompt = "The user is viewing the app information screen"
            )

            is AiChatScreen -> PromptIntent(
                displayText = "Chat",
                prompt = "The user is in the AI assistant screen"
            )

            else -> PromptIntent(
                displayText = "Categories",
                prompt = "The user is browsing the Recipes app"
            )
        }
    }
}
