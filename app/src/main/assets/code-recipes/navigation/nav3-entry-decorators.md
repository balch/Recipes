## Description

- Define **Nav3** `entryDecorators` to provide state management and to facilitate ***ViewModel*** creation.

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