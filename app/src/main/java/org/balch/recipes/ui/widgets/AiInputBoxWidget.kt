package org.balch.recipes.ui.widgets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import org.balch.recipes.AiChatScreen
import org.balch.recipes.AiInquireMode
import org.balch.recipes.RecipeRoute
import org.balch.recipes.ui.preview.AiInputBoxVisibilityStatePreviewProvider
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.theme.ThemePreview
import org.balch.recipes.ui.widgets.input.CompactionInputBox
import org.balch.recipes.ui.widgets.input.FloatingActionMenuButton
import org.balch.recipes.ui.widgets.input.FloatingActionMenuItem

sealed interface AiInputBoxVisibilityState {

    interface Loadable {
        val isLoading: Boolean
    }
    enum class MessageType { Editable, Error, Loading, NotAvailable }

    val message: String

    val isError: Boolean
        get() = messageType == MessageType.Error

    val messageType: MessageType
        get() = when (this) {
            is Collapsed, is Expanded ->
                (this as Loadable).let {
                    if (it.isLoading) MessageType.Loading
                    else MessageType.Editable
                }
            is Error -> MessageType.Error
            Gone, FloatingActionBox -> MessageType.NotAvailable
        }

    // AI TODO - use the comments below to derive the requirements of the AiInputBoxWidget. This Widget will need to replace the FloatingActionBox in MainActivity

    /**
     * Represents a state where the AI input box is collapsed and editable.
     * In this state the input box is editable on a single Single line,
     * but automatically expands to [Expanded] state when takes
     * input box contains more than one row of text
     *
     * @property message editable text the displayed to the user
     */
    data class Collapsed(
        override val message: String,
        override val isLoading: Boolean = false,
    ) : AiInputBoxVisibilityState, Loadable

    /**
     * Represents a state where the AI input box is collapsed, readonly
     * and denotes an error condition
     * In this state the input box is readonly on a single Single line,
     * and displayed in a manner that indicates an error state
     *
     * @property message readonly error text the displayed to the user
     */
    data class Error(override val message: String) : AiInputBoxVisibilityState

    /**
     * Represents a state where the AI is available but input is not visible to the user.
     *
     * Clicking this will display the agent in a collapsed state.
     */
    object FloatingActionBox : AiInputBoxVisibilityState { override val message = "" }

    /**
     * Represents a state where the AI input box is expanded and editable.
     * In this state the input box is editable and displays in a box
     * capable of displaying 4 lines and scrolls vertically to accommodate
     * longer messages up to 512 characters. The expanded state should should
     * more info under the box including character count, submit, and
     * cancel buttons.
     *
     * @property message editable text the displayed to the user
     * @property isLoading whether the AI is currently processing the prompt
     */
    data class Expanded(
        override val message: String,
        override val isLoading: Boolean = false,
    ) : AiInputBoxVisibilityState, Loadable

    /**
     * Represents a state where AI is not available
     * and the input is not visible to the user.
     */
    object Gone : AiInputBoxVisibilityState { override val message = "" }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AiInputBoxWidget(
    uiState: AiInputBoxVisibilityState,
    prompt: String,
    hazeState: HazeState,
    onNavigateTo: (RecipeRoute) -> Unit,
    onSendPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    val menuItems = remember {
        listOf(
            FloatingActionMenuItem(Icons.AutoMirrored.Filled.Chat, "Let's Chat") {
                onNavigateTo(AiChatScreen())
            },
            FloatingActionMenuItem(Icons.AutoMirrored.Filled.Send, "Context Inquiry") {
                onNavigateTo(AiInquireMode)
            }
        )
    }

    SharedTransitionLayout {
        AnimatedContent(
            targetState = uiState,
            modifier = modifier,
        ) {

            var text by remember(prompt) { mutableStateOf(prompt) }

            when (it) {
                AiInputBoxVisibilityState.Gone -> { }// No UI
                AiInputBoxVisibilityState.FloatingActionBox -> {
                    FloatingActionMenuButton(
                        items = menuItems,
                        onNavigateTo = onNavigateTo,
                    )
                }

                is AiInputBoxVisibilityState.Collapsed -> {
                    CompactionInputBox(
                        prompt = it.message,
                        isError = it.isError,
                        onNavigateTo = onNavigateTo,
                        onSendPrompt = onSendPrompt,
                        enabled = !it.isLoading,
                        hazeState = hazeState,
                    )
                }

                is AiInputBoxVisibilityState.Error -> {
                    CompactionInputBox(
                        prompt = it.message,
                        onNavigateTo = onNavigateTo,
                        onSendPrompt = onSendPrompt,
                        enabled =false,
                        isError = true,
                        hazeState = hazeState,
                    )
                }

                is AiInputBoxVisibilityState.Expanded -> {
                    val enabled = !it.isLoading
                    val shape = RoundedCornerShape(24.dp)
                    Surface(
                        tonalElevation = 6.dp,
                        shadowElevation = 8.dp,
                        shape = shape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(shape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                                .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = text,
                                onValueChange = { if (it.length <= 512) text = it },
                                enabled = enabled,
                                placeholder = { Text("Ask AI about recipes…") },
                                singleLine = false,
                                shape = shape,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                                ),
                                modifier = Modifier
                                    .weight(1f)
                            )

                            IconButton(
                                enabled = enabled,
                                onClick = {
                                    val toSend = text.trim()
                                    if (toSend.isNotEmpty()) {
                                        onSendPrompt(toSend)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send prompt"
                                )
                            }
                        }
                    }

                    // Character count + hint row
                    Text(
                        text = "${text.length}/512",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 28.dp, top = 4.dp)
                    )
                }
            }
        }
    }
}


@ThemePreview
@Composable
private fun AiInputBoxWidgetPreview(
    @PreviewParameter(AiInputBoxVisibilityStatePreviewProvider::class) visibilityState: AiInputBoxVisibilityState,
) {
    RecipesTheme {
        AiInputBoxWidget(
            uiState = visibilityState,
            prompt = "What is the weather like in Boston?",
            onNavigateTo = {},
            onSendPrompt = {},
            hazeState = HazeState()
        )
    }
}
