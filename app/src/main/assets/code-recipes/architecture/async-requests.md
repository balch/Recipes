## Description

- Sometimes the ***ViewModel*** state requires data from multiple sources
- Optimize these use cases by _parallelizing_ the API requests 
  - Emit `Loading` state to the UI when loading data
  - Create async jobs for each API call scoped to the ***ViewModel*** 
    - `val job = viewModelScope.async { }`
  - Wait for all the jobs to complete and emit the results
    - Use `val result = job.await()` to get the result when the job completes

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
        .flowOn(dispatcherProvider.default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = IdeasUiState.Loading
        )

    private suspend fun deriveState(browsableType: BrowsableType): IdeasUiState =
        try {
            when (browsableType) {
                // ...
                BrowsableType.Area -> {
                    val areasJob = viewModelScope.async { repository.getAreas() }
                    val randomMealJob = viewModelScope.async { repository.getRandomMeal() }

                    IdeasUiState.Areas(
                        areas = areasJob.await().getOrThrow(),
                        imageUrl = randomMealJob.await().getOrNull()?.thumbnail,
                        codeRecipes = randomCodeRecipes
                    )
                }
                // ...
            }
        } catch (e: Exception) {
            IdeasUiState.Error(e.message ?: "Unknown error occurred")
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