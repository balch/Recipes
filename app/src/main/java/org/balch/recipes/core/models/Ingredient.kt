package org.balch.recipes.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data classes representing the API response for ingredients.
 */
@Serializable
data class Ingredient(
    @SerialName("idIngredient")
    override val id: String,
    @SerialName("strIngredient")
    val name: String,
    @SerialName("strDescription")
    val description: String? = null,
    @SerialName("strType")
    val type: String? = null
): UniqueItem

@Serializable
data class IngredientsResponse(
    @SerialName("meals")
    val meals: List<Ingredient>
)