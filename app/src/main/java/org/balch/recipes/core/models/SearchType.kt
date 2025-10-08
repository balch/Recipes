package org.balch.recipes.core.models

import kotlinx.serialization.Serializable

@Serializable
sealed class SearchType(
    val displayText: String,
) {
    abstract val searchText: String

    @Serializable
    data class Area(override val searchText: String) : SearchType("Cuisine")
    @Serializable
    data class Category(override val searchText: String) : SearchType("Categories")
    @Serializable
    data class Ingredient(override val searchText: String) : SearchType("Ingredients")
    @Serializable
    data class Search(override val searchText: String) : SearchType(searchText)
}
