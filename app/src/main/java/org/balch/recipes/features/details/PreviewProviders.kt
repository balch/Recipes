package org.balch.recipes.features.details

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import org.balch.recipes.core.models.Meal
import org.balch.recipes.core.models.MealSummary
import org.balch.recipes.ui.preview.CodeRecipeProvider

class DetailStateProvider : PreviewParameterProvider<UiState> {
    override val values = mutableListOf(
        UiState.Loading(),
        UiState.Loading(MealSummary("1", "Spaghetti Carbonara", "https://via.placeholder.com/400x300")),
        UiState.ShowMeal(previewMeal),
        UiState.Error("Something went wrong"),
    ).apply {
        addAll<UiState>(
            DetailCodeRecipeProvider().values
        )
    }.asSequence()

    companion object {
        val previewMeal = Meal(
            id = "1",
            name = "Spaghetti Carbonara",
            category = "Pasta",
            area = "Italian",
            instructions = "1. Bring a large pot of salted water to boil. Add spaghetti and cook according to package directions.\r\n2. While pasta cooks, heat oil in a large skillet over medium heat.\r\n3. Add pancetta and cook until crispy, about 5-7 minutes.\r\n4. In a bowl, whisk together eggs, parmesan, salt and pepper.\r\n5. Drain pasta, reserving 1 cup pasta water.\r\n6. Add hot pasta to skillet with pancetta.\r\n7. Remove from heat and quickly stir in egg mixture.\r\n8. Add pasta water as needed to create creamy sauce.\r\n9. Serve immediately with extra parmesan.",
            thumbnail = "https://via.placeholder.com/400x300",
            youtube = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
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
    }
}


class DetailCodeRecipeProvider : PreviewParameterProvider<UiState.ShowCodeRecipe> {
    override val values =
        CodeRecipeProvider().values
            .map { UiState.ShowCodeRecipe(it) }
}
