## Description

- Use **Metro DI** with `@DependencyGraph` and `@Inject` for compile-time dependency injection
- Define scopes with marker objects and `@SingleIn`
- Contribute bindings with `@ContributesBinding` and `@ContributesTo`

## Code Snippet

```kotlin
// 1. Define a scope marker
object AppScope

// 2. Create the dependency graph
@DependencyGraph(AppScope::class)
interface AppGraph {
    val metroViewModelFactory: MetroViewModelFactory
    fun inject(activity: MainActivity)
    
    companion object {
        fun create(): AppGraph = createAppGraph()
    }
}

// 3. Mark singletons with @SingleIn
@SingleIn(AppScope::class)
class ApiService @Inject constructor(
    private val httpClient: HttpClient
) { /* ... */ }

// 4. Contribute bindings automatically
@ContributesBinding(AppScope::class)
class DefaultDispatcherProvider @Inject constructor() : DispatcherProvider

// 5. Use ViewModels with Metro annotations
@Inject
@ViewModelKey(HomeViewModel::class)
@ContributesIntoMap(AppScope::class)
class HomeViewModel : ViewModel() { /* ... */ }

// 6. Access in Compose via metroViewModel()
@Composable
fun HomeScreen(viewModel: HomeViewModel = metroViewModel()) {
    // ...
}
```
