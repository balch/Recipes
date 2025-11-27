package org.balch.recipes.features.agent

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.model.DefaultMarkdownColors
import com.mikepenz.markdown.model.DefaultMarkdownTypography
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.theme.ThemePreview
import org.balch.recipes.ui.utils.sharedBounds
import org.balch.recipes.ui.widgets.RecipeMaestroWidget

@Composable
fun AgentScreen(
    modifier: Modifier = Modifier,
    viewModel: AgentViewModel,
    onBack: () -> Unit,
) {
    val messages by viewModel.messages.collectAsState()
    val view = LocalView.current

    AgentLayout(
        modifier = modifier,
        messages = messages,
        onSendMessage = { message ->
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            viewModel.sendPrompt(message)
        },
        onBack = onBack,
        moodTintColor = viewModel.moodTintColor,
    )
}

@Composable
private fun AgentLayout(
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    moodTintColor: Color? = null,
) {
    val listState = rememberLazyListState()
    val hazeState = rememberHazeState()

    val isLoading = messages.lastOrNull()?.type == ChatMessageType.Loading

    LaunchedEffect(isLoading) {
        if (messages.isNotEmpty()) {
            // Always bring the latest message into view
            listState.animateScrollToItem(
                index = messages.lastIndex,
                scrollOffset = -1
            )
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                modifier = Modifier
                    .hazeEffect(state = hazeState, style = LocalHazeStyle.current) {
                        HazeProgressive.verticalGradient(
                            startIntensity = 0f,
                            endIntensity = 1f,
                        )
                    },
                onBack = onBack,
                iconTint = moodTintColor,
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            // Messages
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .hazeSource(hazeState),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = innerPadding,
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatMessageBubble(message = message)
                }
                item {
                    Spacer(modifier = Modifier.height(55.dp))
                }
            }

            // Input area
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    iconTint: Color? = null,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        TopAppBar(
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RecipeMaestroWidget(
                        fontSize = 32.sp,
                        iconTint = iconTint,
                    )

                    Column {
                        Text(
                            text = "Recipe Maestro",
                            style = typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Your culinary—coding companion",
                            style = typography.bodyMedium,
                            color = colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = colorScheme.onSurface,
            )
        )
    }
}

