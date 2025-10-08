package org.balch.recipes.core.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data classes representing the API response for areas.
 */
@Serializable
@Parcelize
data class Area(
    @SerialName("strArea")
    val name: String
): UniqueItem, Parcelable {
    override val id = name
}

@Serializable
data class AreasResponse(
    @SerialName("meals")
    val meals: List<Area>
)