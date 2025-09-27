package org.balch.recipes.ui.widgets

import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

private const val WEBVIEW_TAG = "recipeWebViewTag"

@Composable
fun WebViewScreen(
    modifier: Modifier = Modifier,
    url: String
) {
    Box(modifier = modifier.fillMaxSize()) {

        AndroidView(
            factory = { context ->
                FrameLayout(context).apply {
                    addView(
                        WebView(context).apply {
                            tag = WEBVIEW_TAG
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.allowContentAccess = true
                            settings.allowFileAccess = true
                            settings.javaScriptCanOpenWindowsAutomatically = true
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    )
                }
            },
            update = { frameLayout ->
                frameLayout.findViewWithTag<WebView>(WEBVIEW_TAG).apply {
                    loadUrl(url)
                }
            }
        )
    }}