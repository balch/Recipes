package org.balch.recipes.ui.widgets.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
fun InputRowContainer(
    text: String,
    onTextChange: (String) -> Unit,
    enabled: Boolean,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null,
    trailing: @Composable () -> Unit = {},
) {
    val shape = RoundedCornerShape(24.dp)

    // Use a Surface to get proper tonal elevation in light/dark
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
                value = text,
                onValueChange = onTextChange,
                enabled = enabled,
                isError = isError,
                placeholder = { Text("Ask AI about recipes…") },
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
            trailing()
        }
    }

    if (supportingText != null) {
        Text(
            text = supportingText,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp, top = 4.dp)
        )
    }
}

@ThemePreview
@Composable
fun InputRowContainerPreview(
    enabled: Boolean = true,
    text: String = "Enabled"
) {
    RecipesTheme {
        InputRowContainer(
            text = text,
            onTextChange = {},
            enabled = enabled,
            hazeState = HazeState()
        )
    }
}

@ThemePreview
@Composable
fun InputRowContainerDisabledPreview() =
    InputRowContainerPreview(false, "Disabled")
