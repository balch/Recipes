package org.balch.recipes.core.models

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class CodeArea {
    Theme,
    Navigation,
    Architecture,
    Testing
}

/**
 * Returns the theme-aware color for this CodeArea based on the current MaterialTheme.
 * Automatically adapts between light and dark themes.
 */
@Composable
fun CodeArea.color(): Color {
    return when (this) {
        CodeArea.Theme -> MaterialTheme.colorScheme.inversePrimary
        CodeArea.Navigation -> MaterialTheme.colorScheme.errorContainer
        CodeArea.Architecture -> MaterialTheme.colorScheme.primaryContainer
        CodeArea.Testing -> MaterialTheme.colorScheme.tertiary
    }
}


data class CodeRecipe(
    val area: CodeArea,
    val title: String,
    val description: String,
    val fileName: String? = null,
    val codeSnippet: String? = null,
)