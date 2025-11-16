package org.balch.recipes.core.models

import ai.koog.agents.core.tools.annotations.LLMDescription
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data classes representing the API response for areas.
 */
@LLMDescription("Represents a geographical area or cuisine type for recipes")
@Serializable
@Parcelize
data class Area(
    @property:LLMDescription("The name of the geographical area or cuisine (e.g., 'Italian', 'Mexican', 'American')")
    @SerialName("strArea")
    val name: String
): UniqueItem, Parcelable {
    @IgnoredOnParcel
    override val id = name
}

@LLMDescription("API response wrapper containing a list of areas")
@Serializable
data class AreasResponse(
    @property:LLMDescription("List of available cuisine areas")
    @SerialName("meals")
    val meals: List<Area>
)