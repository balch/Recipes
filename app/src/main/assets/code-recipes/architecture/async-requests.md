## Description

- Sometimes the ***ViewModel*** state requires data from multiple sources
- Optimize these use cases by _parallelizing_ the API requests 
  - Emit `Loading` state to the UI when loading data
  - Create **async jobs** for each API call scoped to `coroutineScope`
  - Wait for all the jobs to complete and emit the results
- Catch exceptions in `.catch` and emit Error state

## Code Snippet

```
@HiltViewModel
class IdeasViewModel @Inject constructor(
    private val repository: RecipeRepository,
    dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val browsableTypeFlow = MutableStateFlow(BrowsableType.Category)

    val uiState: StateFlow<IdeasUiState> =
        browsableTypeFlow.transformLatest { browsableType ->
            emit(IdeasUiState.Loading)
            emit(deriveState(browsableType))
        }
        .catch { e ->
            emit(IdeasUiState.Error(e.message ?: "Unknown error occurred"))
        }
        .onEach { logger.d { "UIState: ${it.javaClass.simpleName} } }
        .flowOn(dispatcherProvider.default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = IdeasUiState.Loading
        )

    private suspend fun deriveState(browsableType: BrowsableType): IdeasUiState =
        when (browsableType) {
            // ...
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
            // ...
        }
    }

    fun changeBrowsableType(browsableType: BrowsableType) {
        browsableTypeFlow.value = browsableType
    }
}

sealed interface IdeasUiState {
    data object Loading : IdeasUiState
    data class Error(val message: String) : IdeasUiState
    data class Areas(
        val areas: List<Area>,
        override val imageUrl: String?,
    ) : IdeasUiState
}
```