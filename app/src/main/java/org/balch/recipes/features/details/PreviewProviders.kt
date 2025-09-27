package org.balch.recipes.features.details

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import org.balch.recipes.core.models.CodeArea
import org.balch.recipes.core.models.CodeRecipe
import org.balch.recipes.core.models.Meal

class DetailStateProvider : PreviewParameterProvider<UiState> {
    override val values = mutableListOf(
        UiState.Loading,
        UiState.ShowMeal(
            Meal(
                id = "1",
                name = "Spaghetti Carbonara",
                category = "Pasta",
                area = "Italian",
                instructions = "1. Bring a large pot of salted water to boil. Add spaghetti and cook according to package directions.\r\n2. While pasta cooks, heat oil in a large skillet over medium heat.\r\n3. Add pancetta and cook until crispy, about 5-7 minutes.\r\n4. In a bowl, whisk together eggs, parmesan, salt and pepper.\r\n5. Drain pasta, reserving 1 cup pasta water.\r\n6. Add hot pasta to skillet with pancetta.\r\n7. Remove from heat and quickly stir in egg mixture.\r\n8. Add pasta water as needed to create creamy sauce.\r\n9. Serve immediately with extra parmesan.",
                thumbnail = "https://via.placeholder.com/400x300",
                ingredient1 = "Spaghetti",
                ingredient2 = "Pancetta",
                ingredient3 = "Eggs",
                ingredient4 = "Parmesan cheese",
                ingredient5 = "Black pepper",
                measure1 = "400g",
                measure2 = "150g",
                measure3 = "4 large",
                measure4 = "100g grated",
                measure5 = "To taste"
            )
        ),
        UiState.Error("Something went wrong"),
    ).apply {
        addAll<UiState>(
            DetailCodeRecipeProvider().values
        )
    }.asSequence()
}


class DetailCodeRecipeProvider : PreviewParameterProvider<UiState.ShowCodeRecipe> {
    override val values = sequenceOf(

        UiState.ShowCodeRecipe(
            CodeRecipe(
                area = CodeArea.Architecture,
                title = "Architecture Title",
                description = "`Architecture` Description",
                codeSnippet = "print('Hello, Architecture!')"
            )
        ),
        UiState.ShowCodeRecipe(
            CodeRecipe(
                area = CodeArea.Navigation3,
                title = "Navigation Title",
                description = "`Navigation` Description",
                codeSnippet = "print('Hello, Navigation!')"
            )
        ),
        UiState.ShowCodeRecipe(
            CodeRecipe(
                area = CodeArea.Theme,
                title = "Theme Title",
                description = "`Theme` Description",
                codeSnippet = "print('Hello, Theme!')"
            )
        ),
        UiState.ShowCodeRecipe(
            CodeRecipe(
                area = CodeArea.Testing,
                title = "Testing Title",
                description = "`Testing` Description",
                codeSnippet = "print('Hello, Testing!')"
            )
        )
    )
}
