package org.balch.recipes.core.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data classes representing the API response for meals.
 */
@Serializable
data class Meal(
    @SerialName("idMeal")
    val id: String,
    @SerialName("strMeal")
    val name: String,
    @SerialName("strDrinkAlternate")
    val drinkAlternate: String? = null,
    @SerialName("strCategory")
    val category: String,
    @SerialName("strArea")
    val area: String,
    @SerialName("strInstructions")
    val instructions: String,
    @SerialName("strMealThumb")
    val thumbnail: String?,
    @SerialName("strTags")
    val tags: String? = null,
    @SerialName("strYoutube")
    val youtube: String? = null,
    @SerialName("strSource")
    val source: String? = null,
    @SerialName("strImageSource")
    val imageSource: String? = null,
    @SerialName("strCreativeCommonsConfirmed")
    val creativeCommonsConfirmed: String? = null,
    @SerialName("dateModified")
    val dateModified: String? = null,
    // Ingredients (up to 20)
    @SerialName("strIngredient1") val ingredient1: String? = null,
    @SerialName("strIngredient2") val ingredient2: String? = null,
    @SerialName("strIngredient3") val ingredient3: String? = null,
    @SerialName("strIngredient4") val ingredient4: String? = null,
    @SerialName("strIngredient5") val ingredient5: String? = null,
    @SerialName("strIngredient6") val ingredient6: String? = null,
    @SerialName("strIngredient7") val ingredient7: String? = null,
    @SerialName("strIngredient8") val ingredient8: String? = null,
    @SerialName("strIngredient9") val ingredient9: String? = null,
    @SerialName("strIngredient10") val ingredient10: String? = null,
    @SerialName("strIngredient11") val ingredient11: String? = null,
    @SerialName("strIngredient12") val ingredient12: String? = null,
    @SerialName("strIngredient13") val ingredient13: String? = null,
    @SerialName("strIngredient14") val ingredient14: String? = null,
    @SerialName("strIngredient15") val ingredient15: String? = null,
    @SerialName("strIngredient16") val ingredient16: String? = null,
    @SerialName("strIngredient17") val ingredient17: String? = null,
    @SerialName("strIngredient18") val ingredient18: String? = null,
    @SerialName("strIngredient19") val ingredient19: String? = null,
    @SerialName("strIngredient20") val ingredient20: String? = null,
    // Measures (up to 20)
    @SerialName("strMeasure1") val measure1: String? = null,
    @SerialName("strMeasure2") val measure2: String? = null,
    @SerialName("strMeasure3") val measure3: String? = null,
    @SerialName("strMeasure4") val measure4: String? = null,
    @SerialName("strMeasure5") val measure5: String? = null,
    @SerialName("strMeasure6") val measure6: String? = null,
    @SerialName("strMeasure7") val measure7: String? = null,
    @SerialName("strMeasure8") val measure8: String? = null,
    @SerialName("strMeasure9") val measure9: String? = null,
    @SerialName("strMeasure10") val measure10: String? = null,
    @SerialName("strMeasure11") val measure11: String? = null,
    @SerialName("strMeasure12") val measure12: String? = null,
    @SerialName("strMeasure13") val measure13: String? = null,
    @SerialName("strMeasure14") val measure14: String? = null,
    @SerialName("strMeasure15") val measure15: String? = null,
    @SerialName("strMeasure16") val measure16: String? = null,
    @SerialName("strMeasure17") val measure17: String? = null,
    @SerialName("strMeasure18") val measure18: String? = null,
    @SerialName("strMeasure19") val measure19: String? = null,
    @SerialName("strMeasure20") val measure20: String? = null
) {
    // Helper function to get ingredients with measurements
    val ingredientsWithMeasures: List<Pair<String, String>>
        get() {
            val ingredients = listOfNotNull(
                ingredient1, ingredient2, ingredient3, ingredient4, ingredient5,
                ingredient6, ingredient7, ingredient8, ingredient9, ingredient10,
                ingredient11, ingredient12, ingredient13, ingredient14, ingredient15,
                ingredient16, ingredient17, ingredient18, ingredient19, ingredient20
            ).filter { it.isNotBlank() }
            
            val measures = listOfNotNull(
                measure1, measure2, measure3, measure4, measure5,
                measure6, measure7, measure8, measure9, measure10,
                measure11, measure12, measure13, measure14, measure15,
                measure16, measure17, measure18, measure19, measure20
            ).filter { it.isNotBlank() }
            
            return ingredients.zip(measures)
        }

    fun toMealSummary(): MealSummary = MealSummary(id, name, thumbnail, youtube)
}

@Serializable
data class MealResponse(
    @SerialName("meals")
    val meals: List<Meal>
)

@Serializable
@Parcelize
data class MealSummary(
    @SerialName("idMeal")
    val id: String,
    @SerialName("strMeal")
    val name: String,
    @SerialName("strMealThumb")
    val thumbnail: String?,
    @SerialName("strYoutube")
    val youtube: String? = null,
): Parcelable


@Serializable
data class MealSummaryResponse(
    @SerialName("meals")
    val meals: List<MealSummary>?
)