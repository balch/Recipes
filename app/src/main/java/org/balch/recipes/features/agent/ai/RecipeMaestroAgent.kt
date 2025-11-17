package org.balch.recipes.features.agent.ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.GraphAIAgent.FeatureContext
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import androidx.navigation3.runtime.NavKey
import com.diamondedge.logging.logging
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import org.balch.recipes.AiChatScreen
import org.balch.recipes.DetailRoute
import org.balch.recipes.Ideas
import org.balch.recipes.Info
import org.balch.recipes.RecipeRoute
import org.balch.recipes.Search
import org.balch.recipes.SearchRoute
import org.balch.recipes.core.ai.GeminiKeyProvider
import org.balch.recipes.core.models.DetailType
import org.balch.recipes.core.models.SearchType
import org.balch.recipes.features.agent.ChatMessage
import org.balch.recipes.features.agent.ChatMessageType
import kotlin.time.ExperimentalTime

/**
 * AI Agent specifically designed for culinary, nutrition, and recipe assistance.
 * Uses Gemini Pro 2.5 Flash to provide expert advice on recipes and food-related questions.
 */
class RecipeMaestroAgent @Inject constructor(
    private val config: RecipeMaestroConfig,
    private val geminiKeyProvider: GeminiKeyProvider,
) {

    private val logger = logging("MasterChefAgent")

    private val userIntent = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun sendUserMessage(message: String) {
        userIntent.tryEmit(message)
    }

    val initialMessage =
        ChatMessage(text = config.initialAgentMessage, type = ChatMessageType.Agent)

    private val messages = mutableListOf(initialMessage)
    private val _navigationFlow = MutableSharedFlow<RecipeRoute>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val navigationFlow = _navigationFlow.asSharedFlow()

    private var currentAgent: AIAgent<String, String>? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun runAgent(prompt: String): Flow<AgentState> = channelFlow {
        sendUserMessage(prompt)

        val strategy = config
            .agentStrategy(
                name = "MasterChefAgent",
                onAssistantMessage = { message ->
                    sendAgentMessage(message)

                    val userMessage =
                        userIntent.mapNotNull {
                            it.trim().takeIf { trimmed -> trimmed.isNotEmpty() }
                        }.first()
                    sendUserMessage(userMessage)
                    userMessage
                }
            )

        createAgent(strategy) {
            handleEvents {
                onAgentExecutionFailed { ctx ->
                    logger.error(ctx.throwable) { "Error running agent" }
                    sendErrorMessage(ctx.throwable, "Whoops!!!")
                }
            }
        }.run(prompt)

        // Keep the channel open for as long as the collector is active
        // ChannelFlow will handle closing; we just suspend here.
        awaitClose { }
    }.onEach {
        logger.info { "Agent state: $it" }
    }

    private fun ProducerScope<AgentState>.sendUserMessage(message: String) {
        messages.add(ChatMessage(text = message, type = ChatMessageType.User))
        messages.add(ChatMessage(text = "Thinking", type = ChatMessageType.Loading))
        // Always emit a fresh list instance so collectors (Compose) detect changes
        trySend(AgentState.Chatting(messages.toList())).onFailure {
            logger.error(it) { "Failed to send user message: $message" }
        }
    }

    private fun ProducerScope<AgentState>.sendAgentMessage(message: String) {
        addOrReplaceMessage(ChatMessage(text = message, type = ChatMessageType.Agent))
        // Always emit a fresh list instance so collectors (Compose) detect changes
        trySend(AgentState.Chatting(messages.toList())).onFailure {
            logger.error(it) { "Failed to send agent message: $message" }
        }
    }

    private fun ProducerScope<AgentState>.sendErrorMessage(
        exception: Throwable,
        message: String
    ) {
        addOrReplaceMessage(ChatMessage(text = message, type = ChatMessageType.Error))
        // Always emit a fresh list instance so collectors (Compose) detect changes
        trySend(AgentState.Error(exception, messages.toList())).onFailure {
            logger.error(it) { "Failed to send error message: $message" }
        }
    }

    private fun addOrReplaceMessage(message: ChatMessage) {
        val lastMessage = messages.lastOrNull()
        if (lastMessage?.type == ChatMessageType.Loading) {
            messages[messages.lastIndex] = message
        } else {
            messages.add(message)
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun createAgent(
        strategy: AIAgentGraphStrategy<String, String>,
        installFeatures: FeatureContext.() -> Unit = {},
    ): AIAgent<String, String> {
        val llmClient = GoogleLLMClient(geminiKeyProvider.apiKey!!)
        val executor = SingleLLMPromptExecutor(llmClient)

        // Create agent config with proper prompt
        val agentConfig = AIAgentConfig(
            prompt = prompt("RecipeMaestroAgent") {
                system(config.systemInstruction)
            },
            model = config.model,
            maxAgentIterations = config.maxAgentIterations
        )

        return AIAgent(
            promptExecutor = executor,
            strategy = strategy,
            agentConfig = agentConfig,
            toolRegistry = config.toolRegistry,
            installFeatures = installFeatures,
        ).also { currentAgent = it }
    }

    companion object Companion {
        /**
         * Converts the current NavKey to a descriptive context string for the AI agent
         */
        fun NavKey.toContext(): String = when (this) {
            is Ideas -> "The user is currently browsing recipe ideas and categories"
            is Search -> "The user is currently searching for recipes with query: ${search.searchText}"
            is SearchRoute -> when (searchType) {
                is SearchType.Category -> "The user is browsing recipes in category: ${searchType.searchText}"
                is SearchType.Area -> "The user is browsing recipes from area: ${searchType.searchText}"
                is SearchType.Ingredient -> "The user is browsing recipes with ingredient: ${searchType.searchText}"
                is SearchType.Search -> "The user is searching for: ${searchType.searchText}"
            }
            is DetailRoute -> when (detailType) {
                is DetailType.MealLookup -> "The user is viewing a recipe: ${detailType.mealSummary.name}"
                is DetailType.MealContent -> "The user is viewing a recipe: ${detailType.meal.name}"
                is DetailType.RandomRecipe -> "The user is viewing a random recipe"
                is DetailType.CodeRecipeContent -> "The user is viewing code recipe: ${detailType.codeRecipe.title}"
            }
            is Info -> "The user is viewing the app information screen"
            is AiChatScreen -> "The user is in the AI assistant screen"
            else -> "The user is browsing the Recipes app"
        }
    }

    sealed interface AgentState {
        val messages: List<ChatMessage>

        data class Chatting(override val messages: List<ChatMessage>): AgentState
        data class Error(
            val exception: Throwable,
            override val messages: List<ChatMessage>
        ): AgentState
        data class Loading(override val messages: List<ChatMessage>): AgentState
    }
}