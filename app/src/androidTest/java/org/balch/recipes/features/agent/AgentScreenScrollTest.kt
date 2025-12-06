package org.balch.recipes.features.agent

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.balch.recipes.features.agent.chat.ChatInputField
import org.balch.recipes.features.agent.chat.ChatMessage
import org.balch.recipes.features.agent.chat.ChatMessageBubble
import org.balch.recipes.features.agent.chat.ChatMessageType
import org.balch.recipes.features.agent.session.SessionUsage
import org.balch.recipes.features.agent.session.TelemetryWidget
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.utils.parallaxLayoutModifier
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test to reproduce and debug the scrolling glitch during typewriter animation.
 * The glitch occurs when long agent messages with markdown are displayed.
 */
@RunWith(AndroidJUnit4::class)
class AgentScreenScrollTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Test with markdown that has blank lines which may cause height fluctuations
     */
    @SuppressLint("UnusedContentLambdaTargetStateParameter")
    @OptIn(ExperimentalSharedTransitionApi::class)
    @Test
    fun agentScreen_typewriterScroll_withBlankLines() {
        val messageWithBlankLines = """
            Here's my response.
            
            
            This line comes after two blank lines.
            
            And here's another paragraph.
            
            
            
            After three blank lines!
            
            **Bold text** and *italic text* mixed in.
        """.trimIndent()

        val messages = mutableStateListOf(
            ChatMessage(
                id = "1",
                text = "Test message",
                type = ChatMessageType.User,
                timestamp = System.currentTimeMillis()
            ),
            ChatMessage(
                id = "2",
                text = messageWithBlankLines,
                type = ChatMessageType.Agent,
                timestamp = System.currentTimeMillis() + 1
            )
        )

        composeTestRule.setContent {
            RecipesTheme {
                SharedTransitionLayout {
                    AnimatedContent(targetState = true) { _ ->
                        AgentLayoutForTest(
                            messages = messages,
                            sessionUsage = SessionUsage(),
                            onSendMessage = { },
                            onScrollChanged = { firstVisible, offset ->
                                println("SCROLL_DEBUG_BLANK: firstVisible=$firstVisible, offset=$offset")
                            }
                        )
                    }
                }
            }
        }

        // Let typewriter run
        Thread.sleep(2000)
        composeTestRule.waitForIdle()
    }

    /**
     * Test specifically for the markdown bold transition which may cause height jumps
     */
    @SuppressLint("UnusedContentLambdaTargetStateParameter")
    @OptIn(ExperimentalSharedTransitionApi::class)
    @Test
    fun agentScreen_typewriterScroll_boldTextTransition() {
        // This message has bold markers that will be parsed mid-animation
        val messageWithBold = "Start **this text is bold** end. More **bold here** too."

        val messages = mutableStateListOf(
            ChatMessage(
                id = "1",
                text = "Test",
                type = ChatMessageType.User,
                timestamp = System.currentTimeMillis()
            ),
            ChatMessage(
                id = "2",
                text = messageWithBold,
                type = ChatMessageType.Agent,
                timestamp = System.currentTimeMillis() + 1
            )
        )

        composeTestRule.setContent {
            RecipesTheme {
                SharedTransitionLayout {
                    AnimatedContent(targetState = true) { _ ->
                        AgentLayoutForTest(
                            messages = messages,
                            sessionUsage = SessionUsage(),
                            onSendMessage = { },
                            onScrollChanged = { firstVisible, offset ->
                                println("SCROLL_DEBUG_BOLD: firstVisible=$firstVisible, offset=$offset")
                            }
                        )
                    }
                }
            }
        }

        Thread.sleep(2000)
        composeTestRule.waitForIdle()
    }
}

/**
 * Test-accessible version of AgentLayout that allows monitoring scroll behavior.
 * Used for debugging the typewriter scroll glitch.
 */
@SuppressLint("FrequentlyChangingValue")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AgentLayoutForTest(
    messages: List<ChatMessage>,
    sessionUsage: SessionUsage,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    moodTintColor: Color? = null,
    onScrollChanged: (firstVisibleItemIndex: Int, firstVisibleItemScrollOffset: Int) -> Unit = { _, _ -> },
) {
    val listState = rememberLazyListState()
    val hazeState = rememberHazeState()

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val isCompactHeight by remember(containerSize, density) {
        derivedStateOf {
            with(density) { containerSize.height.toDp() < 400.dp }
        }
    }
    val isLoading by remember(messages) {
        derivedStateOf {
            messages.lastOrNull()?.type == ChatMessageType.Loading
        }
    }

    val showCondensedTokenUsage by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }

    val initialMessageIds = rememberSaveable {
        messages.filter { it.type != ChatMessageType.Loading }.map { it.id }
    }

    // Report scroll changes for debugging
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        onScrollChanged(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
    }

    LaunchedEffect(isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(
                index = messages.size + 1,
            )
        }
    }

    Scaffold(
        modifier = Modifier.onSizeChanged { containerSize = it },
        topBar = {
            TopBar(
                isCompactHeight = isCompactHeight,
                showCondensedTokenUsage = showCondensedTokenUsage,
                sessionUsage = sessionUsage,
                modifier = Modifier
                    .hazeEffect(state = hazeState, style = LocalHazeStyle.current) {
                        HazeProgressive.verticalGradient(
                            startIntensity = 0f,
                            endIntensity = 1f,
                        )
                    },
                moodTintColor = moodTintColor,
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = innerPadding,
            ) {
                item(key = "tokenUsage") {
                    TelemetryWidget(
                        sessionUsage = sessionUsage,
                        isLoading = isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .parallaxLayoutModifier(listState, 4)
                    )
                }
                items(messages, key = { it.id }) { message ->
                    ChatMessageBubble(
                        message = message,
                        animateTypewriter = message == messages.last() &&
                                message.type == ChatMessageType.Agent &&
                                !initialMessageIds.contains(message.id),
                    )
                }
                item("bottomSpacer") {
                    Spacer(modifier = Modifier.height(55.dp))
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {
                ChatInputField(
                    isEnabled = !isLoading,
                    onSendMessage = onSendMessage,
                    modifier = Modifier,
                    hazeState = hazeState,
                )
            }
        }
    }
}

