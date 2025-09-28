package org.balch.recipes.core.models

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.balch.recipes.ui.theme.AccentYellow
import org.balch.recipes.ui.theme.DarkBackground
import org.balch.recipes.ui.theme.DeepBrown
import org.balch.recipes.ui.theme.FreshGreen
import org.balch.recipes.ui.theme.LightBackground
import org.balch.recipes.ui.theme.WarmOrange

enum class CodeArea {
    Theme,
    Navigation,
    Architecture,
    Testing
}

/**
 * Returns the theme-aware color for this CodeArea based on the current MaterialTheme.
 * Uses semantically appropriate colors that work well with the warm, food-themed palette
 * and are guaranteed to be defined in both custom and dynamic color schemes.
 */
@Composable
fun CodeArea.color(): Color {
    return when (this) {
        CodeArea.Theme -> WarmOrange
        CodeArea.Navigation -> FreshGreen
        CodeArea.Architecture -> DeepBrown
        CodeArea.Testing -> AccentYellow
    }
}

@Composable
fun CodeArea.textColor(): Color {
    return when (this) {
        CodeArea.Theme -> DarkBackground
        CodeArea.Navigation -> DarkBackground
        CodeArea.Architecture -> LightBackground
        CodeArea.Testing -> DarkBackground
    }
}

data class CodeRecipe(
    val index: Int,
    val area: CodeArea,
    val title: String,
    val description: String,
    val fileName: String? = null,
    val codeSnippet: String? = null,
)