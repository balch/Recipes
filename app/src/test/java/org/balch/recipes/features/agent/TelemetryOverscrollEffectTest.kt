package org.balch.recipes.features.agent

import androidx.compose.animation.core.Animatable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class TelemetryOverscrollEffectTest {

    @Test
    fun `applyToScroll accumulates negative overscroll when enabled`() = runTest {
        var reportedOverscroll = 0f
        val effect = TelemetryOverscrollEffect(
            maxOverscroll = 200f,
            enabled = true,
            getOnNewOverscroll = { { offset -> reportedOverscroll = offset } }
        )

        val delta = Offset(0f, -20f) // Pull up

        // Mock performScroll to return Zero (meaning we are at the edge)
        val consumed = effect.applyToScroll(
            delta = delta,
            source = NestedScrollSource.UserInput,
            performScroll = { Offset.Zero }
        )

        // Resistance is 0.5, so -20f input -> -10f overscroll
        // And we consume the original delta + overscroll delta?
        // Logic: consumedByScroll=0. left=delta. overscrollDelta=delta.
        // addedOverscroll = -10f.
        // returns consumedByPreScroll(0) + consumedByScroll(0) + overscrollDelta(-20f) = -20f.

        assertEquals(-10f, reportedOverscroll, 0.1f)
        assertEquals(delta, consumed)
    }

    @Test
    fun `applyToScroll does not accumulate positive overscroll`() = runTest {
        var reportedOverscroll = 0f
        val effect = TelemetryOverscrollEffect(
            maxOverscroll = 200f,
            enabled = true,
            getOnNewOverscroll = { { offset -> reportedOverscroll = offset } }
        )

        val delta = Offset(0f, 20f) // Pull down

        val consumed = effect.applyToScroll(
            delta = delta,
            source = NestedScrollSource.UserInput,
            performScroll = { Offset.Zero } // At edge
        )

        // Should not accumulate positive overscroll for this widget
        assertEquals(0f, reportedOverscroll, 0.0f)
        assertEquals(Offset.Zero, consumed) // Nothing consumed
    }

    @Test
    fun `applyToScroll consumes pre-scroll to relax overscroll`() = runTest {
        var reportedOverscroll = 0f
        val effect = TelemetryOverscrollEffect(
            maxOverscroll = 200f,
            enabled = true,
            getOnNewOverscroll = { { offset -> reportedOverscroll = offset } }
        )

        // First apply negative overscroll
        effect.applyToScroll(
            delta = Offset(0f, -40f), // -> -20f overscroll
            source = NestedScrollSource.UserInput,
            performScroll = { Offset.Zero }
        )
        assertEquals(-20f, reportedOverscroll, 0.1f)

        // Now pull down (positive) to relax
        val relaxDelta = Offset(0f, 10f)
        val consumed = effect.applyToScroll(
            delta = relaxDelta,
            source = NestedScrollSource.UserInput,
            performScroll = { Offset.Zero }
        )

        // Should increase overscroll from -20f to -10f
        assertEquals(-10f, reportedOverscroll, 0.1f)
        // Should consume the relax delta
        assertEquals(relaxDelta, consumed)
    }

    @Test
    fun `applyToScroll respects enabled flag`() = runTest {
        var reportedOverscroll = 0f
        val effect = TelemetryOverscrollEffect(
            maxOverscroll = 200f,
            enabled = false, // Disabled
            getOnNewOverscroll = { { offset -> reportedOverscroll = offset } }
        )

        val delta = Offset(0f, -20f)

        val consumed = effect.applyToScroll(
            delta = delta,
            source = NestedScrollSource.UserInput,
            performScroll = { Offset.Zero }
        )

        assertEquals(0f, reportedOverscroll, 0.0f)
        assertEquals(Offset.Zero, consumed)
    }
}
