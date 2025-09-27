package org.balch.recipes.features

import org.balch.recipes.core.models.CodeArea
import org.balch.recipes.core.models.CodeRecipe
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CodeRecipes @Inject constructor() {
    private val recipes = listOf(
        CodeRecipe(
            area = CodeArea.Theme,
            title = "colorScheme",
            description = "Use `isSystemInDarkTheme` and `dynamicColor` to control color scheme",
            fileName = "RecipesTheme.kt",
            codeSnippet = """
                ```
                val colorScheme = when {
                    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                        val context = LocalContext.current
                        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                    }
                    darkTheme -> DarkColorScheme
                    else -> LightColorScheme
                }
                ```
                """.trimIndent()
        ),
        CodeRecipe(
            area = CodeArea.Theme,
            title = "colorScheme",
            fileName = "ThemePreview.kt",
            description = "Create annotation with sn `@Preview` for each theme",
            codeSnippet = """
                ```
                @Preview(
                    uiMode = Configuration.UI_MODE_NIGHT_YES,
                    group = "Theme",
                    name = "ThemeDark",
                )
                @Preview(
                    uiMode = Configuration.UI_MODE_NIGHT_NO,
                    group = "Theme",
                    name = "ThemeLight",
                )
                annotation class ThemePreview
                ```
                """.trimIndent()
        ),
        CodeRecipe(
            area = CodeArea.Navigation,
            title = "Bottom Navigation",
            description = "Wrap `NavigationBar` in `Scaffold` and `AnimatedVisibility` to position and display the `NavigationBarItem`",
            fileName = "MainActivity.kt",
            codeSnippet = """
                ```
                Scaffold(
                    bottomBar = {
                        AnimatedVisibility(
                            visible = showNavigationBar && backStackManager.peek() is TopLevelRoute,
                            enter = slideInVertically { it },
                            exit = slideOutVertically { it },
                        ) {
                            NavigationBar(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ) {
                                TOP_LEVEL_ROUTES.forEach { topLevelRoute ->
                                    val isSelected = topLevelRoute == backStackManager.peek()
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { backStackManager.push(topLevelRoute) },
                                        icon = {
                                            Icon(
                                                imageVector = topLevelRoute.icon,
                                                contentDescription = topLevelRoute.contentDescription
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                    ```
                    """.trimIndent()
        ),
        CodeRecipe(
            area = CodeArea.Navigation,
            title = "entryDecorators",
            description = "Define `entryDecorators` to provide state management and to facilitate ViewModel creation.",
            fileName = "MainActivity.kt",
            codeSnippet = """
                ```
                    // In order to add the `ViewModelStoreNavEntryDecorator`
                    // we also need to add the default `NavEntryDecorator`s as well. These provide
                    // extra information to the entry's content to enable it to display correctly
                    // and save its state.
                    entryDecorators = listOf(
                        rememberSceneSetupNavEntryDecorator(),
                        rememberSavedStateNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                ```                                
                """.trimIndent()
        ),
        CodeRecipe(
            area = CodeArea.Navigation,
            title = "entryProvider DSL syntax",
            description = "For simple apps, the `entryProvider` DSL syntax provides a convenient way to create ViewModels and push screens on the Backstack.",
            fileName = "MainActivity.kt",
            codeSnippet = """
                ```
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
                    }
                ```
            """.trimIndent()
        ),
        CodeRecipe(
            area = CodeArea.Navigation,
            title = "backstack",
            description = "You own the backstack. Simple push/pop works for simple applications",
            fileName = "BackstackManager.kt",
            codeSnippet = """
                ```
                @ActivityRetainedScoped
                class BackStackManager @Inject constructor() {
                    private val _backStack : SnapshotStateList<NavKey> = mutableStateListOf(Ideas)
                
                    val backStack: List<NavKey>
                        get() = _backStack.toList()
                
                    fun push(destination: NavKey){
                        _backStack.add(destination)
                    }
                
                    fun pop(){
                        _backStack.removeLastOrNull()
                    }
                
                    fun peek(): NavKey? =
                        _backStack.lastOrNull()
                }
                ```
                """.trimIndent()
        ),
        CodeRecipe(
            area = CodeArea.Architecture,
            title = "ViewModel creation",
            description = "Use `HiltViewModel` and `assistedFactory` to creat unique ViewModel per screen to push on the backstack.",
            fileName = "DetailsViewModel.kt",
            codeSnippet = """
                ```
                // ViewModel Definition
                @HiltViewModel(assistedFactory = DetailsViewModel.Factory::class)
                class DetailsViewModel @AssistedInject constructor(
                    @Assisted val detailType: DetailType,
                    private val repository: RecipeRepository,
                    dispatcherProvider: DispatcherProvider
                ) : ViewModel() {

                // ViewModel Factory
                @AssistedFactory
                interface Factory {
                    fun create(detailType: DetailType): DetailsViewModel
                }

                // Create ViewModel
                val viewModel =
                    hiltViewModel<DetailsViewModel, DetailsViewModel.Factory>(
                        creationCallback = { factory ->
                            factory.create(detailRoute.detailType)
                        }
                    )
                ```
            """.trimIndent()
        ),
    )

    fun getRandomRecipes(count: Int): List<CodeRecipe> =
        recipes.shuffled().take(count)
}