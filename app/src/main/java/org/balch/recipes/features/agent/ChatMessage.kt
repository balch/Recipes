package org.balch.recipes.features.agent

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


enum class ChatMessageType {
    User,
    Agent,
    Loading,
    Error,
}

/**
 * Represents a chat message in the AI conversation
 */
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val type: ChatMessageType,
    val timestamp: Long = System.currentTimeMillis(),
)

@Composable
fun ChatMessageType.textColor(): Color = when (this) {
    ChatMessageType.User -> MaterialTheme.colorScheme.onPrimaryContainer
    ChatMessageType.Agent -> MaterialTheme.colorScheme.onSecondaryContainer
    ChatMessageType.Loading -> MaterialTheme.colorScheme.onTertiaryContainer
    ChatMessageType.Error -> MaterialTheme.colorScheme.onErrorContainer
}

@Composable
fun ChatMessageType.containerColor(): Color = when (this) {
    ChatMessageType.User -> MaterialTheme.colorScheme.primaryContainer
    ChatMessageType.Agent -> MaterialTheme.colorScheme.secondaryContainer
    ChatMessageType.Loading -> MaterialTheme.colorScheme.tertiaryContainer
    ChatMessageType.Error -> MaterialTheme.colorScheme.errorContainer
}
