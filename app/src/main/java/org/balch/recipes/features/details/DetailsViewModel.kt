package org.balch.recipes.features.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diamondedge.logging.logging
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import org.balch.recipes.core.coroutines.DispatcherProvider
import org.balch.recipes.core.models.CodeRecipe
import org.balch.recipes.core.models.DetailType
import org.balch.recipes.core.models.Meal
import org.balch.recipes.core.models.MealSummary
import org.balch.recipes.core.random.RandomProvider
import org.balch.recipes.core.repository.RecipeRepository
import org.balch.recipes.features.CodeRecipeRepository
import kotlin.time.Duration.Companion.seconds


/**
 * ViewModel responsible for managing the UI state and data flow for displaying detail information.
 *
 * This ViewModel is instantiated with a specific type of detail via the `DetailType` sealed interface,
 * which determines the kind of data it handles. It interacts with the `RecipeRepository` to fetch and process
 * data based on the given detail type and provides the resultant state through a `StateFlow<UiState>`.
 *
 * The `uiState` flow emits different states of the UI:
 * - `UiState.Loading`: When the data is being fetched or processed.
 * - `UiState.Show`: When the detail of a meal is successfully retrieved.
 * - `UiState.Error`: When an error occurs during data fetching or processing.
 *
 * @property detailType Determines the type of detail being displayed, such as meal content or lookup by ID.
 * @property mealRepository Provides access to data sources for fetching meals and related details.
 * @property uiState Represents the current UI state as a `StateFlow` that can emit loading, success, or error states.
 */
@HiltViewModel(assistedFactory = DetailsViewModel.Factory::class)
class DetailsViewModel @AssistedInject constructor(
    @Assisted val detailType: DetailType,
    private val mealRepository: RecipeRepository,
    private val codeRecipeRepository: CodeRecipeRepository,
    private val randomProvider: RandomProvider,
    dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val logger = logging("DetailsViewModel")

    private val initialUiState: UiState =
        when (detailType) {
            is DetailType.CodeRecipeContent -> {
                UiState.ShowCodeRecipe(detailType.codeRecipe)
            }
            is DetailType.MealContent -> {
                UiState.ShowMeal(detailType.meal)
            }
            is DetailType.MealLookup -> {
                UiState.Loading(detailType.mealSummary)
            }
            else -> {
                UiState.Loading()
            }

        }.also {
            logger.d { "Initial UI state: ${it.javaClass.simpleName}" }
        }

    val uiState: StateFlow<UiState> =
        flow {
            when (detailType) {
                is DetailType.MealContent -> {
                    emit(initialUiState)
                }

                is DetailType.MealLookup -> {
                    mealRepository.getMealById(detailType.mealSummary.id)
                        .onSuccess { emit(UiState.ShowMeal(it)) }
                        .onFailure { emit(UiState.Error(it.message ?: "Unknown Error")) }
                }

                is DetailType.RandomRecipe -> {
                    if (randomProvider.nextFloat() <= RANDOM_MEAL_PERCENTAGE) {
                        mealRepository.getRandomMeal()
                            .onSuccess { emit(UiState.ShowMeal(it)) }
                            .onFailure { emit(UiState.Error(it.message ?: "Unknown Error")) }
                    } else {
                        emit(UiState.ShowCodeRecipe(codeRecipeRepository.getRandomRecipes(1)[0]))
                    }
                }

                is DetailType.CodeRecipeContent -> {
                    emit(UiState.ShowCodeRecipe(detailType.codeRecipe))
                }
            }
        }
        .catch { e ->
            logger.e(e) { "Error loading details" }
            emit(UiState.Error(e.message ?: "Unknown error occurred"))
        }
        .onEach { logger.d { "UIState: ${it.javaClass.simpleName} - ${it.let { if (it is UiState.ShowMeal) "meal: ${it.meal.name}" else ""}}"} }
        .flowOn(dispatcherProvider.default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
            initialValue = initialUiState
        )

    @AssistedFactory
    interface Factory {
        fun create(detailType: DetailType): DetailsViewModel
    }

    companion object {
        internal const val RANDOM_MEAL_PERCENTAGE = .8f
    }
}

sealed interface UiState {
    data class Loading(val mealSummary: MealSummary? = null) : UiState
    data class Error(val message: String) : UiState
    data class ShowMeal(val meal: Meal) : UiState
    data class ShowCodeRecipe(val codeRecipe: CodeRecipe) : UiState
}