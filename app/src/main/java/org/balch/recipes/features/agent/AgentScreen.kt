package org.balch.recipes.features.agent

import android.view.HapticFeedbackConstants
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import org.balch.recipes.ui.theme.ThemePreview
import org.balch.recipes.ui.widgets.OverscrollRevealBox
import org.balch.recipes.ui.widgets.RecipeMaestroWidget

@Composable
fun AgentScreen(
    modifier: Modifier = Modifier,
    viewModel: AgentViewModel,
) {
    val messages by viewModel.messages.collectAsState()
    val tokenUsage by viewModel.sessionUsage.collectAsState()
    val view = LocalView.current

    AgentLayout(
        modifier = modifier,
        messages = messages,
        sessionUsage = tokenUsage,
        onSendMessage = { message ->
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            viewModel.sendPrompt(message)
        },
        moodTintColor = viewModel.moodTintColor,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgentLayout(
    messages: List<ChatMessage>,
    sessionUsage: SessionUsage,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    moodTintColor: Color? = null,
) {
    // Start scrolled to the last message (+1 for tokenUsage item at index 0)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (messages.size).coerceAtLeast(0)
    )
    val hazeState = rememberHazeState()

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val isCompactHeight = remember(containerSize, density) {
        with(density) { containerSize.height.toDp() < 400.dp }
    }
    val isLoading = remember(messages) {
        messages.lastOrNull()?.type == ChatMessageType.Loading
    }

    val showCondensedTokenUsage by remember {
        derivedStateOf {
            !listState.layoutInfo.visibleItemsInfo.any { it.key == "tokenUsage" }
        }
    }

    // Auto-scroll to bottom when new messages arrive or loading state changes
    LaunchedEffect(isLoading, messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
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
        // Track reveal fraction for bottom parallax effect (opposite of top)
        var bottomRevealFraction by remember { mutableStateOf(0f) }

        // Animate the bottom reveal for smoother transitions
        val animatedBottomReveal by animateFloatAsState(
            targetValue = bottomRevealFraction,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "bottomParallax"
        )

        // OverscrollRevealBox wraps everything to intercept scroll events
        OverscrollRevealBox(
            modifier = modifier
                .fillMaxSize()
                .imePadding(),
            enabled = showCondensedTokenUsage,
            revealFraction = { fraction -> bottomRevealFraction = fraction },
            revealContent = {
                // Match the top TelemetryWidget's styling with opposite parallax
                TelemetryWidget(
                    sessionUsage = sessionUsage,
                    isLoading = isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            alpha = animatedBottomReveal
                            // Opposite scale effect: grow from 85% to 100% as it appears
                            scaleX = 0.85f + (0.15f * animatedBottomReveal)
                            scaleY = 0.85f + (0.15f * animatedBottomReveal)
                            // Opposite translation: move upward from +30 to 0 as it appears
                            translationY = (1f - animatedBottomReveal) * 30f
                        }
                )
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Messages
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = innerPadding,
                ) {
                    item(key = "tokenUsage") {
                        TelemetryParallaxEffect(
                            listState = listState,
                            sessionUsage = sessionUsage,
                            isLoading = isLoading,
                        )
                    }
                    items(messages, key = { it.id }) { message ->
                        val shouldAnimate = remember(message.id, messages.size) {
                            message.type == ChatMessageType.Agent && messages.size == 1
                        }
                        ChatMessageBubble(
                            message = message,
                            animateTypewriter = shouldAnimate,
                        )
                    }
                    item("bottomSpacer") {
                        Spacer(modifier = Modifier.height(55.dp))
                    }
                }

                // Input area
                ChatInputField(
                    isEnabled = !isLoading,
                    onSendMessage = onSendMessage,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    hazeState = hazeState,
                )
            }
        }
    }
}

