package org.balch.recipes.core.models

sealed interface DetailType {
    data class Lookup(val mealId: String) : DetailType
    data class Content(val meal: Meal) : DetailType
    data object Random : DetailType
}