package org.balch.recipes.core.models

import ai.koog.agents.core.tools.annotations.LLMDescription
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data classes representing the API response for meals.
 */
@LLMDescription("Represents a complete meal recipe with full details including ingredients, measurements, and cooking instructions")
@Serializable
data class Meal(
    @property:LLMDescription("Unique identifier for the meal")
    @SerialName("idMeal")
    val id: String,
    @property:LLMDescription("The name of the meal or recipe")
    @SerialName("strMeal")
    val name: String,
    @property:LLMDescription("Alternative drink pairing suggestion, if available")
    @SerialName("strDrinkAlternate")
    val drinkAlternate: String? = null,
    @property:LLMDescription("The category this meal belongs to (e.g., 'Dessert', 'Seafood')")
    @SerialName("strCategory")
    val category: String,
    @property:LLMDescription("The geographical area or cuisine type (e.g., 'Italian', 'Mexican')")
    @SerialName("strArea")
    val area: String,
    @property:LLMDescription("Complete cooking instructions for preparing the meal")
    @SerialName("strInstructions")
    val instructions: String,
    @property:LLMDescription("URL to the meal's thumbnail image")
    @SerialName("strMealThumb")
    val thumbnail: String?,
    @property:LLMDescription("Comma-separated tags describing the meal (e.g., 'Spicy', 'Vegetarian')")
    @SerialName("strTags")
    val tags: String? = null,
    @property:LLMDescription("YouTube video URL for the recipe tutorial, if available")
    @SerialName("strYoutube")
    val youtube: String? = null,
    @property:LLMDescription("Original source URL for the recipe, if available")
    @SerialName("strSource")
    val source: String? = null,
    @property:LLMDescription("Source URL for the meal image, if available")
    @SerialName("strImageSource")
    val imageSource: String? = null,
    @property:LLMDescription("Creative Commons license confirmation status")
    @SerialName("strCreativeCommonsConfirmed")
    val creativeCommonsConfirmed: String? = null,
    @property:LLMDescription("Date when the meal data was last modified")
    @SerialName("dateModified")
    val dateModified: String? = null,
    // Ingredients (up to 20)
    @property:LLMDescription("First ingredient name")
    @SerialName("strIngredient1") val ingredient1: String? = null,
    @property:LLMDescription("Second ingredient name")
    @SerialName("strIngredient2") val ingredient2: String? = null,
    @property:LLMDescription("Third ingredient name")
    @SerialName("strIngredient3") val ingredient3: String? = null,
    @property:LLMDescription("Fourth ingredient name")
    @SerialName("strIngredient4") val ingredient4: String? = null,
    @property:LLMDescription("Fifth ingredient name")
    @SerialName("strIngredient5") val ingredient5: String? = null,
    @property:LLMDescription("Sixth ingredient name")
    @SerialName("strIngredient6") val ingredient6: String? = null,
    @property:LLMDescription("Seventh ingredient name")
    @SerialName("strIngredient7") val ingredient7: String? = null,
    @property:LLMDescription("Eighth ingredient name")
    @SerialName("strIngredient8") val ingredient8: String? = null,
    @property:LLMDescription("Ninth ingredient name")
    @SerialName("strIngredient9") val ingredient9: String? = null,
    @property:LLMDescription("Tenth ingredient name")
    @SerialName("strIngredient10") val ingredient10: String? = null,
    @property:LLMDescription("Eleventh ingredient name")
    @SerialName("strIngredient11") val ingredient11: String? = null,
    @property:LLMDescription("Twelfth ingredient name")
    @SerialName("strIngredient12") val ingredient12: String? = null,
    @property:LLMDescription("Thirteenth ingredient name")
    @SerialName("strIngredient13") val ingredient13: String? = null,
    @property:LLMDescription("Fourteenth ingredient name")
    @SerialName("strIngredient14") val ingredient14: String? = null,
    @property:LLMDescription("Fifteenth ingredient name")
    @SerialName("strIngredient15") val ingredient15: String? = null,
    @property:LLMDescription("Sixteenth ingredient name")
    @SerialName("strIngredient16") val ingredient16: String? = null,
    @property:LLMDescription("Seventeenth ingredient name")
    @SerialName("strIngredient17") val ingredient17: String? = null,
    @property:LLMDescription("Eighteenth ingredient name")
    @SerialName("strIngredient18") val ingredient18: String? = null,
    @property:LLMDescription("Nineteenth ingredient name")
    @SerialName("strIngredient19") val ingredient19: String? = null,
    @property:LLMDescription("Twentieth ingredient name")
    @SerialName("strIngredient20") val ingredient20: String? = null,
    // Measures (up to 20)
    @property:LLMDescription("Measurement or quantity for the first ingredient")
    @SerialName("strMeasure1") val measure1: String? = null,
    @property:LLMDescription("Measurement or quantity for the second ingredient")
    @SerialName("strMeasure2") val measure2: String? = null,
    @property:LLMDescription("Measurement or quantity for the third ingredient")
    @SerialName("strMeasure3") val measure3: String? = null,
    @property:LLMDescription("Measurement or quantity for the fourth ingredient")
    @SerialName("strMeasure4") val measure4: String? = null,
    @property:LLMDescription("Measurement or quantity for the fifth ingredient")
    @SerialName("strMeasure5") val measure5: String? = null,
    @property:LLMDescription("Measurement or quantity for the sixth ingredient")
    @SerialName("strMeasure6") val measure6: String? = null,
    @property:LLMDescription("Measurement or quantity for the seventh ingredient")
    @SerialName("strMeasure7") val measure7: String? = null,
    @property:LLMDescription("Measurement or quantity for the eighth ingredient")
    @SerialName("strMeasure8") val measure8: String? = null,
    @property:LLMDescription("Measurement or quantity for the ninth ingredient")
    @SerialName("strMeasure9") val measure9: String? = null,
    @property:LLMDescription("Measurement or quantity for the tenth ingredient")
    @SerialName("strMeasure10") val measure10: String? = null,
    @property:LLMDescription("Measurement or quantity for the eleventh ingredient")
    @SerialName("strMeasure11") val measure11: String? = null,
    @property:LLMDescription("Measurement or quantity for the twelfth ingredient")
    @SerialName("strMeasure12") val measure12: String? = null,
    @property:LLMDescription("Measurement or quantity for the thirteenth ingredient")
    @SerialName("strMeasure13") val measure13: String? = null,
    @property:LLMDescription("Measurement or quantity for the fourteenth ingredient")
    @SerialName("strMeasure14") val measure14: String? = null,
    @property:LLMDescription("Measurement or quantity for the fifteenth ingredient")
    @SerialName("strMeasure15") val measure15: String? = null,
    @property:LLMDescription("Measurement or quantity for the sixteenth ingredient")
    @SerialName("strMeasure16") val measure16: String? = null,
    @property:LLMDescription("Measurement or quantity for the seventeenth ingredient")
    @SerialName("strMeasure17") val measure17: String? = null,
    @property:LLMDescription("Measurement or quantity for the eighteenth ingredient")
    @SerialName("strMeasure18") val measure18: String? = null,
    @property:LLMDescription("Measurement or quantity for the nineteenth ingredient")
    @SerialName("strMeasure19") val measure19: String? = null,
    @property:LLMDescription("Measurement or quantity for the twentieth ingredient")
    @SerialName("strMeasure20") val measure20: String? = null
) {
    val instructionSteps = instructions
        .split("\r\n", "\n")
        .map { it.trim() }
        .filter { !it.startsWith("step", ignoreCase = true) }
        .filter { it.isNotEmpty() }

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

@LLMDescription("API response wrapper containing a list of full meal details")
@Serializable
data class MealResponse(
    @property:LLMDescription("List of meals with complete recipe details")
    @SerialName("meals")
    val meals: List<Meal>
)

@LLMDescription("Represents a simplified meal summary containing only essential information for display in lists")
@Serializable
@Parcelize
data class MealSummary(
    @property:LLMDescription("Unique identifier for the meal")
    @SerialName("idMeal")
    val id: String,
    @property:LLMDescription("The name of the meal")
    @SerialName("strMeal")
    val name: String,
    @property:LLMDescription("URL to the meal's thumbnail image")
    @SerialName("strMealThumb")
    val thumbnail: String?,
    @property:LLMDescription("YouTube video URL for the recipe tutorial, if available")
    @SerialName("strYoutube")
    val youtube: String? = null,
): Parcelable


@LLMDescription("API response wrapper containing a list of meal summaries")
@Serializable
data class MealSummaryResponse(
    @property:LLMDescription("List of meal summaries, or null if no meals found")
    @SerialName("meals")
    val meals: List<MealSummary>?
)