@Composable
private fun TelemetryParallaxEffect(
    listState: LazyListState,
    sessionUsage: SessionUsage,
    isLoading: Boolean,
) {
    
    // Derive visibility fraction from scroll state
    val visibilityFraction by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) {
                val visibleItems = listState.layoutInfo.visibleItemsInfo
                if (visibleItems.isEmpty()) return@derivedStateOf 1f
                
                val itemInfo = visibleItems[0]
                val scrollOffset = listState.firstVisibleItemScrollOffset.toFloat()
                val itemHeight = itemInfo.size.toFloat()
                
                // As user scrolls down, scrollOffset increases from 0 to itemHeight
                // We want visibility to decrease as we scroll down
                if (itemHeight > 0) {
                    // Start fading when 20% scrolled, fully gone at 80%
                    val fadeStartThreshold = itemHeight * 0.2f
                    val fadeEndThreshold = itemHeight * 0.8f
                    
                    when {
                        scrollOffset <= fadeStartThreshold -> 1f
                        scrollOffset >= fadeEndThreshold -> 0f
                        else -> {
                            // Linear interpolation between thresholds
                            val fadeRange = fadeEndThreshold - fadeStartThreshold
                            val fadeProgress = scrollOffset - fadeStartThreshold
                            1f - (fadeProgress / fadeRange)
                        }
                    }
                } else 1f
            } else {
                // First item is completely scrolled off screen
                0f
            }
        }
    }


    // Animate visibility for smoother transitions
    val animatedVisibility by animateFloatAsState(
        targetValue = visibilityFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "topParallax"
    )

    TelemetryWidget(
        sessionUsage = sessionUsage,
        isLoading = isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer {
                alpha = animatedVisibility
                // Scale effect: shrink from 100% to 85%
                scaleX = 0.85f + (0.15f * animatedVisibility)
                scaleY = 0.85f + (0.15f * animatedVisibility)
                // Move upward as it fades
                translationY = (1f - animatedVisibility) * -30f
            }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TopBar(
    isCompactHeight: Boolean,
    showCondensedTokenUsage: Boolean,
    sessionUsage: SessionUsage,
    modifier: Modifier = Modifier,
    moodTintColor: Color?,
) {
    TopAppBar(
        modifier = modifier.fillMaxWidth(),
        title = {
            Crossfade(isCompactHeight) { isCompact ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    RecipeMaestroWidget(
                        fontSize = if (isCompact) 18.sp else 24.sp,
                        iconTint = moodTintColor,
                    )

                    Column {
                        Text(
                            text = "Recipe Maestro",
                            style = if (isCompact) typography.titleMedium else typography.titleLarge,
                            fontWeight = if (isCompact) FontWeight.SemiBold else FontWeight.Bold
                        )
                        Crossfade(targetState = showCondensedTokenUsage, label = "subtitle") { showCondensed ->
                            if (showCondensed) {
                                Text(
                                    text = "In: ${sessionUsage.inputTokens} | Out: ${sessionUsage.outputTokens} | Tools: ${sessionUsage.toolCalls}",
                                    style = typography.labelMedium,
                                    color = colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            } else {
                                if (!isCompact) {
                                    Text(
                                        text = "Your culinary—coding companion",
                                        style = typography.labelMedium,
                                        color = colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = colorScheme.onSurface,
        )
    )
}

@ThemePreview
@Composable
private fun AgentScreenInitialPreview(
    @PreviewParameter(ChatMessagesPreviewProvider::class) messages: List<ChatMessage>,
) {
    RecipesTheme {
        AgentLayout(
            messages = messages,
            sessionUsage = SessionUsage(inputTokens = 150, outputTokens = 450),
            onSendMessage = {},
        )
    }
}

@ThemePreview
@Composable
private fun AgentScreenManyMessagesPreview() {
    // Many messages to demonstrate scrolled state where top widget may be partially visible
    val manyMessages = (1..20).map { index ->
        ChatMessage(
            id = "msg_$index",
            text = "Message $index: This is a sample message for testing scroll behavior and parallax effects.",
            type = if (index % 2 == 0) ChatMessageType.User else ChatMessageType.Agent,
            timestamp = System.currentTimeMillis() + index
        )
    }
    RecipesTheme {
        AgentLayout(
            messages = manyMessages,
            sessionUsage = SessionUsage(
                inputTokens = 2500,
                outputTokens = 7500,
                toolCalls = 15
            ),
            onSendMessage = {},
        )
    }
}
