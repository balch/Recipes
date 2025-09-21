package org.balch.recipes.features.info

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.balch.recipes.ui.widgets.WebViewScreen

@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
) {
    WebViewScreen(
        url = "https://github.com/balch/Recipes/blob/main/README.md"
    )
}
