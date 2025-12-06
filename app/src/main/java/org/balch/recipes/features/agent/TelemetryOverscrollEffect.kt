package org.balch.recipes.features.agent

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

/**
 * An [OverscrollEffect] that exposes the vertical overscroll offset to drive
 * a custom visualization (e.g. telemetry widget reveal).
 *
 * It captures "pull up" overscroll (negative Y delta) which corresponds to
 * the bottom of the list in a standard layout, or the visual bottom in a reverse layout.
 */
@OptIn(ExperimentalFoundationApi::class)
class TelemetryOverscrollEffect(
    private val scope: CoroutineScope,
    private val maxOverscroll: Float, // Max magnitude (absolute value) of allowed overscroll
    private val enabled: Boolean = true,
    private val getOnNewOverscroll: () -> (Float) -> Unit
) : OverscrollEffect {

    // Internal animatable for spring animations (fling/settle)
    private val animatableOffset = Animatable(0f)

    // Synchronous backing field for scroll accumulation to avoid race conditions.
    // This serves as the source of truth during drag.
    // We sync animatableOffset to this value when dragging, and vice versa when animating.
    private var currentOverscroll = 0f

    override val isInProgress: Boolean
        get() = currentOverscroll != 0f

    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset
    ): Offset {
        if (!enabled) {
            return performScroll(delta)
        }

        // 1. Consume any pre-existing overscroll first (relaxing the pull)
        val sameDirection = sign(delta.y) == sign(currentOverscroll)
        var consumedByPreScroll = Offset.Zero

        // We only care about Y axis for this effect
        if (abs(currentOverscroll) > 0.5f && !sameDirection) {
            val prevOverscrollValue = currentOverscroll
            val newOverscrollValue = currentOverscroll + delta.y

            // If sign changed, we crossed zero. Clamp to 0 and consume the part that brought us to 0.
            if (sign(prevOverscrollValue) != sign(newOverscrollValue)) {
                updateOverscroll(0f)
                consumedByPreScroll = Offset(x = 0f, y = -prevOverscrollValue)
            } else {
                updateOverscroll(newOverscrollValue)
                consumedByPreScroll = delta.copy(x = 0f) // consumed all Y
            }
        }

        val leftForScroll = delta - consumedByPreScroll

        // 2. Perform the actual scroll
        val consumedByScroll = performScroll(leftForScroll)

        val overscrollDelta = leftForScroll - consumedByScroll

        // 3. Accumulate new overscroll if we hit the edge
        // We are interested in "pulling up" (delta.y < 0).
        if (source == NestedScrollSource.UserInput && abs(overscrollDelta.y) > 0.5f) {
             val resistance = 0.5f
             val addedOverscroll = overscrollDelta.y * resistance

             if (addedOverscroll < 0 || currentOverscroll < 0) {
                 val targetVal = currentOverscroll + addedOverscroll
                 // Clamp to range [-maxOverscroll, 0]
                 val clampedVal = targetVal.coerceIn(-maxOverscroll, 0f)

                 updateOverscroll(clampedVal)

                 // We consume the delta even if we are clamped (hitting the wall)
                 return consumedByPreScroll + consumedByScroll + overscrollDelta
             }
        }

        return consumedByPreScroll + consumedByScroll
    }

    private fun updateOverscroll(newValue: Float) {
        currentOverscroll = newValue
        getOnNewOverscroll()(newValue)
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity
    ) {
        val consumed = performFling(velocity)
        val remaining = velocity - consumed

        // Sync animatable to current synchronous state before starting animation
        animatableOffset.snapTo(currentOverscroll)

        // Spring back to 0
        animatableOffset.animateTo(
            targetValue = 0f,
            initialVelocity = remaining.y,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) {
             // Update synchronous state and callback on every frame
             currentOverscroll = value
             getOnNewOverscroll()(value)
        }
        // Ensure we end at exactly 0
        currentOverscroll = 0f
        getOnNewOverscroll()(0f)
    }

    override val node: DelegatableNode = object : Modifier.Node(), LayoutModifierNode {
        override fun MeasureScope.measure(
            measurable: Measurable,
            constraints: Constraints
        ): MeasureResult {
            val placeable = measurable.measure(constraints)
            return layout(placeable.width, placeable.height) {
                placeable.placeRelative(0, 0)
            }
        }
    }
}

@Composable
fun rememberTelemetryOverscrollEffect(
    maxOverscroll: Float,
    enabled: Boolean = true,
    onNewOverscroll: (Float) -> Unit
): TelemetryOverscrollEffect {
    val scope = rememberCoroutineScope()
    val currentOnNewOverscroll by rememberUpdatedState(onNewOverscroll)

    // Key on enabled as well, so if it changes we recreate/update the effect state
    return remember(scope, maxOverscroll, enabled) {
        TelemetryOverscrollEffect(scope, maxOverscroll, enabled) { currentOnNewOverscroll }
    }
}
