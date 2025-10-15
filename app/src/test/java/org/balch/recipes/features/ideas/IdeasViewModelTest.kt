package org.balch.recipes.features.ideas

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.balch.recipes.BrowsableType
import org.balch.recipes.MainCoroutineExtension
import org.balch.recipes.core.coroutines.TestDispatcherProvider
import org.balch.recipes.core.models.Area
import org.balch.recipes.core.models.Category
import org.balch.recipes.core.models.Ingredient
import org.balch.recipes.core.models.Meal
import org.balch.recipes.core.repository.RecipeRepository
import org.balch.recipes.features.CodeRecipeRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.wheneverBlocking

@OptIn(ExperimentalCoroutinesApi::class)
class IdeasViewModelTest {

    /**
     * use StandardTestDispatcher to get around the Conflation with the initial [IdeasUiState.Loading]
     * being emitted before the next state with the results
     */
    private val dispatcherProvider = TestDispatcherProvider(StandardTestDispatcher())

    @JvmField
    @RegisterExtension
    val mainCoroutineExtension = MainCoroutineExtension(dispatcherProvider.testDispatcher)

    private val mealRepository = mock<RecipeRepository>()
    private val codeRecipeRepository = mock<CodeRecipeRepository> {
        onBlocking { getRandomRecipes(3) } doReturn listOf(mock(), mock(), mock())
    }

    private val viewModel by lazy {
        IdeasViewModel(
            mealRepository = mealRepository,
            codeRecipeRepository = codeRecipeRepository,
            savedStateHandle = mock(),
            dispatcherProvider = dispatcherProvider
        )
    }

    private val testCategories = listOf(
        Category(id = "1", name = "Beef", thumbnail = "beef.jpg", description = "Beef dishes"),
        Category(
            id = "2",
            name = "Chicken",
            thumbnail = "chicken.jpg",
            description = "Chicken dishes"
        )
    )

    private val testAreas = listOf(
        Area(name = "Italian"),
        Area(name = "Mexican")
    )

    private val testIngredients = listOf(
        Ingredient(id = "1", name = "Salt", description = "Basic seasoning"),
        Ingredient(id = "2", name = "Pepper", description = "Black pepper")
    )

    private val testMeal = Meal(
        id = "1",
        name = "Test Meal",
        category = "Test Category",
        area = "Test Area",
        instructions = "Test Instructions",
        thumbnail = "test_thumbnail.jpg"
    )

    private fun IdeasUiState.assertValidCategories(categories: List<Category>) {
        assertThat(this).isInstanceOf(IdeasUiState.Categories::class.java)
        val state = this as IdeasUiState.Categories
        assertThat(state.categories).isEqualTo(categories)
    }

    private fun IdeasUiState.assertValidAreas(
        areas: List<Area>,
        thumbnail: String?
    ) {
        assertThat(this).isInstanceOf(IdeasUiState.Areas::class.java)
        val state = this as IdeasUiState.Areas
        assertThat(state.areas).isEqualTo(areas)
        assertThat(state.imageUrl).isEqualTo(thumbnail)
    }

    private fun IdeasUiState.assertValidIngredients(
        ingredients: List<Ingredient>,
        thumbnail: String?
    ) {
        assertThat(this).isInstanceOf(IdeasUiState.Ingredients::class.java)
        val state = this as IdeasUiState.Ingredients
        assertThat(state.ingredients).isEqualTo(ingredients)
        assertThat(state.imageUrl).isEqualTo(thumbnail)
    }

    private fun IdeasUiState.assertErrorState(errorMessage: String) {
        assertThat(this).isInstanceOf(IdeasUiState.Error::class.java)
        val state = this as IdeasUiState.Error
        assertThat(state.message).isEqualTo(errorMessage)
    }

    private fun IdeasUiState.assertLoadingState() {
        assertThat(this).isInstanceOf(IdeasUiState.Loading::class.java)
    }

    @Test
    fun `initial state is Categories when default BrowsableType is Category`() = runTest {
        wheneverBlocking { mealRepository.getCategories() } doReturn (Result.success(testCategories))
        viewModel.uiState.test {
            awaitItem().assertLoadingState()
            awaitItem().assertValidCategories(testCategories)
        }
    }

