package org.balch.recipes.core.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.serialization.Serializable

@Parcelize
sealed interface DetailType : Parcelable {
    data class Lookup(val mealId: String) : DetailType
    data class Content(val meal: @RawValue Meal) : DetailType
    data object Random : DetailType
}