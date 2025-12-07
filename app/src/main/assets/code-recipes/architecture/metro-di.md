## Description

- Use **Metro DI** with `@DependencyGraph` and `@Inject` for compile-time dependency injection
- Define scopes with marker objects and `@SingleIn`
- Contribute bindings with `@ContributesBinding` and `@ContributesTo`

## Code Snippet

```kotlin
// 1. Define the Dependency Graph
@DependencyGraph(AppScope::class)
interface AppGraph : MetroAppComponentProviders, ViewModelGraph {
    @Provides fun provideApplicationContext(application: Application): Context = application

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides application: Application): AppGraph
    }
}

// 2. Setup Application
class RecipesApplication : Application(), MetroApplication, SingletonImageLoader.Factory {
    // Create the graph
    private val appGraph by lazy { createGraphFactory<AppGraph.Factory>().create(this) }

    override val appComponentProviders: MetroAppComponentProviders
        get() = appGraph
        
    // ...
}

// 3. Inject into Activity
@ContributesIntoMap(AppScope::class, binding<Activity>())
@ActivityKey(MainActivity::class)
@Inject
class MainActivity(
    private val metroVmf: MetroViewModelFactory,
    // other dependencies...
) : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Pass the factory to your content or composition local
            MainContent(metroVmf = metroVmf, ...)
        }
    }
}

// 4. Use ViewModels (via MetroViewModelFactory)
@Composable
fun MainContent(metroVmf: MetroViewModelFactory, ...) {
    CompositionLocalProvider(LocalMetroViewModelFactory provides metroVmf) {
        val viewModel: AgentViewModel = metroViewModel()
        // ...
    }
}
```
