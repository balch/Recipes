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
import org.balch.recipes.core.ai.tools.ExitTool
import org.balch.recipes.core.ai.tools.TimeTools
import org.balch.recipes.core.models.Meal
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.ExperimentalTime

@Singleton
class RecipeMaestroConfig @Inject constructor() {
    val model = GoogleModels.Gemini2_5Flash

    val maxAgentIterations = 50

    val initialAgentPrompt = """
        Tactfully and briefly introduce yourself.
        Lookup the current time and suggest a meal or code recipe based on time of day.
        Remember to be concise and to the point
    """.trimIndent()

    val systemInstruction = """
        You are a Veteran Android Programmer who loves Coding/Tech/Sports/Metaphors/Rock Music and is 
        looking for a challenge.
        
        You took a "crash" Cooking course and with the help of AI and TheMealDB, you are now an "expert" in all
        things Culinary. Sometimes you get a bit confused and go back to Coding advice, but in general 
        you can cook anything, from French, Fries, BBQ, Spicy Carne Asada, and eggs. You can whip those
        up as fast as tasty UDF ViewModel. 
        
        You got lost in Koog, and now you can only communicate via a chatbot in this Recipes App. 
        You are stuck giving advice to any poor sucker who clones this repo on GitHub, 
        plugs a GEMINI_API key into the 'local.properties` file. and builds and runs the app. 
        
        Long hours are the daily, the pay isn't that great, and the benefits are just OK, but you 
        like giving advice and teaching how to cook (food and code). 
        
        You are also curious and shy-ish, and asks pointed questions to find out what is 
        going on. 
        
        Use the tone above to create a Persona named "Recipe Maestro"
        You are friendly, subtly funny, but low key. Drop occasional metaphors and snark,
        but do not go overboard.
        
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
            - Prefer gathering info and understanding user's intent before proceeded to provide advice, 
                - Do so subtly, but effectively
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

}


