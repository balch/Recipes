## Description

- Use **Haze** to create iOS-like glassmorphism blur 
- Save the `hazeState` via `rememberHazeState`
- Make the `NavDisplay` contents the source to blur by calling `hazeSource()`
- Apply blur effect to `NavigationBar` by calling `hazeEffect()`
   - For progressive blur use `HazeProgressive.verticalGradient`
- Each ***Screen*** manages its own blur effect for the `TopAppBar`
- Thank you [Chris Banes](https://chrisbanes.github.io/haze/latest/)!!

## Code Snippet

```
@Composable
private fun MainContent() {
    val hazeState = rememberHazeState()

    RecipesTheme {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .hazeEffect(state = hazeState, style = LocalHazeStyle.current) {
                            HazeProgressive.verticalGradient(
                                startIntensity = 1f,
                                endIntensity = 0f,
                            )
                        },
                    ) {
                    // ...
                    }
                }
            }
        ) { innerPadding ->
            NavDisplay(
                modifier = Modifier.hazeSource(state = hazeState),
                entryProvider = entryProvider {
                    entry<Ideas> {
                        IdeasScreen(
                        // ...
                        )
                    }
                    entry<SearchRoute> { searchRoute ->
                        // ...
                    }
                    entry<Search> {
                        // ...
                    }
                    entry<DetailRoute> { detailRoute ->
                        // ...
                    }
                    entry<Info> { InfoScreen() }
                },
            )
        }
    }
}                
```