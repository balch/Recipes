package org.balch.recipes.core.ai

import ai.koog.agents.core.agent.AIAgent

/**
 * Interface for agent factory
 */
interface AgentProvider<Input, Output> {
    /**
     * Title for the agent demo screen
     */
    val title: String

    /**
     * Description of the agent
     */
    val description: String

    fun provideAgent(): AIAgent<Input, Output>
}