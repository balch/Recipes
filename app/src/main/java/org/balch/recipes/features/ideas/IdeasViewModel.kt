package org.balch.recipes.features.ideas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diamondedge.logging.logging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import org.balch.recipes.BrowsableType
import org.balch.recipes.core.coroutines.DispatcherProvider
import org.balch.recipes.core.models.Area
import org.balch.recipes.core.models.Category
import org.balch.recipes.core.models.CodeRecipe
import org.balch.recipes.core.models.Ingredient
import org.balch.recipes.core.repository.RecipeRepository
import org.balch.recipes.features.CodeRecipeRepository
import javax.inject.Inject

/**
 * ViewModel responsible for managing and providing UI state for the "Ideas" screen,
 * including categories, areas, and ingredients to browse recipes.
 *
 * This class interacts with the `RecipeRepository` to fetch data and uses a combination of
 * flows and coroutines to update the UI state dynamically based on user interactions.
 *
 * @constructor Injects the required dependencies: `repository` for data access,
 * `codeRecipes` for code recipe examples, and `dispatcherProvider` for coroutine context management.
 */
@HiltViewModel
class IdeasViewModel @Inject constructor(
    private val mealRepository: RecipeRepository,
    private val codeRecipeRepository: CodeRecipeRepository,
    private val savedStateHandle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val logger = logging("IdeasViewModel")

    private val loadIntentFlow = MutableStateFlow(true)

    private val browsableTypeFlow = MutableStateFlow(
        savedStateHandle[KEY_BROWSABLE_TYPE] ?: BrowsableType.Category
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<IdeasUiState> =
        combine(
            loadIntentFlow.filter { it },
            browsableTypeFlow,
            ::Pair
        ).transformLatest { (_, browsableType) ->
            // Save the browsableType to the saved state handle
            savedStateHandle[KEY_BROWSABLE_TYPE] = browsableType

            loadIntentFlow.value = false
            emit(IdeasUiState.Loading)
            emit(deriveState(browsableType))
        }
        .onEach { logger.d { "UIState: ${it.javaClass.simpleName} - ${it.imageUrl?.let { "imageUrl: $it" } ?: "no imageUrl"}"} }
        .flowOn(dispatcherProvider.default)
        .stateIn(
            scope = viewModelScope,
            // use lazy so we don't time out when we go to a detail screen.
            started = SharingStarted.Lazily,
            initialValue = IdeasUiState.Loading
        )

    private suspend fun deriveState(browsableType: BrowsableType): IdeasUiState =
        try {
            // Randomly select 1 or 3 CodeRecipes to sprinkle into the grid
            val randomCodeRecipes = codeRecipeRepository.getRandomRecipes(3)
            
            when (browsableType) {
                BrowsableType.Category -> {
                    val categories = mealRepository.getCategories()
                    IdeasUiState.Categories(
                        categories = categories.getOrThrow(),
                        codeRecipes = randomCodeRecipes
                    )
                }
                BrowsableType.Area -> {
                    coroutineScope {
                        val areasJob = async { mealRepository.getAreas() }
                        val randomMealJob = async { mealRepository.getRandomMeal() }

                        IdeasUiState.Areas(
                            areas = areasJob.await().getOrThrow(),
                            imageUrl = randomMealJob.await().getOrNull()?.thumbnail,
                            codeRecipes = randomCodeRecipes
                        )
                    }
                }
                BrowsableType.Ingredient -> {
                    coroutineScope {
                        val ingredientsJob = async { mealRepository.getIngredients() }
                        val randomMealJob = async { mealRepository.getRandomMeal() }

                        IdeasUiState.Ingredients(
                            ingredients = ingredientsJob.await().getOrThrow(),
                            imageUrl = randomMealJob.await().getOrNull()?.thumbnail,
                            codeRecipes = randomCodeRecipes
                        )
                    }
                }
                BrowsableType.CodeRecipe -> {
                    IdeasUiState.CodeRecipes(
                        imageUrl = null,
                        codeRecipes = codeRecipeRepository.sortedRecipes(),
                    )
                }
            }
        } catch (e: Exception) {
            logger.e(e) { "Error loading ideas" }
            IdeasUiState.Error(e.message ?: "Unknown error occurred")
        }

    fun retry() {
        loadIntentFlow.value = true
    }

    fun changeBrowsableType(browsableType: BrowsableType) {
        browsableTypeFlow.value = browsableType
    }

    companion object {
        private const val KEY_BROWSABLE_TYPE = "browsableType"
    }
}

sealed interface IdeasUiState {
    val imageUrl: String?
        get() = null
    val codeRecipes: List<CodeRecipe>
        get() = emptyList()

    val isTopLevelState: Boolean
        get() = true

    data object Loading : IdeasUiState
    data class Error(val message: String) : IdeasUiState
    data class Categories(
        val categories: List<Category>,
        override val codeRecipes: List<CodeRecipe>
    ) : IdeasUiState
    data class Areas(
        val areas: List<Area>,
        override val imageUrl: String?,
        override val codeRecipes: List<CodeRecipe>,
        override val isTopLevelState: Boolean = false,
    ) : IdeasUiState
    data class Ingredients(
        val ingredients: List<Ingredient>,
        override val imageUrl: String?,
        override val codeRecipes: List<CodeRecipe>,
        override val isTopLevelState: Boolean = false,
    ) : IdeasUiState
    data class CodeRecipes(
        override val imageUrl: String?,
        override val codeRecipes: List<CodeRecipe>,
        override val isTopLevelState: Boolean = false,
    ) : IdeasUiState
}