package org.balch.recipes.features.agent.ai

import com.google.common.truth.Truth.assertThat
import org.balch.recipes.core.ai.GeminiKeyProvider
import org.balch.recipes.core.coroutines.TestDispatcherProvider
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class RecipeMaestroAgentTest {

    private val config = RecipeMaestroConfig(
        mock(),
        mock(),
    )

    private val dispatcherProvider = TestDispatcherProvider()

    private val agent = RecipeMaestroAgent(
        config = config,
        geminiKeyProvider = GeminiKeyProvider(),
        dispatcherProvider = dispatcherProvider,
    )

    @Test
    fun deriveRandomPromptData() {
        assertThat(agent.deriveRandomPromptData(1))
            .isEqualTo(config.initialAgentPrompts.first())

        assertThat(agent.deriveRandomPromptData(100))
            .isEqualTo(config.initialAgentPrompts.last())
    }
}