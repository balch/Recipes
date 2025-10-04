package org.balch.recipes.features.search

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import org.balch.recipes.core.models.CodeArea
import org.balch.recipes.core.models.CodeRecipe
import org.balch.recipes.core.models.MealSummary
import org.balch.recipes.core.models.SearchType

class SearchStateProvider : PreviewParameterProvider<SearchUiState> {
    override val values = sequenceOf(
        SearchUiState.Welcome,
        SearchUiState.Loading("Beef", false),
        SearchUiState.Loading("Chick", true),
        SearchUiState.Error("Something went wrong"),
        SearchUiState.Show(
            searchType = SearchType.Category("Beef"),
            items = listOf(
                MealSummary(
                    id = "1",
                    name = "Beef and Mustard Pie",
                    thumbnail = "https://www.themealdb.com/images/media/meals/sytuqu151"
                ).toItemType(),
                MealSummary(
                    id = "2",
                    name = "Beef Stew",
                    thumbnail = "https://www.themealdb.com/images/media/meals/sytuqu152"
                ).toItemType(),
            ),
            isFetching = true,
            searchTerm = "Beef"
        ),
        SearchUiState.Show(
            searchType = SearchType.Search("Chicken"),
            items = listOf(
                CodeRecipe(
                    index = 1,
                    area = CodeArea.Architecture,
                    title = "title 1",
                    description = "description 1",
                ).toItemType(),
                MealSummary(
                    id = "1",
                    name = "Chicken and Mustard Pie",
                    thumbnail = "https://www.themealdb.com/images/media/meals/sytuqu151"
                ).toItemType(),
                MealSummary(
                    id = "2",
                    name = "Chicken Stew",
                    thumbnail = "https://www.themealdb.com/images/media/meals/sytuqu152"
                ).toItemType(),
                CodeRecipe(
                    index = 2,
                    area = CodeArea.Compose,
                    title = "title 2",
                    description = "description 2",
                ).toItemType()
            ),
            isFetching = false,
            searchTerm = "Chicken"
        ),
    )
}
