package org.balch.recipes.core.models

import androidx.compose.ui.graphics.Color
import org.balch.recipes.ui.theme.AccentYellow
import org.balch.recipes.ui.theme.DeepBrown
import org.balch.recipes.ui.theme.FreshGreen
import org.balch.recipes.ui.theme.WarmOrange

enum class CodeArea(val color: Color) {
    Theme(WarmOrange),
    Navigation(FreshGreen),
    Architecture(DeepBrown),
    Testing(AccentYellow)
}


data class CodeRecipe(
    val area: CodeArea,
    val title: String,
    val description: String,
    val githubRef: String? = null,
    val fileName: String? = null,
)