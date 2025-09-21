package org.balch.recipes.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data classes representing the API response for categories.
 */
@Serializable
data class Category(
    @SerialName("idCategory")
    override val id: String,
    @SerialName("strCategory")
    val name: String,
    @SerialName("strCategoryThumb")
    val thumbnail: String,
    @SerialName("strCategoryDescription")
    val description: String
): UniqueItem

@Serializable
data class CategoriesResponse(
    @SerialName("categories")
    val categories: List<Category>
)