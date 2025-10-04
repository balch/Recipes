package org.balch.recipes.features.search

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.balch.recipes.MainCoroutineExtension
import org.balch.recipes.core.coroutines.TestDispatcherProvider
import org.balch.recipes.core.models.Meal
import org.balch.recipes.core.models.MealSummary
import org.balch.recipes.core.models.SearchType
import org.balch.recipes.core.repository.RecipeRepository
import org.balch.recipes.features.CodeRecipeRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.wheneverBlocking
import org.mockito.stubbing.OngoingStubbing

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    /**
     * use StandardTestDispatcher to get around the Conflation with the initial Show state having
     * isFetching=`true` before returning the next state with the results
     *
     * Use [TestScope.whenBlocking] to automatically advance until idle
     */
    private val dispatcherProvider = TestDispatcherProvider(StandardTestDispatcher())

    @JvmField
    @RegisterExtension
    val mainCoroutineExtension = MainCoroutineExtension(dispatcherProvider.testDispatcher)

    /**
     * Executes a suspending method call in a test scope while advancing the test dispatcher to allow
     * pending coroutine operations to complete.
     *
     * This function is designed to facilitate the testing of suspending methods by stubbing
     * their behavior using `OngoingStubbing`.
     *
     * @param methodCall The suspending lambda representing the method call to be executed in the
     * test coroutine scope.
     * @return An instance of [OngoingStubbing] for the result of the executed `methodCall`.
     */
    private fun <T> TestScope.whenBlocking(methodCall: suspend CoroutineScope.() -> T): OngoingStubbing<T> =
        wheneverBlocking {
            advanceUntilIdle()
            methodCall().also {
                advanceUntilIdle()
            }
        }

    private val repository = mock<RecipeRepository>()

    private val codeRecipeRepository = mock<CodeRecipeRepository> {
        onBlocking { searchRecipes(any()) } doReturn(emptyList())
    }

    private val testMeal = Meal(
        id = "1",
        name = "Test Meal",
        category = "Test Category",
        area = "Test Area",
        instructions = "Test Instructions",
        thumbnail = "test_thumbnail.jpg"
    )

    private val testMealDescriptors = listOf(
        MealSummary(id = "1", name = "Test Meal 1", thumbnail = "thumb1.jpg"),
        MealSummary(id = "2", name = "Test Meal 2", thumbnail = "thumb2.jpg")
    )

    private val testMeals = listOf(
        testMeal,
        Meal(id = "2", name = "Test Meal 2", category = "Test Category 2", area = "Test Area 2", 
             instructions = "Test Instructions 2", thumbnail = "thumb2.jpg")
    )

    private fun SearchUiState.assertValidShowState(
        searchType: SearchType,
        items: List<ItemType> = emptyList(),
        isFetching: Boolean = true,
    ) {
        assertThat(this).isInstanceOf(SearchUiState.Show::class.java)
        val state = this as SearchUiState.Show
        assertThat(state.searchType).isEqualTo(searchType)
        assertThat(state.isFetching).isEqualTo(isFetching)
        assertThat(state.items).isEqualTo(items)
    }

    private fun SearchUiState.assertWelcomeState() {
        assertThat(this).isInstanceOf(SearchUiState.Welcome::class.java)
    }

    private fun SearchUiState.assertLoadingState(
        searchTerm: String = "",
        showSearchBar: Boolean = false
    ) {
        assertThat(this).isInstanceOf(SearchUiState.Loading::class.java)
        val state = this as SearchUiState.Loading
        assertThat(state.searchText).isEqualTo(searchTerm)
        assertThat(state.showSearchBar).isEqualTo(showSearchBar)
    }

    private fun SearchUiState.assertErrorState(message: String) {
        assertThat(this).isInstanceOf(SearchUiState.Error::class.java)
        val state = this as SearchUiState.Error
        assertThat(state.message).isEqualTo(message)
    }

    private fun getViewModel(searchType: SearchType) =
        SearchViewModel(
            searchType,
            repository,
            codeRecipeRepository,
            dispatcherProvider,
        )

    @Test
    fun `initial state is Welcome when SearchType is Search with blank text`() = runTest {
        val searchType = SearchType.Search("")
        val viewModel = getViewModel(searchType)

        viewModel.uiState.test {
            awaitItem().assertWelcomeState()
        }
    }

    @Test
    fun `initial state is Show when SearchType is Area`() = runTest {
        val searchType = SearchType.Area("Italian")
        whenBlocking { repository.getMealsByArea("Italian") } doReturn(Result.success(testMealDescriptors))

        val viewModel = getViewModel(searchType)

        viewModel.uiState.test {
            awaitItem().assertValidShowState(searchType)
            awaitItem().assertValidShowState(
                searchType = searchType,
                items = testMealDescriptors.map { it.toItemType() },
                isFetching = false
            )
        }
    }

    @Test
    fun `initial state is Show when SearchType is Category`() = runTest {
        val searchType = SearchType.Category("Beef")
        whenBlocking { repository.getMealsByCategory("Beef") } doReturn(Result.success(testMealDescriptors))
        val viewModel = getViewModel(searchType)

        viewModel.uiState.test {
            awaitItem().assertValidShowState(searchType)
            awaitItem().assertValidShowState(
                searchType = searchType,
                items = testMealDescriptors.map { it.toItemType() },
                isFetching = false
            )
        }
    }

    @Test
    fun `initial state is Show when SearchType is Ingredient`() = runTest {
        val searchType = SearchType.Ingredient("Salt")
        whenBlocking { repository.getMealsByIngredient("Salt") } doReturn(Result.success(testMealDescriptors))
        val viewModel = getViewModel(searchType)

        viewModel.uiState.test {
            awaitItem().assertValidShowState(searchType)
            awaitItem().assertValidShowState(
                searchType = searchType,
                items = testMealDescriptors.map { it.toItemType() },
                isFetching = false
            )
        }
    }

    @Test
    fun `updateSearchQuery emits Show state with search results`() = runTest {
        val searchType = SearchType.Search("")
        val searchTerm = "pasta"
        whenBlocking { repository.searchMeals(searchTerm) } doReturn(Result.success(testMeals))
        val viewModel = getViewModel(searchType)

        viewModel.uiState.test {
            awaitItem().assertWelcomeState()
            viewModel.updateSearchQuery(searchTerm)
            awaitItem().assertLoadingState(searchTerm, true)
            awaitItem().assertValidShowState(
                searchType = SearchType.Search(searchTerm),
                items = testMeals.map { it.toMealSummary().toItemType() },
                isFetching = false
            )
        }
    }

    @Test
    fun `clearSearch emits Welcome state`() = runTest {
        val searchType = SearchType.Search("pasta")
        whenBlocking { repository.searchMeals("pasta") } doReturn(Result.success(testMeals))
        val viewModel = getViewModel(searchType)

        viewModel.uiState.test {
            awaitItem().assertValidShowState(searchType)
            awaitItem().assertValidShowState(
                searchType = searchType,
                items = testMeals.map { it.toMealSummary().toItemType() },
                isFetching = false
            )

            viewModel.clearSearch()
            awaitItem().assertWelcomeState()
        }
    }


    @Test
    fun `emits Error state when area search fails`() = runTest {
        val searchType = SearchType.Area("NonExistent")
        val errorMessage = "Area not found"
        whenBlocking { repository.getMealsByArea("NonExistent") } doReturn(Result.failure(Exception(errorMessage)))
        val viewModel = getViewModel(searchType)

        viewModel.uiState.test {
            awaitItem().assertValidShowState(searchType)
            awaitItem().assertErrorState(errorMessage)
        }
    }

    @Test
    fun `emits Error state when category search fails`() = runTest {
        val searchType = SearchType.Category("NonExistent")
        val errorMessage = "Category not found"
        whenBlocking { repository.getMealsByCategory("NonExistent") } doReturn(Result.failure(Exception(errorMessage)))
        val viewModel = getViewModel(searchType)

        viewModel.uiState.test {
            awaitItem().assertValidShowState(searchType)
            awaitItem().assertErrorState(errorMessage)
        }
    }

    @Test
    fun `emits Error state when ingredient search fails`() = runTest {
        val searchType = SearchType.Ingredient("NonExistent")
        val errorMessage = "Ingredient not found"
        whenBlocking { repository.getMealsByIngredient("NonExistent") } doReturn(Result.failure(Exception(errorMessage)))
        val viewModel = getViewModel(searchType)

        viewModel.uiState.test {
            awaitItem().assertValidShowState(searchType)
            awaitItem().assertErrorState(errorMessage)
        }
    }


    @Test
    fun `general search returns empty list when no results`() = runTest {
        val searchType = SearchType.Search("")
        val searchTerm = "nonexistent"
        whenBlocking { repository.searchMeals(searchTerm) } doReturn(Result.success(emptyList()))
        val viewModel = getViewModel(searchType)

        viewModel.uiState.test {
            awaitItem().assertWelcomeState()

            viewModel.updateSearchQuery(searchTerm)
            awaitItem().assertLoadingState(searchTerm, true)

            awaitItem().assertValidShowState(SearchType.Search(searchTerm), isFetching = false)
        }
    }
}