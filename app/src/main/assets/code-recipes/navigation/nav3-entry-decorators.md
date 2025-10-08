## Description

- Define **Nav3** `entryDecorators` to provide necessary configuration for state management and ***ViewModel*** creation
  - `rememberSceneSetupNavEntryDecorator()` manages and optimizes lifecycle of ***Screens*** and **NavEntry**s
  - `rememberSavedStateNavEntryDecorator()` facilitates saving state across Process Death using `rememberSaveable` 
  - `rememberViewModelStoreNavEntryDecorator()` causes the ***ViewModel** to be cleared when the screen is popped off the ***backstack***

## Code Snippet

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