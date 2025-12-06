package org.balch.recipes.features.agent

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import org.balch.recipes.features.agent.chat.ChatMessage
import org.balch.recipes.features.agent.chat.ChatMessageType

/**
 * Preview parameter provider for different chat states
 */
class ChatMessagesPreviewProvider : PreviewParameterProvider<List<ChatMessage>> {
    override val values: Sequence<List<ChatMessage>> = sequenceOf(
        // Initial state
        listOf(
            ChatMessage(text = "Hi, how can I help?", type = ChatMessageType.Agent, timestamp = 1)
        ),
        // Conversation
        listOf(
            ChatMessage(text = "Hi, how can I help?", type = ChatMessageType.Agent, timestamp = 1),
            ChatMessage(
                text = "What's a good recipe for pasta?",
                type = ChatMessageType.User,
                timestamp = 2
            ),
            ChatMessage(
                text = "I'd recommend Spaghetti Carbonara! It's a classic Italian dish with eggs, cheese, pancetta, and black pepper. Would you like the detailed recipe?",
                type = ChatMessageType.Agent,
                timestamp = 3
            ),
            ChatMessage(text = "Yes please!", type = ChatMessageType.User, timestamp = 4)
        ),
        // With loading
        listOf(
            ChatMessage(text = "Hi, how can I help?", type = ChatMessageType.Agent, timestamp = 1),
            ChatMessage(
                text = "How do I make risotto?",
                type = ChatMessageType.User,
                timestamp = 2
            ),
            ChatMessage(text = "Thinking", type = ChatMessageType.Loading, timestamp = 3)
        ),
        // With error
        listOf(
            ChatMessage(text = "Hi, how can I help?", type = ChatMessageType.Agent, timestamp = 1),
            ChatMessage(text = "Tell me about recipes", type = ChatMessageType.User, timestamp = 2),
            ChatMessage(
                text = "Sorry, I encountered an error. Please try again.",
                type = ChatMessageType.Error,
                timestamp = 3
            )
        ),
        // Mixed conversation
        listOf(
            ChatMessage(text = "Hi, how can I help?", type = ChatMessageType.Agent, timestamp = 1),
            ChatMessage(
                text = "What's a healthy breakfast?",
                type = ChatMessageType.User,
                timestamp = 2
            ),
            ChatMessage(
                text = "Great question! Here are some healthy breakfast options:\n\n• Oatmeal with fresh berries\n• Greek yogurt parfait\n• Avocado toast with eggs\n• Smoothie bowl",
                type = ChatMessageType.Agent,
                timestamp = 3
            ),
            ChatMessage(
                text = "Tell me more about the smoothie bowl",
                type = ChatMessageType.User,
                timestamp = 4
            ),
            ChatMessage(
                text = "A smoothie bowl is like a thick smoothie served in a bowl with toppings! Blend frozen fruits with a little liquid, pour into a bowl, and top with granola, fresh fruits, nuts, and seeds.",
                type = ChatMessageType.Agent,
                timestamp = 5
            ),
            ChatMessage(
                text = "Sounds delicious! What fruits work best?",
                type = ChatMessageType.User,
                timestamp = 6
            ),
            ChatMessage(
                text = "Grapefruit</br></br>My favorite",
                type = ChatMessageType.Agent,
                timestamp = 7
            )
        )
    )
}
