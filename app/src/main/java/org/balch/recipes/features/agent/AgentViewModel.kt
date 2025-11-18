package org.balch.recipes.features.agent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diamondedge.logging.logging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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

    val navigationFlow = agent.navigationFlow

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<ChatMessage>> =
        agent.agentFlow
            .map { it.messages }
            .flowOn(dispatcherProvider.default)
            .stateIn(viewModelScope, SharingStarted.Lazily, listOf(agent.initialMessage))

    /**
     * Send a message to the AI agent and add both user message and AI response to chat
     */
    fun sendMessage(message: String) {
        if (message.isBlank()) return
        agent.sendResponseMessage(message)
    }
}
