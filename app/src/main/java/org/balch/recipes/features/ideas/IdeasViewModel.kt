package org.balch.recipes.features.ideas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diamondedge.logging.logging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
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
import org.balch.recipes.features.CodeRecipes
import javax.inject.Inject
import kotlin.random.Random

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
    private val repository: RecipeRepository,
    private val codeRecipes: CodeRecipes,
    dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val logger = logging("IdeasViewModel")

    private val loadIntentFlow = MutableStateFlow(true)

    private val browsableTypeFlow = MutableStateFlow(BrowsableType.Category)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<IdeasUiState> =
        combine(
            loadIntentFlow.filter { it },
            browsableTypeFlow,
            ::Pair
        ).transformLatest { (_, browsableType) ->
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
            val randomCodeRecipes = codeRecipes.getRandomRecipes(3)
            
            when (browsableType) {
                BrowsableType.Category -> {
                    val categories = repository.getCategories()
                    IdeasUiState.Categories(
                        categories = categories.getOrThrow(),
                        codeRecipes = randomCodeRecipes
                    )
                }
                BrowsableType.Area -> {
                    val areasJob = viewModelScope.async { repository.getAreas() }
                    val randomMealJob = viewModelScope.async { repository.getRandomMeal() }

                    IdeasUiState.Areas(
                        areas = areasJob.await().getOrThrow(),
                        imageUrl = randomMealJob.await().getOrNull()?.thumbnail,
                        codeRecipes = randomCodeRecipes
                    )
                }
                BrowsableType.Ingredient -> {
                    val ingredientsJob = viewModelScope.async { repository.getIngredients() }
                    val randomMealJob = viewModelScope.async { repository.getRandomMeal() }

                    IdeasUiState.Ingredients(
                        ingredients = ingredientsJob.await().getOrThrow(),
                        imageUrl = randomMealJob.await().getOrNull()?.thumbnail,
                        codeRecipes = randomCodeRecipes
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
}

sealed interface IdeasUiState {
    val imageUrl: String?
        get() = null
    val codeRecipes: List<CodeRecipe>
        get() = emptyList()

    data object Loading : IdeasUiState
    data class Error(val message: String) : IdeasUiState
    data class Categories(
        val categories: List<Category>,
        override val codeRecipes: List<CodeRecipe>
    ) : IdeasUiState
    data class Areas(
        val areas: List<Area>,
        override val imageUrl: String?,
        override val codeRecipes: List<CodeRecipe>
    ) : IdeasUiState
    data class Ingredients(
        val ingredients: List<Ingredient>,
        override val imageUrl: String?,
        override val codeRecipes: List<CodeRecipe>
    ) : IdeasUiState
}