package org.balch.recipes.features.details

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.balch.recipes.MainCoroutineExtension
import org.balch.recipes.core.coroutines.TestDispatcherProvider
import org.balch.recipes.core.models.DetailType
import org.balch.recipes.core.models.Meal
import org.balch.recipes.core.models.MealSummary
import org.balch.recipes.core.random.RandomProvider
import org.balch.recipes.core.repository.RecipeRepository
import org.balch.recipes.features.CodeRecipeRepository
import org.balch.recipes.features.details.DetailsViewModel.Companion.RANDOM_MEAL_PERCENTAGE
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.wheneverBlocking

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {

    private val dispatcherProvider: TestDispatcherProvider = TestDispatcherProvider()

    @JvmField
    @RegisterExtension
    val mainCoroutineExtension = MainCoroutineExtension(dispatcherProvider.testDispatcher)

    private val mealRepository = mock<RecipeRepository>()
    private val codeRecipeRepository = mock<CodeRecipeRepository>()

    private var nextRandomFloat = RANDOM_MEAL_PERCENTAGE
    private val randomProvider = mock<RandomProvider> {
        on { nextFloat() } doAnswer { nextRandomFloat }
    }

    private val testMeal = Meal(
        id = "1",
        name = "Test Meal",
        category = "Test Category",
        area = "Test Area",
        instructions = "Test Instructions",
        thumbnail = "test_thumbnail.jpg"
    )

    private fun getViewModel(detailType: DetailType): DetailsViewModel =
        DetailsViewModel(
            detailType = detailType,
            mealRepository = mealRepository,
            codeRecipeRepository = codeRecipeRepository,
            randomProvider = randomProvider,
            dispatcherProvider = dispatcherProvider
        )

    private fun UiState.assertValidShowState(meal: Meal) {
        assertThat(this).isInstanceOf(UiState.ShowMeal::class.java)
        val state = this as UiState.ShowMeal
        assertThat(state.meal).isEqualTo(meal)
    }

    private fun UiState.assertErrorState(message: String) {
        assertThat(this).isInstanceOf(UiState.Error::class.java)
        val state = this as UiState.Error
        assertThat(state.message).isEqualTo(message)
    }

    @Test
    fun `initial state is Show when DetailType is Content`() = runTest {
        val detailType = DetailType.MealContent(testMeal)
        val viewModel = getViewModel(detailType)
        viewModel.uiState.test {
            awaitItem().assertValidShowState(testMeal)
        }
    }

    @Test
    fun `initial state is Loading when DetailType is Lookup and then Shows meal on success`() = runTest {
        val detailType = DetailType.MealLookup(MealSummary("1", "Test Meal", "test_thumbnail.jpg"))
        wheneverBlocking { mealRepository.getMealById("1") } doReturn(Result.success(testMeal))
        val viewModel = getViewModel(detailType)
        viewModel.uiState.test {
            awaitItem().assertValidShowState(testMeal)
        }
    }

    @Test
    fun `emits Error state when lookup fails`() = runTest {
        val detailType = DetailType.MealLookup(MealSummary("1", "Test Meal", "test_thumbnail.jpg"))
        val errorMessage = "Network Error"
        wheneverBlocking { mealRepository.getMealById("1") } doReturn(Result.failure(Exception(errorMessage)))
        val viewModel = getViewModel(detailType)
        viewModel.uiState.test {
            awaitItem().assertErrorState(errorMessage)
        }
    }

    @Test
    fun `emits Error state with default message when exception message is null`() = runTest {
        val detailType = DetailType.MealLookup(MealSummary("1", "Test Meal", "test_thumbnail.jpg"))
        val exceptionWithNullMessage = Exception(null as String?)
        wheneverBlocking { mealRepository.getMealById("1") } doReturn(Result.failure(exceptionWithNullMessage))
        val viewModel = getViewModel(detailType)
        viewModel.uiState.test {
            awaitItem().assertErrorState("Unknown Error")
        }
    }

    @Test
    fun `initial state is Loading when DetailType is Random and then Shows meal on success`() = runTest {
        val detailType = DetailType.RandomRecipe
        wheneverBlocking { mealRepository.getRandomMeal() } doReturn(Result.success(testMeal))
        val viewModel = getViewModel(detailType)
        viewModel.uiState.test {
            awaitItem().assertValidShowState(testMeal)
        }
    }

    @Test
    fun `getRandomMeal emits Show state with random meal`() = runTest {
        val detailType = DetailType.RandomRecipe
        wheneverBlocking { mealRepository.getRandomMeal() } doReturn(Result.success(testMeal))
        val viewModel = getViewModel(detailType)

        viewModel.uiState.test {
            awaitItem().assertValidShowState(testMeal)
        }
    }
    @Test
    fun `emits Error state when random meal fails`() = runTest {
        val detailType = DetailType.RandomRecipe
        val errorMessage = "Random meal failed"
        wheneverBlocking { mealRepository.getRandomMeal() } doReturn(Result.failure(Exception(errorMessage)))
        val viewModel = getViewModel(detailType)
        viewModel.uiState.test {
            awaitItem().assertErrorState(errorMessage)
        }
    }

    @Test
    fun `emits Error state with default message when random meal exception message is null`() = runTest {
        val detailType = DetailType.RandomRecipe
        val exceptionWithNullMessage = Exception(null as String?)
        wheneverBlocking { mealRepository.getRandomMeal() } doReturn(Result.failure(exceptionWithNullMessage))
        val viewModel = getViewModel(detailType)
        viewModel.uiState.test {
            awaitItem().assertErrorState("Unknown Error")
        }
    }
}