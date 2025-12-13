package org.balch.recipes.features.agent.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.model.DefaultMarkdownColors
import com.mikepenz.markdown.model.DefaultMarkdownTypography
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.theme.ThemePreview
import org.balch.recipes.ui.widgets.RecipeMaestroWidget
import org.balch.recipes.ui.widgets.TypewriterText

@Composable
internal fun ChatMessageBubble(
    message: ChatMessage,
    animateTypewriter: Boolean = false,
    onAnimationComplete: () -> Unit = {},
) {
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    when (message.type) {
                                        ChatMessageType.Agent -> {
                                            RecipeMaestroWidget(
                                                fontSize = 24.sp,
                                                modifier = Modifier.padding(end = 4.dp)
                                            )
                                            Text(
                                                text = "Maestro",
                                                style = typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = message.type.textColor().copy(alpha = 0.8f)
                                            )
                                        }
                                        ChatMessageType.Error -> {
                                            Text(
                                                text = "⚠️ Error",
                                                style = typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = message.type.textColor().copy(alpha = 0.8f)
                                            )
                                        }
                                        else -> {}
                                    }
                                }
                            }

                            TypewriterText(
                                text = message.text,
                                textId = message.id,
                                animate = animateTypewriter,
                                onAnimationComplete = onAnimationComplete,
                            ) { renderedText ->
                                Markdown(
                                    content = renderedText,
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
