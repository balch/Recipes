package org.balch.recipes.core.models

import ai.koog.agents.core.tools.annotations.LLMDescription
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data classes representing the API response for ingredients.
 */
@LLMDescription("Represents a cooking ingredient that can be used in recipes")
@Serializable
@Parcelize
data class Ingredient(
    @property:LLMDescription("Unique identifier for the ingredient")
    @SerialName("idIngredient")
    override val id: String,
    @property:LLMDescription("The name of the ingredient (e.g., 'Chicken', 'Tomato', 'Salt')")
    @SerialName("strIngredient")
    val name: String,
    @property:LLMDescription("Detailed description of the ingredient, if available")
    @SerialName("strDescription")
    val description: String? = null,
    @property:LLMDescription("The type or category of the ingredient, if available")
    @SerialName("strType")
    val type: String? = null
): UniqueItem, Parcelable

@LLMDescription("API response wrapper containing a list of ingredients")
@Serializable
data class IngredientsResponse(
    @property:LLMDescription("List of available ingredients")
    @SerialName("meals")
    val meals: List<Ingredient>
)