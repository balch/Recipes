package org.balch.recipes.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.IconButtonDefaults.iconButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.balch.recipes.AiChatScreen
import org.balch.recipes.RecipeRoute
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.theme.ThemePreview
import org.balch.recipes.ui.utils.sharedBounds

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AiFloatingToolbar(
    modifier: Modifier = Modifier,
    onNavigateTo: (RecipeRoute) -> Unit,
    expanded: Boolean,
    moodTintColor: Color?,
) {

    // The toolbar should receive focus before the screen content, so place it first.
    // Make sure to set its zIndex so it's above the screen content visually.
    HorizontalFloatingToolbar(
        modifier = modifier
            .clickable(onClick = { onNavigateTo(AiChatScreen) }),
        expanded = expanded,
        leadingContent = {
            Text(
                modifier = Modifier
                    .sharedBounds("RecipeMaestroText")
                    .defaultMinSize(minWidth = 56.dp)
                    .padding(8.dp),
                text = "Maestro"
            )
        },
        content = {
            FilledIconButton(
                onClick = { onNavigateTo(AiChatScreen) },
                colors = iconButtonColors(
                    moodTintColor?.copy(alpha = .6f) ?: Color.Unspecified,
                )
            ) {
                RecipeMaestroWidget(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    fontSize = 42.sp,
                )
            }
        },
    )
}

@ThemePreview
@Composable
fun AiFloatingToolbarPreview(
    expanded: Boolean = true,
) {
    RecipesTheme {
        AiFloatingToolbar(
            onNavigateTo = {},
            expanded = expanded,
            moodTintColor = Color.Green,
        )
    }
}

@ThemePreview
@Composable
fun AiFloatingToolbarCollapsedPreview() =
    AiFloatingToolbarPreview(expanded = false)
