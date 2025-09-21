package org.balch.recipes.core.models

sealed class SearchType(
    val displayText: String,
) {
    abstract val searchText: String

    data class Area(override val searchText: String) : SearchType("Cuisine")
    data class Category(override val searchText: String) : SearchType("Categories")
    data class Ingredient(override val searchText: String) : SearchType("Ingredients")
    data class Search(override val searchText: String) : SearchType(searchText)
}
