package org.balch.recipes.features.info

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.balch.recipes.ui.theme.WarmOrangeDark
import org.balch.recipes.ui.widgets.WebViewScreen

@Composable
fun InfoScreen() {
    val theme = if (isSystemInDarkTheme()) "dark" else "light"

    BoxWithConstraints(modifier = Modifier
        .safeDrawingPadding()
        .fillMaxSize()) {
        val boxHeight = with(LocalDensity.current) { constraints.maxHeight.toDp() }
        var topPartHeight by remember { mutableStateOf(boxHeight / 2) }

        Column {
            WebViewScreen(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topPartHeight),
                url = "https://github.com/balch/Recipes/tree/main/app/src/main/assets/code-recipes"
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            val newHeight =
                                topPartHeight + with(density) { dragAmount.toDp() }
                            val minHeight = boxHeight * 0.33f
                            val maxHeight = boxHeight * (1 - 0.33f)
                            topPartHeight = newHeight.coerceIn(minHeight, maxHeight)
                        }
                    },
                thickness = 8.dp,
                color = WarmOrangeDark
            )

            WebViewScreen(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(boxHeight - topPartHeight - 4.dp),
                url = "https://www.themealdb.com/api.php"
            )
        }
    }
}
