## Description

- Use `Turbine` for ***ViewModel*** stateFlow testing
- Ensures all emissions are accounted for
- May need to use `StandTestDispatcher` for Conflation issues when the ***ViewModel*** emits initial state too quickly.

## Code Snippet

```
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