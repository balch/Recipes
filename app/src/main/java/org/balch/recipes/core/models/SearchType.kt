package org.balch.recipes.core.models

import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.Serializable

@LLMDescription("Represents different types of recipe search filters available in the app")
@Serializable
sealed class SearchType(
    @property:LLMDescription("The display text shown to the user for this search type")
    val displayText: String,
) {
    @property:LLMDescription("The search query or filter text to be applied")
    abstract val searchText: String

    @LLMDescription("Search recipes by geographical area or cuisine type")
    @Serializable
    data class Area(override val searchText: String) : SearchType("Cuisine")
    @LLMDescription("Search recipes by category (e.g., Breakfast, Dessert)")
    @Serializable
    data class Category(override val searchText: String) : SearchType("Categories")
    @LLMDescription("Search recipes by ingredient name")
    @Serializable
    data class Ingredient(override val searchText: String) : SearchType("Ingredients")
    @LLMDescription("General text search for recipes")
    @Serializable
    data class Search(override val searchText: String) : SearchType(searchText)
}