@Composable
private fun ChatMessageBubble(message: ChatMessage) {
    val alignment = if (message.type == ChatMessageType.User) {
        Alignment.CenterEnd
    } else {
        Alignment.CenterStart
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (message.type == ChatMessageType.User) 20.dp else 4.dp,
                bottomEnd = if (message.type == ChatMessageType.User) 4.dp else 20.dp
            ),
            color = message.type.containerColor(),
            shadowElevation = 2.dp
        ) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                when (message.type) {
                    ChatMessageType.Loading -> ChefThinkingAnimation()
                    else -> {
                        Column {
                            if (message.type != ChatMessageType.User) {
                                Text(
                                    text = when (message.type) {
                                        ChatMessageType.Agent -> "👨‍🍳 Maestro"
                                        ChatMessageType.Error -> "⚠️ Error"
                                        else -> ""
                                    },
                                    style = typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = message.type.textColor().copy(alpha = 0.8f),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            Markdown(
                                content = message.text,
                                modifier = Modifier.fillMaxWidth(),
                                colors = message.markdownColors(),
                                typography = message.markdownTypography()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChefThinkingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking")
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "👨‍🍳",
            fontSize = 24.sp
        )
        
        // Pulsing dots
        repeat(3) { index ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 600,
                        delayMillis = index * 150,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot$index"
            )
            
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .scale(scale)
                    .background(
                        colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class,
)
@Composable
private fun ChatInputField(
    isEnabled: Boolean,
    onSendMessage: (String) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    onMeasuredHeight: (Int) -> Unit = {},
) {
    var message by remember { mutableStateOf("") }
    val sendMessage = {
        val trimmed = message.trim()
        if (trimmed.isNotEmpty()) {
            onSendMessage(trimmed)
            message = ""
        }
    }

    Row(
        modifier = modifier
            .hazeEffect(state = hazeState, style = LocalHazeStyle.current)
            .fillMaxWidth()
            .padding(16.dp)
            // Respect the IME (keyboard) and system navigation bars
            .imePadding(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .sharedBounds("RecipeMaestroText")
                .onSizeChanged { onMeasuredHeight(it.height) }
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown &&
                        (keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter) &&
                        !keyEvent.isShiftPressed
                    ) {
                        sendMessage()
                        true // Consume the event
                    } else {
                        false // Do not consume the event
                    }
                },
            placeholder = {
                if (isEnabled) {
                    Text(
                        "Ask me about recipes...",
                        style = typography.bodyLarge
                    )
                } else {
                    Text(
                        "Pinging LLM Friend...",
                        style = typography.bodyLarge
                    )
                }
            },
            leadingIcon = {
                IconButton(
                    enabled = isEnabled,
                    onClick = sendMessage,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send message",
                    )
                }
            },
            textStyle = typography.bodyLarge.copy(fontSize = 18.sp),
            shape = RoundedCornerShape(24.dp),
            minLines = 1,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { sendMessage() })
        )
    }
}

@Composable
private fun ChatMessage.markdownColors() = DefaultMarkdownColors(
    text = type.textColor(),
    codeBackground = type.containerColor(),
    inlineCodeBackground = type.containerColor(),
    dividerColor = type.textColor().copy(alpha = 0.2f),
    tableBackground = type.containerColor(),
)

@Composable
private fun ChatMessage.markdownTypography() = DefaultMarkdownTypography(
    h1 = typography.headlineLarge.copy(fontSize = 24.sp),
    h2 = typography.headlineMedium.copy(fontSize = 20.sp),
    h3 = typography.headlineSmall.copy(fontSize = 18.sp),
    h4 = typography.titleLarge.copy(fontSize = 16.sp),
    h5 = typography.titleMedium.copy(fontSize = 14.sp),
    h6 = typography.titleSmall.copy(fontSize = 12.sp),
    text = typography.bodyLarge.copy(fontSize = 16.sp),
    code = typography.bodyMedium.copy(
        fontSize = 14.sp,
        background = type.containerColor(),
    ),
    inlineCode = typography.bodyMedium,
    paragraph = typography.bodyMedium.copy(fontSize = 16.sp),
    ordered = typography.bodyMedium.copy(fontSize = 16.sp),
    bullet = typography.bodyMedium.copy(fontSize = 16.sp),
    list = typography.bodyMedium.copy(fontSize = 16.sp),
    quote = typography.bodyMedium.copy(fontSize = 16.sp),
    table = typography.bodyMedium.copy(fontSize = 16.sp),
    textLink = TextLinkStyles()
)


// ============================================
// Previews
// ============================================

@ThemePreview
@Composable
private fun AgentScreenInitialPreview(
    @PreviewParameter(ChatMessagesPreviewProvider::class) messages: List<ChatMessage>,
) {
    RecipesTheme {
        AgentLayout(
            messages = messages,
            onSendMessage = {},
            onBack = {},
        )
    }
}

@ThemePreview
@Composable
private fun ChatMessageBubbleUserPreview() {
    RecipesTheme {
        Box(
            modifier = Modifier.padding(16.dp)
                .height(128.dp)
        ) {
            ChatMessageBubble(
                message = ChatMessage(
                    text = "What's a good recipe for pasta carbonara?",
                    type = ChatMessageType.User,
                    timestamp = 1
                )
            )
        }
    }
}

@ThemePreview
@Composable
private fun ChatMessageBubbleAgentPreview() {
    RecipesTheme {
        Box(
            modifier = Modifier.padding(16.dp)
                .height(128.dp)
        ) {
            ChatMessageBubble(
                message = ChatMessage(
                    text = "I'd recommend Spaghetti Carbonara! It's a classic Italian dish with eggs, cheese, pancetta, and black pepper. The key is to use the pasta water to create a creamy sauce without cream!",
                    type = ChatMessageType.Agent,
                    timestamp = 1
                )
            )
        }
    }
}

@ThemePreview
@Composable
private fun ChatMessageBubbleLoadingPreview() {
    RecipesTheme {
        ChatMessageBubble(
            message = ChatMessage(
                text = "Thinking",
                type = ChatMessageType.Loading,
                timestamp = 1
            )
        )
    }
}

@ThemePreview
@Composable
private fun ChatMessageBubbleErrorPreview() {
    RecipesTheme {
        Box(
            modifier = Modifier.padding(16.dp)
                .height(128.dp)
        ) {
            ChatMessageBubble(
                message = ChatMessage(
                    text = "Sorry, I encountered an error while processing your request. Please try again.",
                    type = ChatMessageType.Error,
                    timestamp = 1
                )
            )
        }
    }
}

@ThemePreview
@Composable
private fun ChefThinkingAnimationPreview() {
    RecipesTheme {
        Surface(
            color = colorScheme.tertiaryContainer,
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                ChefThinkingAnimation()
            }
        }
    }
}

@ThemePreview
@Composable
private fun ChatInputFieldPreview() {
    RecipesTheme {
        Column {
            ChatInputField(
                onSendMessage = {},
                modifier = Modifier.fillMaxWidth(),
                isEnabled = true,
                hazeState = HazeState(),
            )
            ChatInputField(
                onSendMessage = {},
                modifier = Modifier.fillMaxWidth(),
                isEnabled = false,
                hazeState = HazeState(),
            )
        }
    }
}