package org.balch.recipes.features.agent

import android.view.HapticFeedbackConstants
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.coroutines.launch
import org.balch.recipes.core.navigation.isCompact
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
    val listState = rememberLazyListState()
    val isDragged by listState.interactionSource.collectIsDraggedAsState()
    val hazeState = rememberHazeState()

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val isCompactHeight by remember(containerSize, density) {
        derivedStateOf {
            with(density) { containerSize.height.toDp() < 400.dp }
        }
    }
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()

    val isLoading by remember(messages) {
        derivedStateOf {
            messages.lastOrNull()?.type == ChatMessageType.Loading
        }
    }

    val reversedMessages = remember(messages) { messages.reversed() }

    val showCondensedTokenUsage by remember {
        derivedStateOf {
            !listState.layoutInfo.visibleItemsInfo.any { it.key == "tokenUsage" }
        }
    }

    val initialMessageIds = rememberSaveable {
        messages.filter { it.type != ChatMessageType.Loading }.map { it.id }
    }

    // Cancel typewriter when user scrolls up (away from bottom)
    var cancelTypewriter by remember { mutableStateOf(false) }

    // Helper to scroll to bottom (index 0 in reverse layout)
    val coroutineScope = rememberCoroutineScope()
    val scrollToBottom: () -> Unit = remember {
        {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    // Detect when user drags to scroll - cancel animation
    LaunchedEffect(isDragged) {
        if (isDragged) {
            cancelTypewriter = true
        }
    }

    // Auto-scroll to bottom when new messages arrive or loading state changes
    LaunchedEffect(isLoading, messages.size) {
        if (messages.isNotEmpty() && !cancelTypewriter) {
            listState.animateScrollToItem(0)
        }
    }

    // Reset cancel state when a new message arrives
    LaunchedEffect(messages.lastOrNull()?.id) {
        cancelTypewriter = false
    }

    Column(
        modifier = modifier,
    ) {
        if (!windowAdaptiveInfo.isCompact()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surface),
            ) {
                DragHandle(
                    modifier = Modifier.align(Alignment.TopCenter),
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
            // OverscrollRevealBox wraps everything to intercept scroll events
            OverscrollRevealBox(
                modifier = modifier
                    .fillMaxSize()
                    .imePadding(),
                reverseLayout = true,
                enabled = showCondensedTokenUsage,
                revealContent = {
                    TelemetryWidget(
                        sessionUsage = sessionUsage,
                        isLoading = isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(vertical = 8.dp)
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
                        reverseLayout = true,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = innerPadding,
                    ) {
                        item("bottomSpacer") {
                            Spacer(modifier = Modifier.height(55.dp))
                        }
                        items(reversedMessages, key = { it.id }) { message ->
                            val shouldAnimate = message.id == messages.lastOrNull()?.id &&
                                    message.type == ChatMessageType.Agent &&
                                    !initialMessageIds.contains(message.id)
                            ChatMessageBubble(
                                message = message,
                                animateTypewriter = shouldAnimate,
                                cancelTypewriter = cancelTypewriter,
                                onAnimationComplete = {
                                    if (shouldAnimate && !cancelTypewriter) {
                                        scrollToBottom()
                                    }
                                }
                            )
                        }
                        item(key = "tokenUsage") {
                            TelemetryParallaxEffect(
                                listState = listState,
                                innerPadding = innerPadding,
                                sessionUsage = sessionUsage,
                                isLoading = isLoading,
                            )
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
}

@Composable
private fun TelemetryParallaxEffect(
    listState: LazyListState,
    innerPadding: PaddingValues,
    sessionUsage: SessionUsage,
    isLoading: Boolean,
) {
    // For reverseLayout=true, this item is at the visual TOP
    val itemInfo = listState.layoutInfo.visibleItemsInfo
        .find { it.key == "tokenUsage" }

    // Calculate parallax based on position relative to viewport
    // The item should:
    // - Be fully visible when it's comfortably in the viewport
    // - Start fading/shrinking as it approaches any edge
    // - Disappear quickly once mostly off-screen

    val visibilityFraction = if (itemInfo != null) {
        val offset = itemInfo.offset
        val size = itemInfo.size
        val contentPaddingTop =
            innerPadding.calculateTopPadding().value * LocalDensity.current.density

        // Distance from top edge (content area starts after top padding)
        val distanceFromTop = offset - contentPaddingTop

        // Transition zones for parallax effect
        val enterTransitionZone =
            size * 0.8f  // 80% of item height for entering
        val exitTransitionZone =
            size * 0.5f   // Start fading out when 50% of height from top

        when {
            // Item is scrolled above viewport
            offset + size <= contentPaddingTop -> 0f

            // Item is partially above top edge (parallax out - final phase)
            distanceFromTop < 0 -> {
                val visiblePart = size + distanceFromTop
                (visiblePart / size).coerceIn(
                    0f,
                    1f
                ) * 0.5f // Max 50% at this point
            }

            // Item is approaching top edge - early exit zone (start fading)
            distanceFromTop < exitTransitionZone -> {
                // Fade from 1.0 to 0.5 as it approaches the edge
                val progress =
                    distanceFromTop / exitTransitionZone // 1.0 at zone start, 0 at edge
                0.5f + (progress * 0.5f) // 0.5 to 1.0
            }

            // Item is in enter transition zone near top (parallax in)
            distanceFromTop < enterTransitionZone -> {
                (distanceFromTop / enterTransitionZone).coerceIn(0f, 1f)
            }

            // Item is comfortably in viewport
            else -> 1f
        }
    } else {
        // Not visible at all
        0f
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
                        fontSize = if (isCompact) 24.sp else 32.sp,
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
