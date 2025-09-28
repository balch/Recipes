package org.balch.recipes.features

import com.diamondedge.logging.logging
import org.balch.recipes.core.models.CodeArea
import org.balch.recipes.core.models.CodeRecipe
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CodeRecipes @Inject constructor() {

    private val logger = logging(this::class.simpleName)
    val sortedRecipes by lazy {
        recipes.sortedWith(
            compareBy<CodeRecipe> { it.area.name }
                .thenBy { it.title }
        ).also { logger.v { "Sorted Recipes: $it" } }
    }

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
        if (count <= 0) { return emptyList() }

        if (randomRecipes.size < count) {
            randomRecipes.addAll(recipes.shuffled())
            logger.d { "Reshuffled Code Recipes Pool" }
        }

        val result = mutableListOf<CodeRecipe>()
        repeat(count) {
            result.add(randomRecipes.removeFirst())
        }
        return result
            .also { logger.v { "getRandomRecipes: $it" } }
    }

    private val randomRecipes by lazy {
        mutableListOf(*recipes.shuffled().toTypedArray())
    }

    companion object {
        private data class CodeRecipeRaw(
            val area: CodeArea,
            val title: String,
            val description: String,
            val fileName: String? = null,
            val codeSnippet: String? = null,
        ) {
            fun toCodeRecipe(index: Int) = CodeRecipe(
                index = index,
                area = area,
                title = title,
                description = description,
                fileName = fileName,
                codeSnippet = codeSnippet,
            )
        }

        /**
         * Maintiant the index based on the order of the `recipesRaw` list
         * without having to update indiviudal elements.
         *
         * This allows me to rank them in order of importance and display
         * the rank in the Detail Page Title
         */

        private val recipes by lazy {
            rawRecipes.mapIndexed { index, rawRecipe ->
                rawRecipe.toCodeRecipe(index + 1)
            }
        }
        private val rawRecipes by lazy {
            listOf(
                CodeRecipeRaw(
                    area = CodeArea.Architecture,
                    title = "Separation of Concerns",
                    description = """
                         - Encapsulate feature functionality into Modules
                         - Each Module contains separate classes for Biz Logic and UI
                            - Use ViewModels for BizLogic
                            - Use Compose Screens for UI
                         - Hoist State from ViewModel to UI to render the UX
                         - Send user action back to ViewModel to generate new state 
                         - **Separation of Concerns** facilitates **Single Responsibility** principle
                        """.trimIndent(),
                    codeSnippet = """
                       **App File Structure**
                        ```
                        app/src/main/java/org/balch/recipes/
                        ├── MainActivity.kt                 # Entry point with navigation
                        ├── RecipesApplication.kt           # App-level configuration
                        ├── NavRoutes.kt                    # Route definitions
                        │
                        ├── features/                       # 🎯 FEATURE MODULES
                        │   ├── ideas/                      # Ideas feature (Single Responsibility)
                        │   │   ├── IdeasScreen.kt          # ✨ UI Layer - Compose Screen
                        │   │   └── IdeasViewModel.kt       # 🧠 Business Logic Layer
                        │   ├── search/                     # Search feature (Single Responsibility)  
                        │   │   ├── SearchScreen.kt         # ✨ UI Layer - Compose Screen
                        │   │   └── SearchViewModel.kt      # 🧠 Business Logic Layer
                        │   └── details/                    # Details feature (Single Responsibility)
                        │       ├── DetailScreen.kt         # ✨ UI Layer - Compose Screen
                        │       └── DetailsViewModel.kt     # 🧠 Business Logic Layer
                        │
                        ├── core/                           # 🔧 SHARED BUSINESS LOGIC
                        │   ├── models/                     # Data models used across features
                        │   │   ├── Meal.kt                 
                        │   │   ├── Category.kt             
                        │   │   └── CodeRecipe.kt           
                        │   ├── network/                    # API communication layer
                        │   │   ├── ApiService.kt           
                        │   │   ├── HttpClientFactory.kt           
                        │   │   └── TheMealDbApi.kt         
                        │   ├── repository/                 # Data access abstraction
                        │   │   ├── RecipeRepository.kt     
                        │   │   └── RepositoryModule.kt     
                        │   └── coroutines/                 # Async utilities
                        │       └── DispatcherProvider.kt   
                        │
                        └── ui/                             # 🎨 SHARED UI COMPONENTS
                            ├── theme/                      # Material 3 theming
                            │   ├── Theme.kt                
                            │   ├── Color.kt                
                            │   └── ThemePreview.kt         
                            ├── widgets/                    # Reusable UI components
                            │   ├── FoodLoadingIndicator.kt
                            │   └── WebViewScreen.kt        
                            └── nav/                        # Navigation utilities
                                └── BackStackManager.kt     
                                
                        🎯 Each FEATURE has dedicated ViewModel + Screen (Single Responsibility)
                        🔧 CORE contains shared business logic and data access
                        🎨 UI contains shared visual components and theming
                        ```                                              
                    """.trimIndent()
                ),
                CodeRecipeRaw(
                    area = CodeArea.Architecture,
                    title = "Repository with ApiService",
                    description = """
                        - Use **Ktor** for Networking
                        - Create injectable `HttpClientFactory` to configure HTTP connections
                        - `ApiService` provides call patterns for HTTP requests
                        - Use `ApiService` to create specific services to remote apis
                        - Wrap the Specific ApiSerivce in  a Repository pattern to map raw API responses to domain objects
                    """.trimIndent(),
                    codeSnippet = """
                        ```
                        @Singleton
                        class ApiService @Inject constructor(
                            private val httpClientFactory: HttpClientFactory,
                            val dispatcherProvider: DispatcherProvider,
                        ) {
                            val client: HttpClient by lazy { httpClientFactory.create() }
                        
                            suspend inline fun <reified T> get(
                                url: String,
                                parameters: Map<String, String> = emptyMap()
                            ): Result<T> =
                                // ...
                        
                            fun close() {
                                client.close()
                            }
                        }

                        @Singleton
                        class TheMealDbApi @Inject constructor(
                            private val apiService: ApiService
                        ) {
                            companion object {
                                private const val BASE_URL = 'https://www.themealdb.com/api/json/v1/1'
                                private const val CATEGORIES = "BASE_URL/categories.php"
                                private const val MEAL_BY_ID = "BASE_URL/lookup.php"
                            }
                            
                            suspend fun getCategories(): Result<CategoriesResponse> {
                                return apiService.get(CATEGORIES)
                            }
                            
                            suspend fun getMealById(id: String): Result<MealResponse> {
                                return apiService.get(
                                    url = MEAL_BY_ID,
                                    parameters = mapOf("i" to id)
                                )
                            }
                        }

                        @Singleton
                        class RecipeRepositoryImpl @Inject constructor(
                            private val api: TheMealDbApi
                        ) : RecipeRepository {
                            override suspend fun getCategories(): Result<List<Category>> {
                                return api.getCategories().map { response ->
                                    response.categories
                                }
                            }
                            override suspend fun getMealById(id: String): Result<Meal> {
                                return try {
                                    api.getMealById(id).map { response ->
                                        response.meals.firstOrNull()
                                            ?: throw IllegalArgumentException("Meal not found")
                                    }
                                } catch (e: Exception) {
                                    Result.failure(e)
                                }
                            }
                        }
                        ```
                    """.trimIndent()
                ),
                CodeRecipeRaw(
                    area = CodeArea.Theme,
                    title = "ColorScheme",
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
                CodeRecipeRaw(
                    area = CodeArea.Theme,
                    title = "ThemePreview",
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
                CodeRecipeRaw(
                    area = CodeArea.Navigation,
                    title = "Bottom Nav",
                    description = """
                    - Wrap `NavigationBar` in `Scaffold` 
                    - Use `AnimatedVisibility` to control visibility of `NavigationBar`
                    - `TopLevelRoute` represent displayable items in `NavigationBarItem`
                    - Manage `backstack` in the `NavigationBar`
                       - Pop the current screen off the backstack if it not the root
                       - Push the new route onto the backstack
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
                                        onClick = {
                                            // pop the current screen off the backstack if it not the root
                                            if (backStackManager.peek() != TOP_LEVEL_ROUTES[0]) {
                                                backStackManager.pop()
                                            }
                                            // push the new route onto the backstack
                                            if (backStackManager.peek() != topLevelRoute) {
                                                backStackManager.push(topLevelRoute)
                                            }
                                        },
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
                CodeRecipeRaw(
                    area = CodeArea.Navigation,
                    title = "Nav3 EntryDecorators",
                    description = "- Define **Nav3** `entryDecorators` to provide state management and to facilitate ViewModel creation.",
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
                CodeRecipeRaw(
                    area = CodeArea.Navigation,
                    title = "Nav3 EntryProvider DSL Syntax",
                    description = """
                - Use **Nav3** `entryProvider` DSL syntax for simple App Nav
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
                CodeRecipeRaw(
                    area = CodeArea.Navigation,
                    title = "Nav3 Backstack Management",
                    description = """
                    - In **Nav3**, you own the backstack.
                    - Push/Pop works for simple applications
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
                CodeRecipeRaw(
                    area = CodeArea.Architecture,
                    title = "Hilt ViewModel Factory",
                    description = """
                - Use `HiltViewModel` and `assistedFactory` to create unique ViewModel per screen to push on the backstack.
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
                CodeRecipeRaw(
                    area = CodeArea.Theme,
                    title = """Markdown Render🎨💰""",
                    description = """
                 - Simple Markdown Composable that renders beautiful code in Android (and other platforms)
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
                CodeRecipeRaw(
                    area = CodeArea.Testing,
                    title = "TestDispatcher on MainThread",
                    description = """
                    - `DispatcherProvider` injected into ViewModels to control StateFlows
                    - `TestDispatcherProvider` implements `DispatcherProvider` for testing
                    - `MainCoroutineExtension` assigns provided `TestDispatcher` to `Dispatchers.Main`
                """.trimIndent(),
                    fileName = "DetailsViewModelTest.kt",
                    codeSnippet = """
                    
                **Declare Junit 5 Extension**   
                ```
                class MainCoroutineExtension(
                    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
                ) : BeforeEachCallback, AfterEachCallback {               
                    override fun beforeEach(context: ExtensionContext?) {
                        Dispatchers.setMain(testDispatcher)
                    }                
                    override fun afterEach(context: ExtensionContext?) {
                        Dispatchers.resetMain()
                    }
                }
                ```
                
                **Use Extension in Test Class**   
                ```
                class DetailsViewModelTest {

                    private val dispatcherProvider: TestDispatcherProvider = TestDispatcherProvider()

                    @JvmField
                    @RegisterExtension
                    val mainCoroutineExtension = MainCoroutineExtension(dispatcherProvider.testDispatcher)

                    private val repository = mock<RecipeRepository>()
                    
                    // ...
                }
                ```
                """.trimIndent()
                ),
                CodeRecipeRaw(
                    area = CodeArea.Testing,
                    title = "Turbine For StateFlow Testing",
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
                CodeRecipeRaw(
                    area = CodeArea.Testing,
                    title = "Test Assertion Extensions",
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
                CodeRecipeRaw(
                    area = CodeArea.Navigation,
                    title = "BackHandler in Screens",
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
                    // Return to categories if this is not a top level tob
                    BackHandler(enabled = !uiState.isTopLevelState) {
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
                CodeRecipeRaw(
                    area = CodeArea.Theme,
                    title = "Glass Blur with Haze",
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
                CodeRecipeRaw(
                    area = CodeArea.Navigation,
                    title = "Bottom Nav AutoHide",
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
            ),
            CodeRecipeRaw(
                area = CodeArea.Architecture,
                title = "Random Exhaustive List ",
                description = """
                - Sometimes you need to randomly show items from a list 
                - Using a random index is easy, but leads to many repeated items
                - Its better to show all the items in a random list to enure freshness  
                    - Save shuffled main list to randomize order
                    - Return and remove items from front of saved list 
                    - Add more shuffled items when capacity runs low
            """.trimIndent(),
                fileName = "CodeRecipes.kt",
                codeSnippet = """
                ```
                private val randomRecipes by lazy {
                    mutableListOf(*recipes.shuffled().toTypedArray())
                }
                fun getRandomRecipes(count: Int): List<CodeRecipe> {
                    if (count <= 0) { return emptyList() }
            
                    if (randomRecipes.size < count) {
                        randomRecipes.addAll(recipes.shuffled())
                    }
            
                    val result = mutableListOf<CodeRecipe>()
                    repeat(count) {
                        result.add(randomRecipes.removeFirst())
                    }
                    return result
                }
                ```
            """.trimIndent()
            )
            )
        }
    }
}