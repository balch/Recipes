package org.balch.recipes.features.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.ktor.websocket.Frame
import org.balch.recipes.core.models.CodeArea
import org.balch.recipes.core.models.CodeRecipe
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.theme.ThemePreview
import org.balch.recipes.ui.widgets.WebViewScreen

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
        // Area/Category Card
        CodeRecipeAreaCard(
            codeRecipe = codeRecipe,
            modifier = Modifier.fillMaxWidth()
        )

        // Description Card
        CodeRecipeDescriptionCard(
            codeRecipe = codeRecipe,
            modifier = Modifier.fillMaxWidth()
        )

        CodeRecipeGithubCard(
            codeRecipe = codeRecipe,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CodeRecipeAreaCard(
    codeRecipe: CodeRecipe,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = codeRecipe.area.color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = codeRecipe.area.color,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = codeRecipe.area.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeRecipeDescriptionCard(
    codeRecipe: CodeRecipe,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = codeRecipe.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CodeRecipeGithubCard(
    codeRecipe: CodeRecipe,
    modifier: Modifier = Modifier
) {
    if (codeRecipe.codeSnippet != null) {
        Card(modifier = modifier.fillMaxHeight()) {
            Text(codeRecipe.codeSnippet)
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