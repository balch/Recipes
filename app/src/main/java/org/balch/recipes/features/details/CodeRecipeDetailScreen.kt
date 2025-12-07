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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onVisibilityChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.balch.recipes.core.models.CodeRecipe
import org.balch.recipes.core.models.color
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.theme.ThemePreview
import org.balch.recipes.ui.utils.TransitionKeySuffix.KEY_CODE_RECIPE_TITLE
import org.balch.recipes.ui.utils.sharedBounds
import org.balch.recipes.ui.widgets.CodeRecipeAreaBadge
import org.balch.recipes.ui.widgets.MarkdownCodeSnippet

@Composable
fun CodeDetailItem(
    modifier: Modifier = Modifier,
    codeRecipe: CodeRecipe,
    onTittleVisible: (Boolean) -> Unit,
    topBarHazeState: HazeState,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .hazeSource(topBarHazeState),
            colors = CardDefaults.cardColors(
                containerColor = codeRecipe.area.color().copy(alpha = 0.1f)
            )
        ) {
            CodeRecipeAreaBadge(
                codeRecipe = codeRecipe,
                largeFont = true,
                modifier = Modifier
                    .padding(start = 12.dp, top = 16.dp),
            )

            Text(
                text = codeRecipe.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp, top = 16.dp)
                    .fillMaxWidth()
                    .sharedBounds("${KEY_CODE_RECIPE_TITLE}-${codeRecipe.id}")
                    .onVisibilityChanged { isVisible -> onTittleVisible(isVisible) }
            )

            MarkdownCodeSnippet(
                codeSnippet = codeRecipe.description,
                modifier = Modifier.fillMaxWidth()
            )

            MarkdownCodeSnippet(
                codeSnippet = codeRecipe.codeSnippet,
                modifier = Modifier.fillMaxWidth()
            )
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
            CodeDetailItem(
                codeRecipe = uiState.codeRecipe,
                onTittleVisible = {},
                topBarHazeState = HazeState()
            )
        }
    }
}