## Description

- Conditionally enable `BackHandler` 
- Use to return to initial ***Screen*** state before exiting app/screen

## Code Snippet

```
@Composable
fun IdeasScreen(
    modifier: Modifier = Modifier,
    viewModel: IdeasViewModel
) {
    // Return to categories if this is not a top level layout
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