## Description

- Use `Turbine` for ***ViewModel*** stateFlow testing
- Ensures all emissions are accounted for
- May need to use `StandardTestDispatcher` for Conflation issues when the ViewModel emits initial state too quickly.

## Code Snippet

```
private val testMeals = listOf(
    Meal(id = "1", name = "Pasta Carbonara"),
    Meal(id = "2", name = "Pasta Bolognese")
)

private fun getViewModel(searchType: SearchType): SearchViewModel {
    return SearchViewModel(
        searchType = searchType,
        repository = repository,
        dispatcherProvider = dispatcherProvider
    )
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
            meals = testMeals,
            isFetching = false
        )

        viewModel.clearSearch()
        awaitItem().assertWelcomeState()
    }
}
```