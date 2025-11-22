package org.balch.recipes.ui.widgets

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import org.balch.recipes.features.agent.ai.AppContextData
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.theme.ThemePreview
import org.balch.recipes.ui.utils.sharedBounds

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AiFloatingActionWidget(
    modifier: Modifier = Modifier,
    appContext: AppContextData,
    onNavigateTo: (RecipeRoute) -> Unit,
    expanded: Boolean,
    moodTintColor: Color?,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
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
                    .sharedBounds(
                        key = "RecipeMaestroText",
                        animatedVisibilityScope = animatedVisibilityScope,
                        sharedTransitionScope = sharedTransitionScope,
                    )
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
                    fontSize = 24.sp,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                )
            }
        },
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@ThemePreview
@Composable
fun AiFloatingActionWidgetPreview(
    expanded: Boolean = true,
) {
    RecipesTheme {
        AiFloatingActionWidget(
            onNavigateTo = {},
            expanded = expanded,
            appContext = AppContextData("Preview Context", "Test"),
            moodTintColor = Color.Green,
            animatedVisibilityScope = null,
            sharedTransitionScope = null,
        )
    }
}

@ThemePreview
@Composable
fun CompactionInputBoxDisabledPreview() =
    AiFloatingActionWidgetPreview(expanded = false)

