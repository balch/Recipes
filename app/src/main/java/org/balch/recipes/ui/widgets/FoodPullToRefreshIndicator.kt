package org.balch.recipes.ui.widgets

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.IndicatorBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun FoodPullToRefreshIndicator(
    modifier: Modifier = Modifier,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    innerPadding: PaddingValues,
    hazeState: HazeState,
    content: @Composable () -> Unit,
) {
    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = state,
        indicator = {
            IndicatorBox(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
                    .height(128.dp)
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
        content()
    }
}