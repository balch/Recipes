package org.balch.recipes.core.models

import ai.koog.agents.core.tools.annotations.LLMDescription
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data classes representing the API response for categories.
 */
@LLMDescription("Represents a recipe category such as Breakfast, Dessert, Seafood, etc.")
@Serializable
@Parcelize
data class Category(
    @property:LLMDescription("Unique identifier for the category")
    @SerialName("idCategory")
    override val id: String,
    @property:LLMDescription("The name of the category (e.g., 'Breakfast', 'Dessert', 'Seafood')")
    @SerialName("strCategory")
    val name: String,
    @property:LLMDescription("URL to the category thumbnail image")
    @SerialName("strCategoryThumb")
    val thumbnail: String,
    @property:LLMDescription("Detailed description of the category")
    @SerialName("strCategoryDescription")
    val description: String
): UniqueItem, Parcelable

@LLMDescription("API response wrapper containing a list of recipe categories")
@Serializable
data class CategoriesResponse(
    @property:LLMDescription("List of available recipe categories")
    @SerialName("categories")
    val categories: List<Category>
)