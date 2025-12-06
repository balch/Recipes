package org.balch.recipes.features.agent.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.theme.ThemePreview

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class,
)
@Composable
internal fun ChatInputField(
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
