package org.balch.recipes.core.models

import android.os.Parcelable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.parcelize.Parcelize

/**
 * Data classes representing the API response for ingredients.
 */
@Serializable
@Parcelize
data class Ingredient(
    @SerialName("idIngredient")
    override val id: String,
    @SerialName("strIngredient")
    val name: String,
    @SerialName("strDescription")
    val description: String? = null,
    @SerialName("strType")
    val type: String? = null
): UniqueItem, Parcelable

@Serializable
data class IngredientsResponse(
    @SerialName("meals")
    val meals: List<Ingredient>
)