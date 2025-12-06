## Description

- Migrate from **Hilt** to **Metro** dependency injection
- Replace Hilt annotations with Metro equivalents
- Update ViewModel injection patterns for Compose

## Key Migration Steps

| Hilt | Metro |
|------|-------|
| `@HiltAndroidApp` | Create `AppGraph` interface |
| `@AndroidEntryPoint` | Manual injection via `graph.inject(this)` |
| `@Singleton` | `@SingleIn(AppScope::class)` |
| `@Binds` | `@ContributesBinding` |
| `@HiltViewModel` | `@ViewModelKey` + `@ContributesIntoMap` |
| `@AssistedInject` + `@AssistedFactory` | `@AssistedInject` + `ManualViewModelAssistedFactory` |

## Code Snippet

```kotlin
// BEFORE: Hilt ViewModel
@HiltViewModel(assistedFactory = DetailsViewModel.Factory::class)
class DetailsViewModel @AssistedInject constructor(
    @Assisted val detailType: DetailType,
    private val repository: RecipeRepository
) : ViewModel() {
    
    @AssistedFactory
    interface Factory {
        fun create(detailType: DetailType): DetailsViewModel
    }
}

// AFTER: Metro ViewModel with Assisted Injection
class DetailsViewModel @AssistedInject constructor(
    @Assisted val detailType: DetailType,
    private val repository: RecipeRepository
) : ViewModel() {

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey(Factory::class)
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(detailType: DetailType): DetailsViewModel
    }
}

// BEFORE: Compose with Hilt
val viewModel = hiltViewModel<DetailsViewModel, DetailsViewModel.Factory>(
    creationCallback = { factory -> factory.create(detailType) }
)

// AFTER: Compose with Metro
val viewModel = assistedMetroViewModel<DetailsViewModel, DetailsViewModel.Factory> { 
    create(detailType)
}
```
