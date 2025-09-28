package org.balch.recipes.features.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import org.balch.recipes.core.models.CodeRecipe
import org.balch.recipes.core.models.color
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.theme.ThemePreview
import org.balch.recipes.ui.widgets.CodeRecipeAreaBadge
import org.balch.recipes.ui.widgets.MarkdownCodeSnippet

@Composable
fun CodeDetailItem(
    modifier: Modifier = Modifier,
    codeRecipe: CodeRecipe,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = codeRecipe.area.color().copy(alpha = 0.1f)
            )
        ) {
            CodeRecipeAreaBadge(
                codeRecipe = codeRecipe,
                modifier = modifier
                    .padding(start = 12.dp, top = 16.dp),
            )

            MarkdownCodeSnippet(
                codeSnippet = "#### ${codeRecipe.title}\n\n${codeRecipe.description}",
                modifier = Modifier.fillMaxWidth()
            )

            if (codeRecipe.codeSnippet != null) {
                MarkdownCodeSnippet(
                    codeSnippet = codeRecipe.codeSnippet,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@ThemePreview
@Composable
private fun CodeRecipeDetailScreenPreview(
    @PreviewParameter(DetailCodeRecipeProvider::class) uiState: UiState.ShowCodeRecipe,
) {
    RecipesTheme {
        Box(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            CodeDetailItem(codeRecipe = uiState.codeRecipe)
        }
    }
}