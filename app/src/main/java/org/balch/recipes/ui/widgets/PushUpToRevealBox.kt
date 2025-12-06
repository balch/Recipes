@file:OptIn(ExperimentalAnimationApi::class)

package org.balch.recipes.ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.diamondedge.logging.logging
import kotlinx.coroutines.delay

private val logger = logging("PushUpToRevealBox")

/**
 * A container that handles the "push up from bottom to reveal" gesture.
 * It coordinates with a child scrollable (like LazyColumn) via NestedScrollConnection.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param enabled Whether the gesture is enabled.
 * @param autoCollapseDelayMs Time in ms to wait before auto-collapsing. 0 to disable.
 * @param revealContent The content to be revealed.
 * @param content The main content.
 */
@Composable
fun PushUpToRevealBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    autoCollapseDelayMs: Long = 3000,
    revealContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    // Auto-collapse after delay when revealed
    LaunchedEffect(isVisible) {
        if (isVisible && autoCollapseDelayMs > 0 && enabled) {
            logger.d { "AUTO_COLLAPSE: Starting ${autoCollapseDelayMs}ms timer" }
            delay(autoCollapseDelayMs)
            logger.d { "AUTO_COLLAPSE: Timer expired, hiding" }
            isVisible = false
        }
    }
    
    // NestedScrollConnection to intercept over-scroll
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            // Called after child consumes scroll - we get the leftover
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // available.y < 0 means user is scrolling up but list can't scroll more (at bottom)
                // In reverse layout, this is when the newest content is visible and user swipes up
                if (available.y < 0 && source == NestedScrollSource.UserInput && enabled && !isVisible) {
                    // Threshold to trigger reveal (prevent accidental triggers)
                    if (available.y < -2f) {
                        logger.v { "NESTED_SCROLL: Triggering reveal! available.y=${available.y}" }
                        isVisible = true
                        return available // Consume the scroll
                    }
                }
                return Offset.Zero
            }
        }
    }

    Box(
        modifier = modifier.nestedScroll(nestedScrollConnection)
    ) {
        // Main Content
        content()
        
        // Revealed Content (slides up from bottom)
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically { height -> height } + fadeIn(),
            exit = slideOutVertically { height -> height } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                revealContent()
            }
        }
    }
}
