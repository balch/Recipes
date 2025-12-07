package org.balch.recipes.core.network

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import org.balch.recipes.core.models.AreasResponse
import org.balch.recipes.core.models.CategoriesResponse
import org.balch.recipes.core.models.IngredientsResponse
import org.balch.recipes.core.models.MealResponse
import org.balch.recipes.core.models.MealSummaryResponse

/**
 * API service interface for TheMealDb API.
 */
@SingleIn(AppScope::class)
class TheMealDbApi @Inject constructor(
    private val apiService: ApiService
) {
    companion object {
        private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1"
        
        // API Endpoints
        private const val CATEGORIES = "$BASE_URL/categories.php"
        private const val MEALS_FILTER = "$BASE_URL/filter.php"

        private const val MEAL_BY_ID = "$BASE_URL/lookup.php"
        private const val SEARCH_MEALS = "$BASE_URL/search.php"
        private const val RANDOM_MEAL = "$BASE_URL/random.php"
        private const val LIST_AREAS = "$BASE_URL/list.php"
        private const val LIST_INGREDIENTS = "$BASE_URL/list.php"
    }
    
    suspend fun getAreas(): Result<AreasResponse> {
        return apiService.get(
            url = LIST_AREAS,
            parameters = mapOf("a" to "list")
        )
    }
    
    suspend fun getIngredients(): Result<IngredientsResponse> {
        return apiService.get(
            url = LIST_INGREDIENTS,
            parameters = mapOf("i" to "list")
        )
    }
    
    suspend fun getCategories(): Result<CategoriesResponse> {
        return apiService.get(CATEGORIES)
    }
    
    suspend fun getMealsByCategory(category: String): Result<MealSummaryResponse> {
        return apiService.get(
            url = MEALS_FILTER,
            parameters = mapOf("c" to category)
        )
    }
    
    suspend fun getMealById(id: String): Result<MealResponse> {
        return apiService.get(
            url = MEAL_BY_ID,
            parameters = mapOf("i" to id)
        )
    }
    
    suspend fun searchMeals(query: String): Result<MealResponse> {
        return apiService.get(
            url = SEARCH_MEALS,
            parameters = mapOf("s" to query)
        )
    }
    
    suspend fun getRandomMeal(): Result<MealResponse> {
        return apiService.get(RANDOM_MEAL)
    }
    
    suspend fun getMealsByArea(area: String): Result<MealSummaryResponse> {
        return apiService.get(
            url = MEALS_FILTER,
            parameters = mapOf("a" to area)
        )
    }
    
    suspend fun getMealsByIngredient(ingredient: String): Result<MealSummaryResponse> {
        return apiService.get(
            url = MEALS_FILTER,
            parameters = mapOf("i" to ingredient)
        )
    }
}