    @Test
    fun `emits Areas state when browsable type is changed to Area`() = runTest { // Given
        wheneverBlocking { mealRepository.getCategories() } doReturn (Result.success(testCategories))
        wheneverBlocking { mealRepository.getAreas() } doReturn (Result.success(testAreas))
        wheneverBlocking { mealRepository.getRandomMeal() } doReturn (Result.success(testMeal))
        viewModel.uiState.test {
            awaitItem().assertLoadingState()
            awaitItem().assertValidCategories(testCategories)

            viewModel.changeBrowsableType(BrowsableType.Area)
            awaitItem().assertLoadingState()
            awaitItem().assertValidAreas(areas = testAreas, thumbnail = testMeal.thumbnail)
        }
    }

    @Test
    fun `emits Ingredients state when browsable type is changed to Ingredient`() = runTest {
        wheneverBlocking { mealRepository.getCategories() } doReturn (Result.success(testCategories))
        wheneverBlocking { mealRepository.getIngredients() } doReturn (Result.success(testIngredients))
        wheneverBlocking { mealRepository.getRandomMeal() } doReturn (Result.success(testMeal))
        viewModel.uiState.test {
            awaitItem().assertLoadingState()
            awaitItem().assertValidCategories(testCategories)

            viewModel.changeBrowsableType(BrowsableType.Ingredient)
            awaitItem().assertLoadingState()
            awaitItem().assertValidIngredients(testIngredients, testMeal.thumbnail)
        }
    }

    @Test
    fun `emits Error state when categories fetch fails`() = runTest {
        val errorMessage = "Network Error"
        wheneverBlocking { mealRepository.getCategories() } doReturn (Result.failure(Exception(errorMessage)))
        viewModel.uiState.test {
            awaitItem().assertLoadingState()
            awaitItem().assertErrorState(errorMessage)
        }
    }

    @Test
    fun `emits Error state when areas fetch fails`() = runTest {
        wheneverBlocking { mealRepository.getCategories() } doReturn (Result.success(testCategories))
        val errorMessage = "Areas fetch failed"
        wheneverBlocking { mealRepository.getAreas() } doReturn (Result.failure(Exception(errorMessage)))
        wheneverBlocking { mealRepository.getRandomMeal() } doReturn (Result.success(testMeal))
        viewModel.uiState.test {
            awaitItem().assertLoadingState()
            awaitItem().assertValidCategories(testCategories)

            viewModel.changeBrowsableType(BrowsableType.Area)
            awaitItem().assertLoadingState()
            awaitItem().assertErrorState(errorMessage)
        }
    }

    @Test
    fun `emits Error state when ingredients fetch fails`() = runTest {
        wheneverBlocking { mealRepository.getCategories() } doReturn (Result.success(testCategories))
        val errorMessage = "Ingredients fetch failed"
        wheneverBlocking { mealRepository.getIngredients() } doReturn (Result.failure(Exception(errorMessage)))
        wheneverBlocking { mealRepository.getRandomMeal() } doReturn (Result.success(testMeal))
        viewModel.uiState.test {
            awaitItem().assertLoadingState()
            awaitItem().assertValidCategories(testCategories)

            viewModel.changeBrowsableType(BrowsableType.Ingredient)
            awaitItem().assertLoadingState()
            awaitItem().assertErrorState(errorMessage)
        }
    }

    @Test
    fun `retry function triggers data reload`() = runTest {
        wheneverBlocking { mealRepository.getCategories() } doReturn (Result.success(testCategories))
        viewModel.uiState.test {
            awaitItem().assertLoadingState()
            awaitItem().assertValidCategories(testCategories)

            viewModel.retry()
            awaitItem().assertLoadingState()
            awaitItem().assertValidCategories(testCategories)
        }
    }

    @Test
    fun `handles null random meal gracefully for Areas`() = runTest {
        wheneverBlocking { mealRepository.getCategories() } doReturn (Result.success(testCategories))
        wheneverBlocking { mealRepository.getAreas() } doReturn (Result.success(testAreas))
        wheneverBlocking { mealRepository.getRandomMeal() } doReturn (Result.failure(Exception()))
        viewModel.uiState.test {
            awaitItem().assertLoadingState()
            awaitItem().assertValidCategories(testCategories)

            viewModel.changeBrowsableType(BrowsableType.Area)
            awaitItem().assertLoadingState()
            awaitItem().assertValidAreas(areas = testAreas, thumbnail = null)
        }
    }
}