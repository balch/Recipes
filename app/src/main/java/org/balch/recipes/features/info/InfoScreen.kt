package org.balch.recipes.features.info

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import org.balch.recipes.ui.theme.WarmOrangeDark
import org.balch.recipes.ui.widgets.WebViewWidget

@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
    viewModel: InfoViewModel,
) {
    val urls: List<String> by viewModel.uiState.collectAsState()
    InfoLayout(urls = urls)
}

@Composable
private fun InfoLayout(urls: List<String>) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val boxHeight = LocalWindowInfo.current.containerDpSize.height
            var topPartHeight by remember { mutableStateOf(boxHeight / 2) }

            Column {
                WebViewWidget(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(topPartHeight),
                    url = urls[0]
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

                WebViewWidget(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(boxHeight - topPartHeight - 4.dp),
                    url = urls[1]
                )
            }
        }
    }
}
