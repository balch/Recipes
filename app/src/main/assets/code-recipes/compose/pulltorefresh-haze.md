## Description

- Wrap `PullToRefreshBox` in `Scaffold` content section
- Declare `state` to share between `PullToRefreshBox` and `IndicatorBox`
- Define `IndicatorBox` that contains a custom indicator Composable
- Use `state.distanceFraction` to provide feedback
     - Only render indicator when _distance_ is greater than 0
     - Use _distance_ to show dynamic text
- Add the `.hazeSource(hazeState)` to the `modifier` of the custom indicator Composable

## Code Snippet

```
val state = rememberPullToRefreshState()
val isRefreshing = uiState is IdeasUiState.Loading
PullToRefreshBox(
    isRefreshing = isRefreshing,
    onRefresh = onRetry,
    modifier = modifier,
    state = state,
    indicator = {
        IndicatorBox(
            modifier = Modifier
                .align(Alignment.TopCenter),
            isRefreshing = isRefreshing,
            containerColor = Color.Transparent,
            maxDistance = 128.dp,
            elevation = 8.dp,
            state = state
        ) {
            if (state.distanceFraction > 0F) {
                val text = when {
                    isRefreshing -> "Refreshing Food and Code..."
                    state.distanceFraction > 1F -> "Got it!!!"
                    state.distanceFraction > 0.9F -> "Almost.."
                    state.distanceFraction > 0.5F -> "Keep pulling..."
                    state.distanceFraction > 0.05F -> "Harder......"
                    else -> ""
                }
                val rotationDegrees =
                    (360f * (state.distanceFraction.coerceIn(0f, 1f)))
                        .takeUnless { isRefreshing }

                FoodLoadingIndicator(
                    modifier = Modifier.hazeSource(hazeState),
                    text = text,
                    rotationDegrees = rotationDegrees,
                )
            }
        }
    }
) {
    Box {
        // ..
    }
}
```