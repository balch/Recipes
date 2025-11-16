package org.balch.recipes.features.agent

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.balch.recipes.core.ai.GeminiKeyProvider
import org.balch.recipes.core.coroutines.DefaultDispatcherProvider
import org.balch.recipes.features.agent.ai.RecipeMaestroAgent
import org.balch.recipes.features.agent.ai.RecipeMaestroConfig
import org.balch.recipes.ui.theme.RecipesTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AgentScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()


    @Inject
    lateinit var config: RecipeMaestroConfig

    @Inject
    lateinit var keyProvider: GeminiKeyProvider

    private lateinit var testViewModel: AgentViewModel

    @Before
    fun setup() {
        hiltRule.inject()
        
        // Create a test ViewModel with mocked dependencies
        val testAgent = RecipeMaestroAgent(
            config = config,
            geminiKeyProvider = keyProvider,
        )
        testViewModel = AgentViewModel(
            initialContext = "test context",
            meal = null,
            agent = testAgent,
            dispatcherProvider = DefaultDispatcherProvider(),
        )
    }

    @SuppressLint("UnusedContentLambdaTargetStateParameter")
    @OptIn(ExperimentalSharedTransitionApi::class)
    @Test
    fun agentScreen_hasInputField() {
        // Launch the Agent screen
        composeTestRule.setContent {
            RecipesTheme {
                SharedTransitionLayout {
                    AnimatedContent(targetState = true) {
                        AgentScreen(
                            viewModel = testViewModel,
                            onBack =  { },
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@AnimatedContent,
                        )
                    }
                }
            }
        }

        // Verify the input field placeholder is displayed
        composeTestRule.onNodeWithText("Ask me about recipes...").assertIsDisplayed()
    }

    @SuppressLint("UnusedContentLambdaTargetStateParameter")
    @OptIn(ExperimentalSharedTransitionApi::class)
    @Test
    fun agentScreen_showsInitialMessage() {
        // Launch the Agent screen
        composeTestRule.setContent {
            RecipesTheme {
                SharedTransitionLayout {
                    AnimatedContent(targetState = true) {
                        AgentScreen(
                            viewModel = testViewModel,
                            onBack =  { },
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@AnimatedContent,
                        )
                    }
                }
            }
        }

        // Verify the initial AI message is displayed
        composeTestRule.onNodeWithText("Hi, how can I help?").assertIsDisplayed()
    }

    @SuppressLint("UnusedContentLambdaTargetStateParameter")
    @OptIn(ExperimentalSharedTransitionApi::class)
    @Test
    fun agentScreen_showsNewMessagesWhenEmitted() {
        // Launch the Agent screen
        composeTestRule.setContent {
            RecipesTheme {
                SharedTransitionLayout {
                    AnimatedContent(targetState = true) {
                        AgentScreen(
                            viewModel = testViewModel,
                            onBack =  { },
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@AnimatedContent,
                        )
                    }
                }
            }
        }

        // Verify initial state
        composeTestRule.onNodeWithText("Hi, how can I help?").assertIsDisplayed()
        
        // Type a message in the input field
        val testMessage = "What is a good pasta recipe?"
        
        println("[DEBUG_LOG] Looking for text field...")
        
        // Find the text field and input text
        composeTestRule.onNode(hasText("Ask me about recipes..."))
            .performTextInput(testMessage)
        
        println("[DEBUG_LOG] Text input performed: $testMessage")
        
        // Wait a moment for the button to be enabled
        composeTestRule.waitForIdle()

        println("[DEBUG_LOG] Clicking send button...")
        
        // Click the send button
        composeTestRule.onNodeWithContentDescription("Send message")
            .performClick()
        
        println("[DEBUG_LOG] Send button clicked")
        
        // Wait for idle after clicking
        composeTestRule.waitForIdle()

        println("[DEBUG_LOG] Waiting for user message to appear...")

        // Verify the user message appears in the chat
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            val nodes = composeTestRule.onAllNodesWithText(testMessage, substring = true)
                .fetchSemanticsNodes()
            println("[DEBUG_LOG] Found ${nodes.size} nodes with text: $testMessage")
            nodes.isNotEmpty()
        }

        println("[DEBUG_LOG] User message found, asserting it's displayed")
        
        // Verify the user message is displayed
        composeTestRule.onNodeWithText(testMessage).assertIsDisplayed()

        println("[DEBUG_LOG] Test passed - message is displayed!")
    }
}
