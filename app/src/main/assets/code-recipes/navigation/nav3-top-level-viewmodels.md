## Description

- Top Level Screens should maintain their last state when navigating back to them
- `rememberViewModelStoreNavEntryDecorator()` causes the ***ViewModel*** to be cleared when the screen is popped off the ***backstack***
- Explicitly set `key` when creating top level ***ViewModels***
   - Example: `hiltViewModel(key = "IdeasTopLevelRoute")`
   - Use `started = SharingStarted.Lazily` when creating the ***StateFlow*** to ensure state is not reset when ***Screen*** is not on the top of the ***backstack***

## Code Snippet

```
val backStack = rememberNavBackStack(TOP_LEVEL_ROUTES.first())

NavDisplay(
    backStack = backStack,
    onBack = { backStack.pop() },    
    entryDecorators = listOf(
        // causes ViewModels to be cleared when the screen is popped off the backstack
        rememberViewModelStoreNavEntryDecorator()
    ),

    entryProvider = entryProvider {
        entry<Ideas> {
            // Note: Use the same ViewModel instance for all Ideas navigation
            IdeasScreen(
                viewModel = hiltViewModel(key = "ideasTopLevelRoute"),
            )
        }
        entry<SearchRoute> { searchRoute ->
        
            // Note: Creates a new ViewModel instance for each SearchRoute navigation
            // (not scoped to Activity, so will be cleared when popped)
            val viewModel =
                hiltViewModel<SearchViewModel, SearchViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(searchRoute.searchType)
                    }
                )
            SearchScreen(
                viewModel = viewModel,
                // ...

            )
        }
        entry<Search> {
            // Note: SearchViewModel uses `SearchTopLevelRoute` instance 
            val viewModel =
                hiltViewModel<SearchViewModel, SearchViewModel.Factory>(
                    key = "SearchTopLevelRoute",
                    creationCallback = { factory ->
                        factory.create(SearchType.Search(""))
                    },
                )
            SearchScreen(
                viewModel = viewModel,
                // ...
            )
        }
        entry<DetailRoute> { detailRoute ->
            val viewModel =
                hiltViewModel<DetailsViewModel, DetailsViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(detailRoute.detailType)
                    }
                )

            DetailScreen(
                viewModel = viewModel,
                onBack = { backstack.pop() }
            )
        }
        entry<Info> { InfoScreen(
            viewModel = hiltViewModel(
                viewModel = hiltViewModel(key = "InfoTopLevelRoute")
            ),
        ) }
    },
)
```