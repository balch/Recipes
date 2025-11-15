package org.balch.recipes.features.agent

import ai.koog.agents.core.tools.ToolRegistry
import org.balch.recipes.core.ai.Agent
import org.balch.recipes.core.ai.AgentModel

/**
 * Fake agent for testing that returns predictable responses
 */
class FakeMasterChefAgent(
    private val response: String = "Test response from chef",
    private val delayMs: Long = 100
) : Agent() {
    override val systemInstruction: String = "Test chef"
    override val agentModel: AgentModel = AgentModel.GEMINI_FLASH
    override val toolRegistry: ToolRegistry = ToolRegistry { }
}
