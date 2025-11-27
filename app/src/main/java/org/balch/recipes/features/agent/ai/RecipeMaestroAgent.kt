package org.balch.recipes.features.agent.ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.GraphAIAgent.FeatureContext
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import androidx.annotation.VisibleForTesting
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import org.balch.recipes.core.ai.GeminiKeyProvider
import org.balch.recipes.core.coroutines.DispatcherProvider
import org.balch.recipes.features.agent.ChatMessage
import org.balch.recipes.features.agent.ChatMessageType
import kotlin.random.Random
import kotlin.time.ExperimentalTime

data class PromptIntent(
    val prompt: String,
    val displayText: String = prompt,
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
    private val applicationScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default)

    private val logger = logging("RecipeMaestroAgent")

    private val userIntent = MutableSharedFlow<PromptIntent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val mood by lazy {
        val chance = Random.nextInt(1, 101)
        deriveRandomPromptData(chance)
            .also {
                logger.info { "Random Mood: chance=${chance}%\n$it" }
            }
    }

    fun sendResponsePrompt(prompt: PromptIntent) {
        userIntent.tryEmit(prompt)
    }

    val initialMessage =
        ChatMessage(text = "Thinking", type = ChatMessageType.Loading)

    private val messages = mutableListOf(initialMessage)

    @OptIn(ExperimentalCoroutinesApi::class)
    val agentFlow: StateFlow<AgentState> =
        runAgent(randomInitialPrompt())
            .flowOn(dispatcherProvider.default)
            .stateIn(
                scope = applicationScope,
                initialValue = AgentState.Loading(messages.toList()),
                started =
                    SharingStarted.Eagerly.takeIf { config.promptAgentAtAppLaunch }
                    ?: SharingStarted.Lazily
            )

    private fun randomInitialPrompt(): String =
        if (mood.isReplacement) mood.prompt
            else config.initialAgentPrompt(mood.prompt)

    @VisibleForTesting
    fun deriveRandomPromptData(chance: Int): RecipeMaestroConfig.RandomAgentPromptData {
        var acc = 0
        return config.initialAgentPrompts
            .firstOrNull {
                acc += it.chance
                (chance <= acc)
            } ?: config.initialAgentPrompts.random()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun runAgent(prompt: String): Flow<AgentState> = channelFlow {
        val strategy = config
            .agentStrategy(
                name = "RecipeMaestroAgent",
                onAssistantMessage = { message ->
                    send(agentMessageToState(message))

                    val userPrompt = userIntent.first()
                    send(userMessageToState(userPrompt.displayText))
                    userPrompt.prompt
                }
            )

        createAgent(strategy) {
            handleEvents {
                onAgentStarting {  ctx ->
                    logger.d { "Agent:${ctx.agent.id} Prompt:${ctx.context.config.prompt}" }
                }
                onAgentCompleted { ctx ->
                    logger.d { "Agent:${ctx.agentId} Completed:${ctx.result}" }
                }
                onAgentExecutionFailed { ctx ->
                    logger.error(ctx.throwable) { "Error running agent" }
                    // Ensure error emission also respects the collector's context
                    send(errorMessageAsState(ctx.throwable, "Whoops!!!"))
                }
                onToolCallFailed { ctx ->
                    logger.error(ctx.throwable) { "Error running tool: ${ctx.tool.name}" }
                    send(errorMessageAsState(ctx.throwable, "Whoops!!!"))
                }
                onToolValidationFailed { ctx ->
                    logger.error { "Error running tool: ${ctx.tool.name}\nError ${ctx.error}" }
                    send(agentMessageToState("Whoops: ${ctx.error}"))
                }
                onToolCallStarting { ctx ->
                    logger.d { "Starting(${ctx.tool.name}) tool: ${ctx.toolArgs}" }
                }
                onToolCallCompleted { ctx ->
                    logger.d { "Completed(${ctx.tool.name}) tool: ${ctx.result}" }
                }
            }
        }.run(prompt)
    }.onEach {
        logger.info { "Agent state: $it" }
    }.catch { throwable ->
        logger.error(throwable) { "An unhandled exception occurred in the agent flow" }
        emit(
            errorMessageAsState(throwable,
            throwable.message
                ?: "Something went wrong. Please try again.")
        )
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