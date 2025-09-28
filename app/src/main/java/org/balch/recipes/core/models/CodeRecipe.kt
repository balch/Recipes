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
 * Uses semantically appropriate colors that work well with the warm, food-themed palette
 * and are guaranteed to be defined in both custom and dynamic color schemes.
 */
@Composable
fun CodeArea.color(): Color {
    return when (this) {
        CodeArea.Theme -> MaterialTheme.colorScheme.primary           // Warm orange - perfect for theme-related content
        CodeArea.Navigation -> MaterialTheme.colorScheme.secondary    // Fresh green - represents movement/navigation
        CodeArea.Architecture -> MaterialTheme.colorScheme.tertiary   // Accent yellow - represents structure/foundation
        CodeArea.Testing -> MaterialTheme.colorScheme.error           // Bright red - for errors/testing
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