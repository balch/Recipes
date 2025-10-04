## Description

- `DispatcherProvider` injected into ViewModels to control StateFlows
- `TestDispatcherProvider` implements `DispatcherProvider` for testing
- `MainCoroutineExtension` assigns provided `TestDispatcher` to `Dispatchers.Main`

## Code Snippet

**Declare Junit 5 Extension**   
```
class MainCoroutineExtension(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : BeforeEachCallback, AfterEachCallback {               
    override fun beforeEach(context: ExtensionContext) {
        Dispatchers.setMain(testDispatcher)
    }                
    override fun afterEach(context: ExtensionContext) {
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