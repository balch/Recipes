package org.balch.recipes.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data classes representing the API response for areas.
 */
@Serializable
data class Area(
    @SerialName("strArea")
    val name: String
): UniqueItem {
    override val id = name
}

@Serializable
data class AreasResponse(
    @SerialName("meals")
    val meals: List<Area>
)