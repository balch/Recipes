package org.balch.recipes.core.ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.functionalStrategy
import ai.koog.agents.core.dsl.extension.asAssistantMessage
import ai.koog.agents.core.dsl.extension.containsToolCalls
import ai.koog.agents.core.dsl.extension.executeMultipleTools
import ai.koog.agents.core.dsl.extension.extractToolCalls
import ai.koog.agents.core.dsl.extension.requestLLMMultiple
import ai.koog.agents.core.dsl.extension.sendMultipleToolResults
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.llm.LLModel
import com.diamondedge.logging.logging
import org.balch.recipes.BuildConfig


enum class AgentModel(internal val llm: LLModel) {
    GEMINI_PRO(GoogleModels.Gemini2_5Pro),
    GEMINI_FLASH(GoogleModels.Gemini2_5Flash),
    GEMINI_FLASH_LIGHT(GoogleModels.Gemini2_5FlashLite),
}

abstract class Agent {

    abstract val systemInstruction: String
    abstract val agentModel: AgentModel
    abstract val toolRegistry: ToolRegistry

    val isModelReady: Boolean by lazy { model != null }

    private val model: AIAgent<String, String>? by lazy {
        requireNotNull(apiKey) { "GEMINI_API_KEY is not set." }

        try {
            AIAgent(
                systemPrompt = systemInstruction,
                promptExecutor = simpleGoogleAIExecutor(apiKey = apiKey),
                llmModel = agentModel.llm,
                toolRegistry = toolRegistry,
                strategy = functionalStrategy { input ->
                    // Send the user input to the LLM
                    var responses = requestLLMMultiple(input)

                    // Only loop while the LLM requests tools
                    while (responses.containsToolCalls()) {
                        // Extract tool calls from the response
                        val pendingCalls = extractToolCalls(responses)
                        // Execute the tools and return the results
                        val results = executeMultipleTools(pendingCalls)
                        // Send the tool results back to the LLM. The LLM may call more tools or return a final output
                        responses = sendMultipleToolResults(results)
                    }

                    // When no tool calls remain, extract and return the assistant message content from the response
                    responses.single().asAssistantMessage().content
                }
            )
        } catch (e: Exception) {
            logger.e(e) {"Error initializing GenerativeModel."}
            null
        }
    }

    /**
     * Send a message to the AI agent and get a response
     */
    suspend fun chat(message: String): String {
        return try {
            model?.run(message)
                ?: "I'm sorry, I couldn't generate a response. Please try again."
        } catch (e: Exception) {
            logger.e(e) { "Error generating response." }
            "I'm sorry, I encountered an error. Please try again."
        }
    }

    companion object {

        private val logger = logging("Agent")

        private val apiKey = BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() }
        val isApiKeySet: Boolean by lazy {
            (apiKey != null).also { b ->
                if (!b) logger.w {"GEMINI_API_KEY is not set." }
                else logger.d {"GEMINI_API_KEY is SET!!!." }
            }
        }
    }
}