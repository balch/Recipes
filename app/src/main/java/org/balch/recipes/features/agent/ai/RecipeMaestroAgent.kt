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
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import org.balch.recipes.AiChatScreen
import org.balch.recipes.DetailRoute
import org.balch.recipes.Ideas
import org.balch.recipes.Info
import org.balch.recipes.RecipeRoute
import org.balch.recipes.Search
import org.balch.recipes.SearchRoute
import org.balch.recipes.core.ai.GeminiKeyProvider
import org.balch.recipes.core.coroutines.DispatcherProvider
import org.balch.recipes.core.models.DetailType
import org.balch.recipes.core.models.SearchType
import org.balch.recipes.features.agent.ChatMessage
import org.balch.recipes.features.agent.ChatMessageType
import kotlin.time.ExperimentalTime

data class PromptIntent(
    val prompt: String,
    val displayPrompt: String = prompt,
)

/**
 * AI Agent specifically designed for culinary, nutrition, and recipe assistance.
 * Uses Gemini Pro 2.5 Flash to provide expert advice on recipes and food-related questions.
 */
@Singleton
class RecipeMaestroAgent @Inject constructor(
    private val config: RecipeMaestroConfig,
    private val geminiKeyProvider: GeminiKeyProvider,
    dispatcherProvider: DispatcherProvider,
) {
    val applicationScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default)

    private val logger = logging("RecipeMaestroAgent")

    private val userIntent = MutableSharedFlow<PromptIntent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun sendResponsePrompt(prompt: PromptIntent) {
        userIntent.tryEmit(prompt)
    }

    val initialMessage =
        ChatMessage(text = "Thinking", type = ChatMessageType.Loading)

    private val messages = mutableListOf(initialMessage)
    private val _navigationFlow = MutableSharedFlow<RecipeRoute>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val navigationFlow = _navigationFlow.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val agentFlow: StateFlow<AgentState> =
        runAgent(config.initialAgentPrompt)
            .flowOn(dispatcherProvider.default)
            .stateIn(
                scope = applicationScope,
                initialValue = AgentState.Loading(messages.toList()),
                started = SharingStarted.Eagerly
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun runAgent(prompt: String): Flow<AgentState> = channelFlow {
        val strategy = config
            .agentStrategy(
                name = "MasterChefAgent",
                onAssistantMessage = { message ->
                    send(agentMessageToState(message))

                    val userPrompt = userIntent.first()
                    send(userMessageToState(userPrompt.displayPrompt))
                    userPrompt.prompt
                }
            )

        createAgent(strategy) {
            handleEvents {
                onAgentExecutionFailed { ctx ->
                    logger.error(ctx.throwable) { "Error running agent" }
                    // Ensure error emission also respects the collector's context
                    send(errorMessageAsState(ctx.throwable, "Whoops!!!"))
                }
            }
        }.run(prompt)
    }.onEach {
        logger.info { "Agent state: $it" }
    }

    private fun userMessageToState(message: String): AgentState {
        messages.add(ChatMessage(text = message, type = ChatMessageType.User))
        messages.add(ChatMessage(text = "Thinking", type = ChatMessageType.Loading))

        // create new list to fool compose
        return AgentState.Chatting(messages.toList())
    }

    private fun agentMessageToState(message: String): AgentState {
        addOrReplaceMessage(ChatMessage(text = message, type = ChatMessageType.Agent))
        return AgentState.Chatting(messages.toList())
    }

    private fun errorMessageAsState(exception: Throwable, message: String): AgentState {
        addOrReplaceMessage(ChatMessage(text = message, type = ChatMessageType.Error))
        return AgentState.Error(exception, messages.toList())
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
        )
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