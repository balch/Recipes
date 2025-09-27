package org.balch.recipes.core.models

enum class CodeArea {
    Theme,
    Navigation,
    Architecture,
    Testing
}


data class CodeRecipe(
    val area: CodeArea,
    val title: String,
    val description: String,
    val githubRef: String? = null,
    val fileName: String? = null,
)