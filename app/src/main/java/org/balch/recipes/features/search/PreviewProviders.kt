package org.balch.recipes.features.search

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
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
            meals = listOf(
                MealSummary(
                    id = "1",
                    name = "Beef and Mustard Pie",
                    thumbnail = "https://www.themealdb.com/images/media/meals/sytuqu151"
                ),
                MealSummary(
                    id = "2",
                    name = "Beef Stew",
                    thumbnail = "https://www.themealdb.com/images/media/meals/sytuqu152"
                )
            ),
            isFetching = true,
            searchTerm = "Beef"
        ),
        SearchUiState.Show(
            searchType = SearchType.Search("Chicken"),
            meals = listOf(
                MealSummary(
                    id = "1",
                    name = "Chicken and Mustard Pie",
                    thumbnail = "https://www.themealdb.com/images/media/meals/sytuqu151"
                ),
                MealSummary(
                    id = "2",
                    name = "Chicken Stew",
                    thumbnail = "https://www.themealdb.com/images/media/meals/sytuqu152"
                )
            ),
            isFetching = false,
            searchTerm = "Chicken"
        ),
    )
}
