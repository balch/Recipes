package org.balch.recipes.features.agent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diamondedge.logging.logging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import org.balch.recipes.core.coroutines.DispatcherProvider
import org.balch.recipes.features.agent.ai.RecipeMaestroAgent
import javax.inject.Inject

@HiltViewModel
class AgentViewModel @Inject constructor(
    private val agent: RecipeMaestroAgent,
    dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private val logger = logging("AgentViewModel")

    // AI Chat state
    private val chatMessagesIntent = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val navigationFlow = agent.navigationFlow

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<ChatMessage>> =
        flow {
            logger.d { "waiting for first prompt"}

            val fistPrompt = chatMessagesIntent
                .mapNotNull { it.trim().takeIf { it.isNotEmpty() } }
                .first()

            logger.d { "sending first prompt: $fistPrompt" }
            emitAll(agent.runAgent(fistPrompt))
        }
            .onEach {
                logger.d { "AgentState: ${it::class.simpleName} msgCount=${it.messages.size}"}
            }
            .map { it.messages }
            .flowOn(dispatcherProvider.default)
            .stateIn(viewModelScope, SharingStarted.Lazily, listOf(agent.initialMessage))

    /**
     * Send a message to the AI agent and add both user message and AI response to chat
     */
    fun sendMessage(message: String) {
        if (message.isBlank()) return
        chatMessagesIntent.tryEmit(message)
        agent.sendUserMessage(message)
    }
}
