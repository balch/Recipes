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
            description = "- Use `isSystemInDarkTheme` and `dynamicColor` to control color scheme",
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
            description = """
                - Create annotation with an `@Preview` for each theme
                - Use `@ThemePreview` on Screens and Widgets
                 """.trimIndent(),
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
            area = CodeArea.Navigation3,
            title = "Bottom Navigation",
            description = """
                    - Wrap `NavigationBar` in `Scaffold` 
                    - Use `AnimatedVisibility` to control visibility of `NavigationBar`
                    - `TopLevelRoute` represent displayable items in `NavigationBarItem`
                    - Manage `backstack` in the `NavigationBar`
                """.trimIndent(),
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
            area = CodeArea.Navigation3,
            title = "entryDecorators",
            description = "- Define `entryDecorators` to provide state management and to facilitate ViewModel creation.",
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
            area = CodeArea.Navigation3,
            title = "entryProvider DSL syntax",
            description = """
                - Use `entryProvider` DSL syntax for simple App Nav
                - Provides a convenient way to create ViewModels and Screens on the backstack
                """.trimIndent(),
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
            area = CodeArea.Navigation3,
            title = "backstack",
            description = """
                    - You own the backstack.
                    - Simple push/pop works for simple applications
                """.trimIndent(),
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
            description = """
                - Use `HiltViewModel` and `assistedFactory` to creat unique ViewModel per screen to push on the backstack.
                - Define Factory using `@AssistedFactory` annotation
                - Use Factory to create ViewModels to pass to Screens via `hiltViewModel`                
                """.trimIndent(),
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
        CodeRecipe(
            area = CodeArea.Theme,
            title = """Markdown Render🎨💰""",
            description = """
                 - Simple Markdown Composable that renders beautiful code in _root_ide_package_.io.ktor.client.engine.android.Android(
                and other platforms
            ).
                 - Support Light/Dark Theme and Code Markdown syntax
                 - Thank you **Mike Penz**!!
                """.trimIndent(),
            fileName = "MarkdownCodeSnippet.kt",
            codeSnippet = """
                ```
                @Composable
                fun MarkdownCodeSnippet(
                    codeSnippet: String,
                    color: Color,
                    modifier: Modifier = Modifier
                ) {
                    val isDarkTheme = isSystemInDarkTheme()
                    val highlightsBuilder = remember(isDarkTheme) {
                        Highlights.Builder()
                            .theme(SyntaxThemes.atom(darkMode = isDarkTheme))
                            .language(SyntaxLanguage.KOTLIN)
                    }
                    Markdown(
                        modifier = modifier.padding(16.dp),
                        content = codeSnippet,
                        components = markdownComponents(
                            codeBlock = {
                                MarkdownHighlightedCodeBlock(
                                    content = it.content,
                                    node = it.node,
                                    highlightsBuilder = highlightsBuilder,
                                    showHeader = true, 
                                )
                            },
                            codeFence = {
                                MarkdownHighlightedCodeFence(
                                    content = it.content,
                                    node = it.node,
                                    highlightsBuilder = highlightsBuilder,
                                    showHeader = true,
                                )
                            },
                        )
                    )
                }
                ```
                """.trimIndent()
        ),
        CodeRecipe(
            area = CodeArea.Testing,
            title = "ViewModel Testing Setup",
            description = """
                - Define `TestDispatcherProvider` to control ViewModel Flows
                - Make sure the correct dispatcher is defined on the **Main Thread**
                """.trimIndent(),
            fileName = "DetailsViewModelTest.kt",
            codeSnippet = """
                ```
                @OptIn(ExperimentalCoroutinesApi::class)
                class DetailsViewModelTest {

                    private val dispatcherProvider: TestDispatcherProvider = TestDispatcherProvider()
                    private val repository = mock<RecipeRepository>()

                    @Before
                    fun setUp() {
                        Dispatchers.setMain(dispatcherProvider.testDispatcher)
                    }

                    @After
                    fun tearDown() {
                        Dispatchers.resetMain()
                    }
                }
                ```
                """.trimIndent()
        ),
        CodeRecipe(
            area = CodeArea.Testing,
            title = "StateFlow Testing with Turbine",
            description = """
                - Use `Turbine` for ViewModel stateFlow testing
                - Ensures all emissions are accounted for
                - May need to use `StandTestDispatcher` for Conflation issues when the ViewModel emits initial state too quickly. 
            """.trimIndent(),
            fileName = "SearchViewModelTest.kt",
            codeSnippet = """
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
                """.trimIndent()
        ),
        CodeRecipe(
            area = CodeArea.Testing,
            title = "Custom Test Assertions",
            description = "- Create extension functions for custom assertions to improve test readability and reusability",
            fileName = "DetailsViewModelTest.kt",
            codeSnippet = """
                ```
                private fun UiState.assertValidShowState(meal: Meal) {
                    assertThat(this).isInstanceOf(UiState.ShowMeal::class.java)
                    val state = this as UiState.ShowMeal
                    assertThat(state.meal).isEqualTo(meal)
                }

                private fun UiState.assertErrorState(message: String) {
                    assertThat(this).isInstanceOf(UiState.Error::class.java)
                    val state = this as UiState.Error
                    assertThat(state.message).isEqualTo(message)
                }
                ```
                """.trimIndent()
        ),
        CodeRecipe(
            area = CodeArea.Navigation3,
            title = "Enable `BackHandler` per screen",
            description = """
                - Conditionally enable `BackHandler` 
                - Use to return to initial Screen state before exiting app/screen
                """.trimIndent(),
            codeSnippet = """     
                ```                          
                @Composable
                fun IdeasScreen(
                    modifier: Modifier = Modifier,
                    viewModel: IdeasViewModel
                ) {
                    val uiState: IdeasUiState by viewModel.uiState.collectAsState()
                    // Return to categories if we are in an area or ingredient
                    BackHandler(enabled = uiState is IdeasUiState.Ingredients || uiState is IdeasUiState.Areas) {
                        viewModel.changeBrowsableType(BrowsableType.Category)
                    }
                
                    IdeasLayout(
                        uiState = uiState,
                        onBrowsableTypeChange = viewModel::changeBrowsableType,
                        modifier = modifier
                    )
                }          
                ```
                """.trimIndent(),
            fileName = "IdeasScreen.kt"
        ),
        CodeRecipe(
            area = CodeArea.Theme,
            title = "glassmorphism blur with Haze",
            description = """
                - Use **Haze** to create iOS-like glassmorphism blur 
                - Save the `hazeState` via `rememberHazeState`
                - Make the `NavDisplay` contents the source to blur by calling `hazeSource()`
                - Apply blur effect to `NavigationBar` by calling `hazeEffect()`
                   - For progressive blur use `HazeProgressive.verticalGradient`
                - Each Screen manages its own blur effect for the `TopAppBar`
                - Thank you **Chris Banes**!!
            """.trimIndent(),
            codeSnippet = """
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
            """.trimIndent(),
            fileName = "MainActivity.kt"
        ),
        CodeRecipe(
            area = CodeArea.Navigation3,
            title = "Bottom Nav Auto Hide",
            description = """
               - Calculate `showNavigationBar` from `firstVisibleIndex` and scroll direction
               - Use `showNavigationBar` in `AnimatedVisibility` to control visibility of `NavigationBar`
               - Delegate scroll handling to each Screen via `onScrollChange`
                  - Set `firstVisibleIndex` in handler to emit new `showNavigationBar` state 
            """.trimIndent(),
            codeSnippet = """
                ```
                @Composable
                private fun MainContent() {
                    var previousVisibleIndex by remember { mutableIntStateOf(0) }
                    var firstVisibleIndex by remember { mutableIntStateOf(0) }
                    var showNavigationBar by remember { mutableStateOf(true) }
                    LaunchedEffect(firstVisibleIndex) {
                        showNavigationBar = firstVisibleIndex == 0 || firstVisibleIndex < previousVisibleIndex
                        previousVisibleIndex = firstVisibleIndex
                    }
            
                    RecipesTheme {
                        Scaffold(
                            bottomBar = {
                                AnimatedVisibility(
                                    visible = showNavigationBar && backStackManager.peek() is TopLevelRoute,
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
            }
            ```
            """.trimIndent(),
            fileName = "MainActivity.kt"
        )
    )

    private val randomRecipes = mutableListOf<CodeRecipe>(
        *recipes.shuffled().toTypedArray()
    )

    /**
     * Retrieves a specified number of random `CodeRecipe` objects.
     * The method attempts to return the requested count of recipes
     * and refills the pool of available random recipes if necessary.
     * All shuffled recipes are displayed before reshuffling occurs.
     *
     * @param count The number of `CodeRecipe` objects to retrieve.
     * @return A list of `CodeRecipe` objects, with a size of up to the specified count.
     */
    fun getRandomRecipes(count: Int): List<CodeRecipe> {
        // Handle edge cases
        if (count <= 0) {
            return emptyList()
        }
        
        val result = mutableListOf<CodeRecipe>()
        var remaining = count
        
        // First, try to get recipes from the current shuffled pool
        while (remaining > 0 && randomRecipes.isNotEmpty()) {
            randomRecipes.removeLastOrNull()?.let { recipe ->
                result.add(recipe)
                remaining--
            }
        }
        
        // If we still need more recipes and have exhausted the current pool,
        // reshuffle and continue
        while (remaining > 0) {
            // Reshuffle the pool only when it's empty
            if (randomRecipes.isEmpty()) {
                randomRecipes.addAll(recipes.shuffled())
            }
            
            // Take more recipes from the newly shuffled pool
            randomRecipes.removeLastOrNull()?.let { recipe ->
                result.add(recipe)
                remaining--
            }
        }
        
        return result
    }

}