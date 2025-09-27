package org.balch.recipes.features.info

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.balch.recipes.ui.widgets.WebViewScreen

@Composable
fun InfoScreen(
) {
    val theme = if (isSystemInDarkTheme()) "dark" else "light"
    WebViewScreen(
        modifier = Modifier.safeDrawingPadding(),
        url = "https://jay-balcher-resume-792247382464.us-west1.run.app/?theme=$theme&topbar=false&deepThoughts=true"
    )
}
