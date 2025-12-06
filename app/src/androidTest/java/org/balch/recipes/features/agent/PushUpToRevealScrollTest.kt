package org.balch.recipes.features.agent

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.balch.recipes.features.agent.chat.ChatMessage
import org.balch.recipes.features.agent.chat.ChatMessageBubble
import org.balch.recipes.features.agent.chat.ChatMessageType
import org.balch.recipes.features.agent.session.SessionUsage
import org.balch.recipes.features.agent.session.TelemetryWidget
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.widgets.PushUpToRevealBox
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for PushUpToReveal and parallax scroll behavior.
 * Tests both the bottom reveal widget and top TelemetryWidget parallax.
 */
@RunWith(AndroidJUnit4::class)
class PushUpToRevealScrollTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Generate a list of chat messages for testing scroll behavior
     */
    private fun generateTestMessages(count: Int): List<ChatMessage> {
        return (1..count).map { index ->
            ChatMessage(
                id = "msg_$index",
                text = "This is test message #$index with some content to make it reasonably sized.",
                type = if (index % 2 == 0) ChatMessageType.User else ChatMessageType.Agent,
                timestamp = System.currentTimeMillis() + index
            )
        }
    }

    /**
     * Test that the push-up-to-reveal widget shows when swiping up from the bottom
     */
    @Test
    fun pushUpToReveal_showsOnSwipeUp() {
        composeTestRule.setContent {
            RecipesTheme(darkTheme = true) {
                TestPushUpLayout(
                    messages = generateTestMessages(20)
                )
            }
        }

        composeTestRule.waitForIdle()

        // Verify initial state - widget should be hidden
        composeTestRule.onNodeWithTag("revealContent").assertDoesNotExist()

        // Perform swipe up on the layout
        composeTestRule.onNodeWithTag("pushUpContainer")
            .performTouchInput {
                swipeUp(
                    startY = bottom - 50f,
                    endY = bottom - 200f,
                    durationMillis = 300
                )
            }

        composeTestRule.waitForIdle()

        // Verify revealed state
        composeTestRule.onNodeWithTag("revealContent").assertIsDisplayed()
    }

    /**
     * Test that the push-up-to-reveal widget auto-collapses after delay
     */
    @Test
    fun pushUpToReveal_autoCollapsesAfterDelay() {
        composeTestRule.setContent {
            RecipesTheme(darkTheme = true) {
                TestPushUpLayout(
                    messages = generateTestMessages(10),
                    autoCollapseDelayMs = 1000L // Short delay for testing
                )
            }
        }

        composeTestRule.waitForIdle()

        // Swipe to reveal
        composeTestRule.onNodeWithTag("pushUpContainer")
            .performTouchInput {
                swipeUp(
                    startY = bottom - 50f,
                    endY = bottom - 200f,
                    durationMillis = 300
                )
            }

        composeTestRule.waitForIdle()
        // Verify revealed
        composeTestRule.onNodeWithTag("revealContent").assertIsDisplayed()

        // Wait for auto-collapse (using waitUntil for robustness)
        // We wait up to 3000ms for the 1000ms delay + animation
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("revealContent").fetchSemanticsNodes().isEmpty()
        }

        // Verify hidden (should definitely be gone now)
        composeTestRule.onNodeWithTag("revealContent").assertDoesNotExist()
    }

    /**
     * Test the top TelemetryWidget parallax effect when scrolling
     */
    @Test
    fun topTelemetryWidget_parallaxOnScroll() {
        var telemetryAlpha = 1f
        var telemetryTranslationY = 0f

        composeTestRule.setContent {
            RecipesTheme(darkTheme = true) {
                TestParallaxLayout(
                    messages = generateTestMessages(5), // Only 5 messages to quickly reach top
                    onTelemetryParallaxChanged = { alpha, translationY ->
                        telemetryAlpha = alpha
                        telemetryTranslationY = translationY
                        println("PARALLAX_TEST: alpha=$alpha, translationY=$translationY")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Initially TelemetryWidget should be fully visible (alpha = 1)
        println("PARALLAX_TEST: Initial alpha=$telemetryAlpha")

        // For reverseLayout=true:
        // - swipeUp scrolls towards older messages (towards TelemetryWidget at top)
        // - First scroll UP to bring TelemetryWidget into view
        // - Continue scrolling to push it partially off the top (offset < 0)
        repeat(10) { // More swipes to reach the TelemetryWidget
            composeTestRule.onNodeWithTag("parallaxList")
                .performTouchInput {
                    swipeUp(
                        startY = bottom - 100f,
                        endY = top + 100f,
                        durationMillis = 200
                    )
                }
            composeTestRule.waitForIdle()
            Thread.sleep(50)
            println("PARALLAX_TEST: After swipe ${it + 1}, alpha=$telemetryAlpha, translationY=$telemetryTranslationY")
        }

        println("PARALLAX_TEST: Final alpha=$telemetryAlpha, translationY=$telemetryTranslationY")
    }

    /**
     * Test scrolling down and up in the reversed layout
     */
    @Test
    fun reversedLayout_scrollBehavior() {
        composeTestRule.setContent {
            RecipesTheme(darkTheme = true) {
                val listState = rememberLazyListState()

                TestReversedScrollLayout(
                    messages = generateTestMessages(20)
                )
            }
        }

        composeTestRule.waitForIdle()

        // Message 1 should be at the bottom in reverse layout
        // Use onAllNodes to handle potential duplicates and check if at least one is displayed
        composeTestRule.onAllNodes(hasText("test message #1", substring = true))
            .onFirst()
            .assertIsDisplayed()

        // Scroll up (in reverse layout, this goes towards older messages)
        composeTestRule.onNodeWithTag("reversedList")
            .performTouchInput {
                swipeUp(
                    startY = bottom - 100f,
                    endY = top + 100f,
                    durationMillis = 500
                )
            }

        composeTestRule.waitForIdle()

        // After scrolling, we should see older messages
        // TelemetryWidget should start to show
    }
}

    /**
     * Test layout with PushUpToRevealBox wrapping a LazyColumn
     */
    @SuppressLint("FrequentlyChangedStateInComposition")
    @Composable
    private fun TestPushUpLayout(
        messages: List<ChatMessage>,
        autoCollapseDelayMs: Long = 3000L
    ) {
        val listState = rememberLazyListState()

        PushUpToRevealBox(
            modifier = Modifier
                .fillMaxSize()
                .testTag("pushUpContainer"),
            autoCollapseDelayMs = autoCollapseDelayMs,
            revealContent = {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .testTag("revealContent"),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    colors = cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.95f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Revealed Content",
                            style = typography.titleSmall,
                            modifier = Modifier.testTag("revealFractionText")
                        )
                    }
                }
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorScheme.background),
                state = listState,
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item("bottomSpacer") {
                    Spacer(modifier = Modifier.height(60.dp))
                }

                items(messages.reversed(), key = { it.id }) { message ->
                    ChatMessageBubble(message = message)
                }

                item(key = "tokenUsage") {
                    TelemetryWidget(
                        sessionUsage = SessionUsage(inputTokens = 150, outputTokens = 450, toolCalls = 2),
                        isLoading = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }

/**
 * Test layout for parallax effect on top TelemetryWidget
 */
@Composable
private fun TestParallaxLayout(
    messages: List<ChatMessage>,
    onTelemetryParallaxChanged: (alpha: Float, translationY: Float) -> Unit = { _, _ -> }
) {
    val listState = rememberLazyListState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .testTag("parallaxList"),
        state = listState,
        reverseLayout = true,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item("bottomSpacer") {
            Spacer(modifier = Modifier.height(60.dp))
        }

        items(messages.reversed(), key = { it.id }) { message ->
            ChatMessageBubble(message = message)
        }

        item(key = "tokenUsage") {
            // Calculate parallax based on item visibility
            val itemInfo = listState.layoutInfo.visibleItemsInfo
                .find { it.key == "tokenUsage" }

            val visibilityFraction = itemInfo?.let { info ->
                val offset = info.offset
                val size = info.size
                
                // Log the actual offset
                println("PARALLAX_OFFSET: offset=$offset, size=$size")

                when {
                    offset < 0 -> {
                        val visibleHeight = (size + offset).coerceAtLeast(0)
                        visibleHeight.toFloat() / size
                    }
                    else -> 1f
                }
            } ?: 0f

            val translationY = (1f - visibilityFraction) * 50f
            
            // Report current parallax state
            onTelemetryParallaxChanged(visibilityFraction, translationY)

            TelemetryWidget(
                sessionUsage = SessionUsage(inputTokens = 150, outputTokens = 450, toolCalls = 2),
                isLoading = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .graphicsLayer {
                        alpha = visibilityFraction
                        this.translationY = translationY
                    }
                    .testTag("telemetryWidget")
            )
        }
    }
}

/**
 * Simple reversed layout for scroll testing
 */
@Composable
private fun TestReversedScrollLayout(
    messages: List<ChatMessage>
) {
    val listState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .testTag("reversedList"),
        state = listState,
        reverseLayout = true,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item("bottomSpacer") {
            Spacer(modifier = Modifier.height(60.dp))
        }

        items(messages.reversed(), key = { it.id }) { message ->
            ChatMessageBubble(message = message)
        }

        item(key = "tokenUsage") {
            TelemetryWidget(
                sessionUsage = SessionUsage(inputTokens = 150, outputTokens = 450, toolCalls = 2),
                isLoading = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("topTelemetry")
            )
        }
    }
}
