package org.balch.recipes.ui.widgets

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.OverscrollFactory
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import com.diamondedge.logging.logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private val logger = logging("OverscrollRevealBox")

/**
 * A container that reveals hidden content proportionally as user over-scrolls.
 * 
 * Uses a custom [OverscrollFactory] to intercept overscroll events while still
 * providing subtle stretch feedback on the main content.
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
    
    // Stretch amount for visual feedback on main content
    val stretchAmount = remember { Animatable(0f) }
    
    // Create custom overscroll factory that drives both reveal and stretch
    val revealOverscrollFactory = remember(reverseLayout, enabled) {
        RevealOverscrollFactory(
            scope = scope,
            reverseLayout = reverseLayout,
            enabled = enabled,
            revealAmount = revealAmount,
            stretchAmount = stretchAmount,
            getRevealHeight = { revealHeightPx }
        )
    }
    
    // Provide our custom overscroll factory to child scrollables
    CompositionLocalProvider(LocalOverscrollFactory provides revealOverscrollFactory) {
        Box(modifier = modifier) {
            // Main content with subtle stretch effect
            Box(
                modifier = Modifier.graphicsLayer {
                    // Apply subtle stretch: scale up slightly when overscrolling
                    val stretch = stretchAmount.value / 1000f // Normalize to small scale factor
                    scaleX = 1f + stretch * 0.02f
                    scaleY = 1f + stretch * 0.02f
                }
            ) {
                content()
            }
            
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

/**
 * Custom OverscrollFactory that creates RevealOverscrollEffect instances.
 */
@OptIn(ExperimentalFoundationApi::class)
private class RevealOverscrollFactory(
    private val scope: CoroutineScope,
    private val reverseLayout: Boolean,
    private val enabled: Boolean,
    private val revealAmount: Animatable<Float, *>,
    private val stretchAmount: Animatable<Float, *>,
    private val getRevealHeight: () -> Int
) : OverscrollFactory {
    
    override fun createOverscrollEffect(): OverscrollEffect {
        return RevealOverscrollEffect(
            scope = scope,
            reverseLayout = reverseLayout,
            enabled = enabled,
            revealAmount = revealAmount,
            stretchAmount = stretchAmount,
            getRevealHeight = getRevealHeight
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RevealOverscrollFactory) return false
        return reverseLayout == other.reverseLayout && enabled == other.enabled
    }
    
    override fun hashCode(): Int {
        return 31 * reverseLayout.hashCode() + enabled.hashCode()
    }
}

/**
 * Custom OverscrollEffect that drives both reveal animation and stretch feedback.
 */
@OptIn(ExperimentalFoundationApi::class)
private class RevealOverscrollEffect(
    private val scope: CoroutineScope,
    private val reverseLayout: Boolean,
    private val enabled: Boolean,
    private val revealAmount: Animatable<Float, *>,
    private val stretchAmount: Animatable<Float, *>,
    private val getRevealHeight: () -> Int
) : OverscrollEffect {
    
    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset
    ): Offset {
        // Let the scrollable consume what it can first
        val consumed = performScroll(delta)
        
        // Calculate leftover (overscroll)
        val overscroll = delta - consumed
        val overscrollY = overscroll.y
        val revealHeight = getRevealHeight()
        
        if (source == NestedScrollSource.UserInput && abs(overscrollY) > 0.5f && revealHeight > 0) {
            // Determine if this scroll direction should reveal
            // reverseLayout=true: negative overscrollY (swiping up at bottom) = reveal
            val shouldReveal = if (reverseLayout) overscrollY < 0f else overscrollY > 0f
            
            if (shouldReveal && enabled) {
                // Drive reveal animation
                val revealDelta = abs(overscrollY) * 0.4f
                val newReveal = (revealAmount.value + revealDelta).coerceAtMost(revealHeight.toFloat())
                scope.launch { revealAmount.snapTo(newReveal) }
                
                // Also apply subtle stretch for visual feedback
                val newStretch = (stretchAmount.value + abs(overscrollY) * 0.3f).coerceAtMost(100f)
                scope.launch { stretchAmount.snapTo(newStretch) }
                
                return overscroll // Consume the overscroll
            } else if (revealAmount.value > 0f) {
                // User scrolling back - hide the reveal
                val hideAmount = abs(overscrollY).coerceAtMost(revealAmount.value)
                val newReveal = (revealAmount.value - hideAmount).coerceAtLeast(0f)
                scope.launch { revealAmount.snapTo(newReveal) }
                return Offset(0f, if (reverseLayout) hideAmount else -hideAmount)
            }
        }
        
        return Offset.Zero
    }
    
    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity
    ) {
        performFling(velocity)
        
        // Animate back to resting state
        if (revealAmount.value > 0f) {
            revealAmount.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)
            )
        }
        
        if (stretchAmount.value > 0f) {
            stretchAmount.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f)
            )
        }
    }
    
    override val isInProgress: Boolean
        get() = revealAmount.value > 0f || stretchAmount.value > 0f
    
    // Node for applying visual effect - we handle this via graphicsLayer in parent
    override val node: DelegatableNode = object : Modifier.Node(), LayoutModifierNode {
        override fun MeasureScope.measure(
            measurable: Measurable,
            constraints: Constraints
        ): MeasureResult {
            val placeable = measurable.measure(constraints)
            return layout(placeable.width, placeable.height) {
                placeable.place(0, 0)
            }
        }
    }
}
