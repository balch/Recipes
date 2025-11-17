package org.balch.recipes.ui.widgets.input

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import org.balch.recipes.RecipeRoute
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.theme.ThemePreview

@Composable
fun CompactionInputBox(
    prompt: String,
    enabled: Boolean,
    isError: Boolean,
    onSendPrompt: (String) -> Unit,
    onNavigateTo: (RecipeRoute) -> Unit,
    hazeState: HazeState,
) {
    InputRowContainer(
        text = prompt,
        onTextChange = onSendPrompt,
        enabled = enabled,
        isError = isError,
        hazeState = hazeState,
    ) {
        IconButton(
            enabled = enabled,
            modifier = Modifier
                .hazeEffect(hazeState, LocalHazeStyle.current),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
            ),
            onClick = {
                val toSend = prompt.trim()
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

@ThemePreview
@Composable
fun CompactionInputBoxPreview(
    enabled: Boolean = true,
    prompt: String = "Enabled",
    isError: Boolean = false,
) {
    RecipesTheme {
        CompactionInputBox(
            prompt = prompt,
            enabled = enabled,
            onSendPrompt = {},
            onNavigateTo = {},
            hazeState = HazeState(),
            isError = isError,
        )
    }
}

@ThemePreview
@Composable
fun CompactionInputBoxDisabledPreview() =
    CompactionInputBoxPreview(false, "Disabled")

@ThemePreview
@Composable
fun CompactionInputBoxErrorPreview() =
    CompactionInputBoxPreview(true, "Error", true)

@ThemePreview
@Composable
fun CompactionInputBoxErrorDisabledPreview() =
    CompactionInputBoxPreview(false, "Error", true)
