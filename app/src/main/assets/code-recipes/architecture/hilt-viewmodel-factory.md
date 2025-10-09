## Description

- Use `HiltViewModel` and `assistedFactory` to create unique ***ViewModel*** per ***Screen*** to push on the ***backstack***.
- Define Factory using `@AssistedFactory` annotation
- Use Factory to create ViewModels to pass to Screens via `hiltViewModel`

## Code Snippet

```
// ViewModel Definition
@HiltViewModel(assistedFactory = DetailsViewModel.Factory::class)
class DetailsViewModel @AssistedInject constructor(
    @Assisted val detailType: DetailType,
    private val repository: RecipeRepository,
    dispatcherProvider: DispatcherProvider
) : ViewModel() {

    // ViewModel implementation...    

    // ViewModel Factory
    @AssistedFactory
    interface Factory {
        fun create(detailType: DetailType): DetailsViewModel
    }
}

// Create ViewModel and pass it to the Screen 
@Composable
fun DetailRoute(detailRoute: DetailRoute) {
    val viewModel = hiltViewModel<DetailsViewModel, DetailsViewModel.Factory>(
        creationCallback = { factory ->
            factory.create(detailRoute.detailType)
        }
    )
    DetailScreen(viewModel = viewModel)
}
```