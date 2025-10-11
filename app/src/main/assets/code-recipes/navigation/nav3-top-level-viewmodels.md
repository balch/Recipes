## Description

- Top Level Screens should maintain their last state when navigating back to them
- `rememberViewModelStoreNavEntryDecorator()` causes the ***ViewModel*** to be cleared when the screen is popped off the ***backstack***
- Explicitly set `viewModelStoreOwner = this@MainActivity` when creating top level ***ViewModels***
  - Use `started = SharingStarted.Lazily` when creating the ***StateFlow*** to ensure state is not reset when ***Screen*** is not on the top of the ***backstack***

## Code Snippet

```
val backStack = rememberNavBackStack(TOP_LEVEL_ROUTES[0])

NavDisplay(
    backStack = backStack,
    onBack = { backStack.pop() },    
    entryDecorators = listOf(
        rememberSceneSetupNavEntryDecorator(),
        rememberSavedStateNavEntryDecorator(),
        
        // causes ViewModels to be cleared when the screen is popped off the backstack
        rememberViewModelStoreNavEntryDecorator()
    ),

    // Use the Activities `ViewModelStoreOwner` for ViewModels belonging to
    // `TopLevelRoute` NavEntry routes. This allows screens to maintain their last
    // states when navigating back to a top level screen
    entryProvider = entryProvider {
        entry<Ideas> {
            IdeasScreen(
                viewModel = hiltViewModel(
                    viewModelStoreOwner = this@MainActivity,
                ),
                // ...
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
            // Note: SearchViewModel uses instance tied to activity from this route
            val viewModel =
                hiltViewModel<SearchViewModel, SearchViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(SearchType.Search(""))
                    },
                    viewModelStoreOwner = this@MainActivity
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
                viewModelStoreOwner = this@MainActivity,
            ),
        ) }
    },
)
```