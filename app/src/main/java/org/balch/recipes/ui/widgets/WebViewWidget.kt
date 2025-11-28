package org.balch.recipes.ui.widgets

import android.graphics.Color
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WebViewWidget(
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
                    // Fix for crash on tablets (RenderThread SIGSEGV / Cache directory access)
                    setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)

                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true

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