package org.balch.recipes.ui.widgets.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.theme.ThemePreview

@Composable
fun CompactionInputBox(
    prompt: String,
    onSendPrompt: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean,
    isError: Boolean,
    hazeState: HazeState,
) {
    val shape = RoundedCornerShape(24.dp)

    Surface(
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        shape = shape,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            OutlinedTextField(
                value = prompt,
                onValueChange = { },
                enabled = enabled,
                isError = isError,
                placeholder = { },
                singleLine = true,
                shape = shape,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                ),
                modifier = Modifier
                    .hazeEffect(hazeState, LocalHazeStyle.current)
                    .weight(1f),
            )

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
                },
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send prompt"
                )
            }
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
            onSendPrompt = {},
            enabled = enabled,
            isError = isError,
            hazeState = HazeState(),
        )
    }
}

@ThemePreview
@Composable
fun CompactionInputBoxDisabledPreview() =
    CompactionInputBoxPreview(enabled = false, prompt = "Disabled")

@ThemePreview
@Composable
fun CompactionInputBoxErrorPreview() =
    CompactionInputBoxPreview(isError = true, prompt = "Error",)

@ThemePreview
@Composable
fun CompactionInputBoxErrorDisabledPreview() =
    CompactionInputBoxPreview(enabled = false, prompt = "Error", isError = true)
