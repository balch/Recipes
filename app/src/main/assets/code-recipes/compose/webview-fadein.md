## Description

- Loading a `WebView` in Compose sometimes causes flashing after url loads
- Use a `mutableStateOf(true)` to track the `WebView` loading state
- Define `animateFloatAsState` to control the animation based on `isLoading` state
- Set the `alpha` according to the `animatedAlpha` state
   - Use `.graphicsLayer { alpha = animatedAlpha }` on `WebView`
   - Use `.graphicsLayer { alpha = 1f - animatedAlpha }` on loading indicator

## Code Snippet

```
@Composable
fun WebViewScreen(
    modifier: Modifier = Modifier,
    url: String
) {

    var isLoading by remember(url) { mutableStateOf(true) }
    val animatedAlpha: Float by animateFloatAsState(
        targetValue = if (isLoading) 0.25f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha"
    )

    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            FoodLoadingIndicator(
                modifier = Modifier.fillMaxSize()
                    .graphicsLayer { alpha = 1f - animatedAlpha },
            )
        }
        AndroidView(
            modifier = Modifier.fillMaxSize()
                .graphicsLayer { alpha = animatedAlpha },
            factory = { context ->
                WebView(context).apply {
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                        }
                    }
                    setBackgroundColor(Color.TRANSPARENT)
                    loadUrl(url)
                }
            },
            onReset = { webView ->
                webView.stopLoading()
                webView.loadUrl(url)
            },
            onRelease = WebView::destroy            
        )
    }
}                
```