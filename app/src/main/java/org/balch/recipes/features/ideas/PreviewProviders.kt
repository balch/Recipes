package org.balch.recipes.features.ideas

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import org.balch.recipes.core.models.Category

class IdeasStateProvider : PreviewParameterProvider<IdeasUiState> {
    override val values = sequenceOf(
        IdeasUiState.Loading,
        IdeasUiState.Error("Something went wrong"),
        IdeasUiState.Categories(
            listOf(
                Category("1", "Category 1", "https://example.com/thumb1.jpg", "Description 1"),
                Category("2", "Category 2", "https://example.com/thumb2.jpg", "Description 2"),
                Category("3", "Category 3", "https://example.com/thumb3.jpg", "Description 3"),
            )
        )
    )
}

