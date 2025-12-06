package org.balch.recipes.ui.widgets

import androidx.compose.animation.core.Animatable
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
 * @param modifier Modifier to apply to the container
 * @param reverseLayout Whether the child scrollable uses reverseLayout=true.
 * @param enabled Whether the reveal gesture is enabled
 * @param revealContent The content to reveal (slides up from bottom)
 * @param content The main child content (should contain a scrollable)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OverscrollRevealBox(
    modifier: Modifier = Modifier,
    reverseLayout: Boolean = false,
    enabled: Boolean = true,
    revealContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    // Track reveal content height
    var revealHeightPx by remember { mutableIntStateOf(0) }
    
    // Reveal amount: 0 = hidden, revealHeightPx = fully revealed
    val revealAmount = remember { Animatable(0f) }
    
    val nestedScrollConnection = remember(reverseLayout, enabled) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // If we're revealed and user scrolls in the opposite direction, consume to hide
                if (!enabled || revealHeightPx <= 0) return Offset.Zero
                
                val scrollY = available.y
                
                // When revealed (revealAmount > 0), scrolling down should hide the widget first
                if (revealAmount.value > 0f && source == NestedScrollSource.UserInput) {
                    // For reverseLayout=true: scrolling down (positive delta) should hide
                    val shouldHide = if (reverseLayout) scrollY > 0f else scrollY < 0f
                    
                    if (shouldHide) {
                        val consumeAmount = abs(scrollY).coerceAtMost(revealAmount.value)
                        val newReveal = (revealAmount.value - consumeAmount).coerceAtLeast(0f)
                        
                        scope.launch { revealAmount.snapTo(newReveal) }
                        
                        // Consume the amount we used to hide
                        return if (reverseLayout) Offset(0f, consumeAmount) else Offset(0f, -consumeAmount)
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
                    // For reverseLayout=true: overscroll < 0 means swiping up at bottom (reveal)
                    val shouldReveal = if (reverseLayout) overscroll < 0f else overscroll > 0f
                    
                    if (shouldReveal) {
                        val delta = abs(overscroll) * 0.4f // Dampening for rubber-band feel
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
