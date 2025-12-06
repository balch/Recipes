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
import org.balch.recipes.features.agent.chat.ChatMessage
import org.balch.recipes.features.agent.session.SessionUsage
import javax.inject.Inject

@HiltViewModel
class AgentViewModel @Inject constructor(
    private val agent: RecipeMaestroAgent,
    dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private val logger = logging("AgentViewModel")

    // Expose the agent's current mood tint color to the UI
    val moodTintColor = agent.mood.tintColor

    // Expose token usage for UI display
    val sessionUsage: StateFlow<SessionUsage> = agent.sessionUsage

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<ChatMessage>> =
        agent.agentFlow
            .map { it.messages }
            .flowOn(dispatcherProvider.default)
            .stateIn(viewModelScope, SharingStarted.Lazily, agent.agentFlow.value.messages)

    /**
     * Send a prompt to the AI agent and add both user message and AI response to chat
     */
    fun sendPrompt(intent: PromptIntent) {
        if (intent.prompt.isBlank()) return
        agent.sendResponsePrompt(intent)
    }

    /**
     * Send a prompt to the AI agent and add both user message and AI response to chat
     */
    fun sendPrompt(prompt: String) {
        sendPrompt(PromptIntent(prompt))
    }
}
