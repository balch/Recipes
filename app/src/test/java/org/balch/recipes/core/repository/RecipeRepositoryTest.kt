package org.balch.recipes.core.repository

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.balch.recipes.core.models.CategoriesResponse
import org.balch.recipes.core.models.Category
import org.balch.recipes.core.models.Meal
import org.balch.recipes.core.models.MealResponse
import org.balch.recipes.core.models.MealSummary
import org.balch.recipes.core.models.MealSummaryResponse
import org.balch.recipes.core.network.TheMealDbApi
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.wheneverBlocking

class RecipeRepositoryTest {

    private val mockApi = mock<TheMealDbApi>()

    private val repository by lazy { RecipeRepositoryImpl(mockApi) }


    @Test
    fun `getCategories returns list of categories on success`() = runTest {
        val expectedCategories = listOf(
            Category("1", "Beef", "thumb1.jpg", "Beef dishes"),
            Category("2", "Chicken", "thumb2.jpg", "Chicken dishes")
        )
        val response = CategoriesResponse(expectedCategories)
        wheneverBlocking { mockApi.getCategories() } doReturn(Result.success(response))

        val result = repository.getCategories()

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(expectedCategories)
    }

    @Test
    fun `getMealsByCategory returns list of meal summaries on success`() = runTest {
        val category = "Beef"
        val expectedMeals = listOf(
            MealSummary("1", "Beef Steak", "thumb1.jpg"),
            MealSummary("2", "Beef Burger", "thumb2.jpg")
        )
        val response = MealSummaryResponse(expectedMeals)
        wheneverBlocking { mockApi.getMealsByCategory(category) } doReturn(Result.success(response))

        val result = repository.getMealsByCategory(category)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(expectedMeals)
    }

    @Test
    fun `getMealsByCategory returns empty list when meals is null`() = runTest {
        val category = "NonExistent"
        val response = MealSummaryResponse(null)
        wheneverBlocking { mockApi.getMealsByCategory(category) } doReturn(Result.success(response))

        val result = repository.getMealsByCategory(category)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(emptyList<MealSummary>())
    }

    @Test
    fun `getMealById returns meal on success`() = runTest {
        val mealId = "52874"
        val expectedMeal = Meal(
            id = mealId,
            name = "Beef and Mustard Pie",
            category = "Beef",
            area = "British",
            instructions = "Instructions here...",
            thumbnail = "thumb.jpg"
        )
        val response = MealResponse(listOf(expectedMeal))
        wheneverBlocking { mockApi.getMealById(mealId) } doReturn(Result.success(response))

        val result = repository.getMealById(mealId)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(expectedMeal)
    }

    @Test
    fun `getMealById throws when meals is not found`() = runTest {
        val mealId = "nonexistent"
        val response = MealResponse(emptyList())
        wheneverBlocking { mockApi.getMealById(mealId) } doReturn(Result.success(response))

        val result = repository.getMealById(mealId)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
    }
}