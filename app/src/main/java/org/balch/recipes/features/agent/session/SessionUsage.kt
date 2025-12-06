package org.balch.recipes.features.agent.session

import kotlinx.serialization.Serializable

/**
 * Represents statistics for a single roundtrip (user message -> agent response).
 * Used for the bar chart visualization in TokenUsageDisplay.
 */
@Serializable
data class AgentSessionStats(
    val inputTokens: Int = 0,
    val outputTokens: Int = 0,
    val toolCalls: Int = 0,
) {
    val totalTokens: Int get() = inputTokens + outputTokens
}

/**
 * Represents token usage statistics for AI agent interactions.
 * Tracks both input (prompt) and output (completion) tokens.
 */
@Serializable
data class SessionUsage(
    val inputTokens: Long = 0,
    val outputTokens: Long = 0,
    val toolCalls: Int = 0,
    val sessionHistory: List<AgentSessionStats> = emptyList(),
) {
    val totalTokens: Long get() = inputTokens + outputTokens

    operator fun plus(other: SessionUsage): SessionUsage = SessionUsage(
        inputTokens = this.inputTokens + other.inputTokens,
        outputTokens = this.outputTokens + other.outputTokens,
        toolCalls = this.toolCalls + other.toolCalls,
        sessionHistory = this.sessionHistory + other.sessionHistory,
    )

    companion object Companion {
        val EMPTY = SessionUsage()
    }
}
