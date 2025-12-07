package org.balch.recipes.ui.widgets

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import com.diamondedge.logging.logging
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private val logger = logging("OverscrollRevealBox")

/**
 * A container that reveals hidden content proportionally as user over-scrolls.
 * 
 * Uses NestedScrollConnection to intercept overscroll events from child scrollables.
 * The widget tracks drag proportionally (rubber band effect) and snaps back on release.
 * 
 * The reveal is triggered by swiping up at the bottom of a scrollable list (overscroll).
 * The widget appears from the bottom and slides up. Scrolling back down hides it.
 *
 * @param modifier Modifier to apply to the container
 * @param enabled Whether the reveal gesture is enabled
 * @param revealThresholdFraction The fraction of the reveal content height that needs to be
 *        dragged to fully reveal the content. Lower values = more sensitive. Default is 0.32f,
 *        meaning dragging 32% of the reveal content height will fully reveal it.
 * @param revealContent The content to reveal (slides up from bottom)
 * @param content The main child content (should contain a scrollable)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OverscrollRevealBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    revealThresholdFraction: Float = 0.32f,
    revealContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    // Track reveal content height
    var revealHeightPx by remember { mutableIntStateOf(0) }
    
    // Reveal amount: 0 = hidden, revealHeightPx = fully revealed
    val revealAmount = remember { Animatable(0f) }
    
    // Sensitivity multiplier: lower threshold = higher sensitivity
    val sensitivityMultiplier = 1f / revealThresholdFraction.coerceIn(0.1f, 1f)
    
    val nestedScrollConnection = remember(enabled, sensitivityMultiplier) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // If we're revealed and user scrolls in the opposite direction, consume to hide
                if (!enabled || revealHeightPx <= 0) return Offset.Zero
                
                val scrollY = available.y
                
                // When revealed (revealAmount > 0), scrolling down (away from the reveal) should hide the widget first
                if (revealAmount.value > 0f && source == NestedScrollSource.UserInput) {
                    // Scrolling down (positive delta) means user is moving away from the revealed content
                    val shouldHide = scrollY > 0f
                    
                    if (shouldHide) {
                        val consumeAmount = abs(scrollY).coerceAtMost(revealAmount.value)
                        val newReveal = (revealAmount.value - consumeAmount).coerceAtLeast(0f)
                        
                        scope.launch { revealAmount.snapTo(newReveal) }
                        
                        // Consume the amount we used to hide
                        return Offset(0f, consumeAmount)
                    }
                }
                return Offset.Zero
            }
            
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (!enabled || revealHeightPx <= 0) return Offset.Zero
                
                // available is what the child couldn't consume (overscroll)
                val overscroll = available.y
                
                if (source == NestedScrollSource.UserInput && abs(overscroll) > 0.5f) {
                    // For reverseLayout=false: overscroll < 0 means at bottom, swiping up to reveal (finger moves up, content tries to move down)
                    // For reverseLayout=true: overscroll < 0 also means swiping up at the "bottom" (which is visual top in reversed layout)
                    val shouldReveal = overscroll < 0f
                    
                    if (shouldReveal) {
                        // Apply cubic ease-in using standard Compose easing
                        val currentProgress = revealAmount.value / revealHeightPx
                        val easedProgress = EaseIn.transform(currentProgress)
                        // Base sensitivity at 0 is 30%, ramps up to 100% at full reveal
                        val easedMultiplier = sensitivityMultiplier * (0.3f + 0.7f * easedProgress)
                        val delta = abs(overscroll) * easedMultiplier
                        val newReveal = (revealAmount.value + delta).coerceAtMost(revealHeightPx.toFloat())
                        
                        scope.launch { revealAmount.snapTo(newReveal) }
                        return available // Consume the overscroll
                    }
                }
                return Offset.Zero
            }
            
            override suspend fun onPreFling(available: Velocity): Velocity {
                // Animate back to hidden on release
                if (revealAmount.value > 0f) {
                    revealAmount.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = 0.7f,
                            stiffness = 400f
                        )
                    )
                }
                return Velocity.Zero // Don't consume velocity - let child fling
            }
        }
    }
    
    // Disable default overscroll effect so we receive the events
    CompositionLocalProvider(LocalOverscrollFactory provides null) {
        Box(
            modifier = modifier.nestedScroll(nestedScrollConnection)
        ) {
            // Main content
            content()
            
            // Revealed content (slides up from below)
            if (enabled) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .onSizeChanged { size -> 
                            if (revealHeightPx != size.height && size.height > 0) {
                                logger.d { "Reveal content height: ${size.height}px" }
                                revealHeightPx = size.height
                            }
                        }
                        .offset { 
                            // Offset from bottom: revealHeightPx = fully hidden, 0 = fully visible
                            val offsetY = (revealHeightPx - revealAmount.value).roundToInt()
                            IntOffset(0, offsetY)
                        }
                ) {
                    revealContent()
                }
            }
        }
    }
}
