package org.balch.recipes.features.ideas

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import org.balch.recipes.core.models.Area
import org.balch.recipes.core.models.Category
import org.balch.recipes.core.models.CodeArea
import org.balch.recipes.core.models.CodeRecipe
import org.balch.recipes.core.models.Ingredient

class IdeasStateProvider : PreviewParameterProvider<IdeasUiState> {
    override val values = sequenceOf(
        IdeasUiState.Loading,
        IdeasUiState.Error("Something went wrong"),
        IdeasUiState.Categories(
            categories = listOf(
                Category("1", "Category 1", "https://example.com/thumb1.jpg", "Description 1"),
                Category("2", "Category 2", "https://example.com/thumb2.jpg", "Description 2"),
                Category("3", "Category 3", "https://example.com/thumb3.jpg", "Description 3"),
            ),
            codeRecipes = listOf(
                CodeRecipe(1, CodeArea.Theme, "Code Recipe 1", "Description 1"),
                CodeRecipe(2, CodeArea.Navigation, "Code Recipe 2", "Description 2"),
                CodeRecipe(3, CodeArea.Architecture, "Code Recipe 3", "Description 3"),
                CodeRecipe(4, CodeArea.Testing, "Code Recipe 4", "Description 4"),
            )
        ),
        IdeasUiState.Ingredients(
            ingredients = listOf(
                Ingredient("1", "Ingredient 1", "https://example.com/thumb1.jpg", "Description 1"),
                Ingredient("2", "Ingredient 2", "https://example.com/thumb1.jpg", "Description 2"),
                Ingredient("3", "Ingredient 3", "https://example.com/thumb1.jpg", "Description 4"),
                Ingredient("4", "Ingredient 4", "https://example.com/thumb1.jpg", "Description 5"),
            ),
            imageUrl = "https://www.themealdb.com/images/media/meals/qqpwsy1511796276.jpg",
            codeRecipes = listOf(
                CodeRecipe(1, CodeArea.Theme, "Code Recipe 1", "Description 1"),
                CodeRecipe(2, CodeArea.Navigation, "Code Recipe 2", "Description 2"),
                CodeRecipe(3, CodeArea.Architecture, "Code Recipe 3", "Description 3"),
                CodeRecipe(4, CodeArea.Testing, "Code Recipe 4", "Description 4"),
            )
        ),
        IdeasUiState.Areas(
            areas = listOf(
                Area("Area 1"),
                Area("Area 2"),
                Area("Area 3"),
            ),
            imageUrl = "https://www.themealdb.com/images/media/meals/qqpwsy1511796276.jpg",
            codeRecipes = listOf(
                CodeRecipe(1, CodeArea.Theme, "Code Recipe 1", "Description 1"),
                CodeRecipe(2, CodeArea.Navigation, "Code Recipe 2", "Description 2"),
                CodeRecipe(3, CodeArea.Architecture, "Code Recipe 3", "Description 3"),
                CodeRecipe(4, CodeArea.Testing, "Code Recipe 4", "Description 4"),
            )
        ),
        IdeasUiState.CodeRecipes(
            imageUrl = "https://www.themealdb.com/images/media/meals/qqpwsy1511796276.jpg",
            codeRecipes = listOf(
                CodeRecipe(1, CodeArea.Theme, "Code Recipe 1", "Description 1"),
                CodeRecipe(2, CodeArea.Navigation, "Code Recipe 2", "Description 2"),
                CodeRecipe(3, CodeArea.Architecture, "Code Recipe 3", "Description 3"),
                CodeRecipe(4, CodeArea.Testing, "Code Recipe 4", "Description 4"),
            )
        )
    )
}

