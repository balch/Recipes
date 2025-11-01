package org.balch.recipes.features.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diamondedge.logging.logging
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import org.balch.recipes.core.coroutines.DispatcherProvider
import org.balch.recipes.core.models.CodeRecipe
import org.balch.recipes.core.models.MealSummary
import org.balch.recipes.core.models.SearchType
import org.balch.recipes.core.repository.RecipeRepository
import org.balch.recipes.features.CodeRecipeRepository
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds


/**
 * ViewModel for managing search-related operations and UI states in the recipe search feature.
 *
 * This ViewModel handles the following functionalities:
 *
 * - Managing and processing user search input to perform queries by various criteria such as area, category,
 *   ingredient, or direct search terms.
 * - Providing a flow of results, ensuring that the UI reflects the loading and success states of queries.
 * - Supporting real-time updates with debouncing to avoid excessive query execution.
 * - Fetching random meal data as part of the search interactions.
 *
 * @param searchType Determines the type of search operation (e.g., Area, Category, Ingredient, or Search term).
 * @param repository An interface for accessing and interacting with recipes data.
 * @param dispatcherProvider Supplies coroutine dispatchers for running concurrent tasks.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel(assistedFactory = SearchViewModel.Factory::class)
class SearchViewModel @AssistedInject constructor(
    @Assisted val searchType: SearchType,
    private val repository: RecipeRepository,
    private val codeRecipeRepository: CodeRecipeRepository,
    dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private val searchQueryIntent =
        MutableSharedFlow<String>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val logger = logging("SearchViewModel")

    private val initialUiState: SearchUiState =
        if (searchType is SearchType.Search && searchType.searchText.isBlank()) {
            SearchUiState.Welcome
        } else {
            SearchUiState.Show(
                searchType = searchType,
                items = emptyList(),
                searchTerm = searchType.searchText,
                isFetching = true,
                youtubeVideos = emptyList()
            )
        }.also { logger.d { "Initial UIState: $it" } }

    private val searchFlow: Flow<SearchUiState> =
        merge(
            searchQueryIntent
                .debounce(300.milliseconds)
                .mapLatest { query ->
                    SearchType.Search(query.trim())
                },
            initialSearchFlow()
        )
            .transformLatest { searchType ->
                if (searchType.searchText.isNotEmpty()) {
                    emit(
                        SearchUiState.Loading(
                            searchTerm = searchType.searchText,
                            showSearchBar = searchType is SearchType.Search
                        )
                    )
                }
                when (searchType) {
                    is SearchType.Area -> {
                        emit(searchType.searchWith {
                            repository.getMealsByArea(searchType.searchText)
                        })
                    }

                    is SearchType.Category -> {
                        emit(searchType.searchWith {
                            repository.getMealsByCategory(searchType.searchText)
                        })
                    }

                    is SearchType.Ingredient -> {
                        emit(searchType.searchWith {
                            repository.getMealsByIngredient(searchType.searchText)
                        })
                    }

                    is SearchType.Search -> {
                        if (searchType.searchText.isBlank()) {
                            emit(SearchUiState.Welcome)
                        } else {
                            emit(searchMealsAndCode(searchType.searchText))
                        }
                    }
                }
            }.scan(initialUiState) { previousState, newState ->
                logger.v { "Previous State: $previousState, New State: $newState" }
                if (newState is SearchUiState.Loading && previousState is SearchUiState.Show) {
                    SearchUiState.Show(
                        searchType = previousState.searchType,
                        items = previousState.items,
                        searchTerm = newState.searchTerm,
                        isFetching = true,
                        youtubeVideos = previousState.youtubeVideos
                    )
                } else {
                    newState
                }
            }.distinctUntilChanged()

    private fun initialSearchFlow(): Flow<SearchType> =
        flow {
            if (initialUiState is SearchUiState.Show) {
                emit(initialUiState.searchType)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SearchUiState> =
        searchFlow
            .catch { e ->
                logger.e(e) { "Error loading in search" }
                emit(SearchUiState.Error(e.message ?: "Search failed", searchType.displayText))
            }
            .onEach { logger.d { "SearchUiState: ${it.javaClass.simpleName} - ${it.searchText}" } }
            .flowOn(dispatcherProvider.default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = initialUiState
            )


    private suspend fun searchMealsAndCode(query: String): SearchUiState {
        val meals = repository.searchMeals(query)
            .getOrElse { emptyList() }
        val items = coroutineScope {
            listOf(
                async {
                    meals.map { it.toMealSummary().toItemType() }
                },
                async {
                    codeRecipeRepository.searchRecipes(query)
                        .map { it.toItemType() }
                }
            ).awaitAll()
                .flatten()
                .sortedBy {
                    when (it) {
                        is ItemType.MealType -> it.meal.name
                        is ItemType.CodeRecipeType -> it.codeRecipe.title
                    }
                }
        }

        return SearchUiState.Show(
            searchType = SearchType.Search(query),
            items = items,
            searchTerm = query,
            isFetching = false,
            youtubeVideos = meals.mapNotNull { it.youtube }
        )
    }

    private suspend fun SearchType.searchWith(
        fetcher: suspend () -> Result<List<MealSummary>>
    ): SearchUiState {
        return try {
            val meals = fetcher().getOrThrow()
            SearchUiState.Show(
                searchType = this,
                items = meals.map { it.toItemType() },
                searchTerm = this.displayText,
                isFetching = false,
                youtubeVideos = meals.mapNotNull { it.youtube }
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            SearchUiState.Error(e.message ?: "Search failed", searchType.displayText)
        }
    }

    fun updateSearchQuery(query: String) {
        logger.d { "updateSearchQuery: $query" }
        searchQueryIntent.tryEmit(query)
    }

    fun clearSearch() {
        logger.d { "clearSearch" }
        searchQueryIntent.tryEmit("")
    }

    @AssistedFactory
    interface Factory {
        fun create(searchType: SearchType): SearchViewModel
    }
}

internal fun MealSummary.toItemType(): ItemType = ItemType.MealType(this)
internal fun CodeRecipe.toItemType(): ItemType = ItemType.CodeRecipeType(this)
sealed class ItemType(val id: String) {

    data class MealType(val meal: MealSummary) :
        ItemType("meal-${meal.id}")

    data class CodeRecipeType(val codeRecipe: CodeRecipe) :
        ItemType ("recipe-${codeRecipe.id}")
}

sealed class SearchUiState {
    val searchText: String
        get() = when (this) {
            is Loading -> searchTerm
            is Error -> displayText
            is Show -> searchType.searchText
            else -> ""
        }

    data object Welcome : SearchUiState()
    data class Loading(val searchTerm: String, val showSearchBar: Boolean) : SearchUiState()
    data class Error(val message: String, val displayText: String) : SearchUiState()
    data class Show(
        val searchType: SearchType,
        val items: List<ItemType>,
        val isFetching: Boolean,
        val searchTerm: String,
        val youtubeVideos: List<String>
    ) : SearchUiState()
}