package org.balch.recipes.core.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class SearchType(
    val displayText: String,
) : Parcelable {
    abstract val searchText: String

    @Parcelize
    data class Area(override val searchText: String) : SearchType("Cuisine")
    @Parcelize
    data class Category(override val searchText: String) : SearchType("Categories")
    @Parcelize
    data class Ingredient(override val searchText: String) : SearchType("Ingredients")
    @Parcelize
    data class Search(override val searchText: String) : SearchType(searchText)
}