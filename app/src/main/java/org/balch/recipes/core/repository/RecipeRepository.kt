package org.balch.recipes.core.repository

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import org.balch.recipes.core.models.Area
import org.balch.recipes.core.models.Category
import org.balch.recipes.core.models.Ingredient
import org.balch.recipes.core.models.Meal
import org.balch.recipes.core.models.MealSummary
import org.balch.recipes.core.network.TheMealDbApi
import org.balch.recipes.di.AppScope
import javax.inject.Inject

/**
 * Repository interface for accessing recipes data.
 */
interface RecipeRepository {
    suspend fun getAreas(): Result<List<Area>>
    suspend fun getIngredients(): Result<List<Ingredient>>
    suspend fun getCategories(): Result<List<Category>>
    suspend fun getMealsByCategory(category: String): Result<List<MealSummary>>
    suspend fun getMealById(id: String): Result<Meal>
    suspend fun searchMeals(query: String): Result<List<Meal>>
    suspend fun getRandomMeal(): Result<Meal>
    suspend fun getMealsByArea(area: String): Result<List<MealSummary>>
    suspend fun getMealsByIngredient(ingredient: String): Result<List<MealSummary>>
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class RecipeRepositoryImpl @Inject constructor(
    private val api: TheMealDbApi
) : RecipeRepository {
    
    override suspend fun getAreas(): Result<List<Area>> {
        return api.getAreas().map { response ->
            response.meals
        }
    }
    
    override suspend fun getIngredients(): Result<List<Ingredient>> {
        return api.getIngredients().map { response ->
            response.meals
        }
    }
    
    override suspend fun getCategories(): Result<List<Category>> {
        return api.getCategories().map { response ->
            response.categories
        }
    }
    
    override suspend fun getMealsByCategory(category: String): Result<List<MealSummary>> {
        return api.getMealsByCategory(category).map { response ->
            response.meals ?: emptyList()
        }
    }
    
    override suspend fun getMealById(id: String): Result<Meal> {
        return try {
            api.getMealById(id).map { response ->
                response.meals.firstOrNull()
                    ?: throw IllegalArgumentException("Meal not found")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun searchMeals(query: String): Result<List<Meal>> {
        return api.searchMeals(query).map { response ->
            response.meals
        }
    }
    
    override suspend fun getRandomMeal(): Result<Meal> {
        return try {
            api.getRandomMeal().map { response ->
                response.meals.firstOrNull()
                    ?: throw IllegalArgumentException("Random Meal not found")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getMealsByArea(area: String): Result<List<MealSummary>> {
        return api.getMealsByArea(area).map { response ->
            response.meals ?: emptyList()
        }
    }
    
    override suspend fun getMealsByIngredient(ingredient: String): Result<List<MealSummary>> {
        return api.getMealsByIngredient(ingredient).map { response ->
            response.meals ?: emptyList()
        }
    }
}