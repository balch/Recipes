## Description

- Calculate `showNavigationBar` from `firstVisibleIndex` and scroll direction
- Use `showNavigationBar` in `AnimatedVisibility` to control visibility of `NavigationBar`
- Delegate scroll handling to each ***Screen*** via `onScrollChange`
  - Set `firstVisibleIndex` in handler to emit new `showNavigationBar` state
- Add `BackHandler` to show Bottom Nav instead of closing the App  

## Code Snippet

```
@Composable
private fun MainContent() {
    // remember backstack in a savable way
    val backStack = rememberNavBackStack(TOP_LEVEL_ROUTES[0])

    var previousVisibleIndex by remember { mutableIntStateOf(0) }
    var firstVisibleIndex by remember { mutableIntStateOf(0) }
    var showNavigationBar by remember { mutableStateOf(true) }
    LaunchedEffect(firstVisibleIndex) {
        showNavigationBar = firstVisibleIndex == 0 || firstVisibleIndex < previousVisibleIndex
        previousVisibleIndex = firstVisibleIndex
    }

    // override back button behavior to prevent closing the app when
    // there is only one screen and the nav bar is down
    BackHandler(enabled = backStack.isLastScreen() && !showNavigationBar) {
        showNavigationBar = true
    }

    RecipesTheme {
        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = showNavigationBar && backStack.peek() is TopLevelRoute,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it },
                ) {
                    NavigationBar(
                        // ...
                    )
                }
            }
        ) { innerPadding ->
            NavDisplay(
                entryProvider = entryProvider {
                    entry<Ideas> {
                        IdeasScreen(
                            onScrollChange = { firstVisibleIndex = it }
                            // ...
                        )
                    }
                    entry<SearchRoute> { searchRoute ->
                        SearchScreen(
                            onScrollChange = { firstVisibleIndex = it },
                            // ...
                        )
                    }
                    entry<Search> {
                        SearchScreen(
                            onScrollChange = { firstVisibleIndex = it },
                            // ...
                        )
                    }
                    // ...
                },
            )
        }
    }
}
```