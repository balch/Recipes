package org.balch.recipes.features.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.balch.recipes.core.models.CodeArea
import org.balch.recipes.core.models.CodeRecipe
import org.balch.recipes.core.models.color
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.theme.ThemePreview
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
        CodeRecipeAreaCard(
            codeRecipe = codeRecipe,
        )

        MarkdownCodeSnippet(
            codeSnippet ="#### Description\n\n${codeRecipe.description}",
            color = codeRecipe.area.color(),
            modifier = Modifier.fillMaxWidth()
        )

        if (codeRecipe.codeSnippet != null) {
            MarkdownCodeSnippet(
                codeSnippet = codeRecipe.codeSnippet,
                color = codeRecipe.area.color(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CodeRecipeAreaCard(
    codeRecipe: CodeRecipe,
) {
    Row(modifier = Modifier.fillMaxWidth().padding(6.dp)) {
        Box(
            modifier = Modifier
                .background(
                    color = codeRecipe.area.color(),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = codeRecipe.area.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


@ThemePreview
@Composable
private fun CodeRecipeDetailScreenPreview() {
    RecipesTheme {
        CodeDetailItem(
            codeRecipe = CodeRecipe(
                area = CodeArea.Architecture,
                title = "MVVM with Repository Pattern",
                description = "A clean architecture implementation using MVVM pattern with Repository for data abstraction. This approach separates concerns and makes the code more testable and maintainable.",
                fileName = "UserRepository.kt",
            )
        )
    }
}