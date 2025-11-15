package org.balch.recipes.features.agent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diamondedge.logging.logging
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import org.balch.recipes.core.coroutines.DispatcherProvider
import org.balch.recipes.core.models.Meal

@HiltViewModel(assistedFactory = AgentViewModel.Factory::class)
class AgentViewModel @AssistedInject constructor(
    private val agent: MasterChefAgent,
    dispatcherProvider: DispatcherProvider,
    @Assisted private val initialContext: String,
    @Assisted private val meal: Meal?,
) : ViewModel() {

    private val logger = logging("AgentViewModel")

    private val chatMessages = mutableListOf(
        ChatMessage(text = "Hi, how can I help?", type = ChatMessageType.Agent)
    ).apply {
        if (initialContext.isNotEmpty()) {
            add(ChatMessage(text = initialContext, type = ChatMessageType.User))
        }
    }
    
    // AI Chat state
    private val chatMessagesIntent = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val navigationFlow = agent.navigationFlow

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<ChatMessage>> =
        chatMessagesIntent
            .transformLatest { sendMessage(it) }
            .flowOn(dispatcherProvider.default)
            .stateIn(viewModelScope, SharingStarted.Lazily, chatMessages.toList())

    /**
     * Send a message to the AI agent and add both user message and AI response to chat
     */
    fun sendMessage(message: String) {
        if (message.isBlank()) return
        chatMessagesIntent.tryEmit(message)
    }

    private suspend fun FlowCollector<List<ChatMessage>>.sendMessage(message: String) {
        chatMessages.add(ChatMessage(text = message, type = ChatMessageType.User))
        chatMessages.add(ChatMessage(text = "Thinking", type = ChatMessageType.Loading))
        emit(chatMessages.toList())
        val response = agent.chat(message)
        chatMessages[chatMessages.lastIndex] = ChatMessage(text = response, type = ChatMessageType.Agent)
        emit(chatMessages.toList())
    }

    @AssistedFactory
    interface Factory {
        fun create(initialContext: String, meal: Meal?): AgentViewModel
    }
}
