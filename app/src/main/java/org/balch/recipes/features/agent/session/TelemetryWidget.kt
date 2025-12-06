package org.balch.recipes.features.agent.session

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Output
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.balch.recipes.ui.theme.RecipesTheme
import java.util.Locale

// Rich, vibrant bar colors
private val InputBarColor = Color(0xFF4FC3F7)  // Bright cyan
private val OutputBarColor = Color(0xFFBA68C8) // Rich purple
private val ToolBarColor = Color(0xFF26A69A)   // Teal green

/**
 * A composable that displays token usage statistics for the AI agent.
 * Shows compact stats on the left and vertical bar chart on the right.
 */
@Composable
fun TelemetryWidget(
    sessionUsage: SessionUsage,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    val animatedInputTokens by animateIntAsState(
        targetValue = sessionUsage.inputTokens.toInt(),
        animationSpec = tween(durationMillis = 500),
        label = "inputTokens"
    )
    val animatedOutputTokens by animateIntAsState(
        targetValue = sessionUsage.outputTokens.toInt(),
        animationSpec = tween(durationMillis = 500),
        label = "outputTokens"
    )
    val animatedToolCalls by animateIntAsState(
        targetValue = sessionUsage.toolCalls,
        animationSpec = tween(durationMillis = 500),
        label = "toolCalls"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val pulsingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsingAlpha"
    )

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(start = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CompactStatsColumn(
                modifier = Modifier,
                input = animatedInputTokens,
                output = animatedOutputTokens,
                toolCalls = animatedToolCalls,
                isLoading = isLoading,
                pulsingAlpha = pulsingAlpha,
            )

            // Calculate current session stats from live tokens minus history totals
            val historyInputTotal = sessionUsage.sessionHistory.sumOf { it.inputTokens }
            val historyOutputTotal = sessionUsage.sessionHistory.sumOf { it.outputTokens }
            val historyToolsTotal = sessionUsage.sessionHistory.sumOf { it.toolCalls }
            val currentSession = if (isLoading) {
                AgentSessionStats(
                    inputTokens = (sessionUsage.inputTokens.toInt() - historyInputTotal).coerceAtLeast(0),
                    outputTokens = (sessionUsage.outputTokens.toInt() - historyOutputTotal).coerceAtLeast(0),
                    toolCalls = (sessionUsage.toolCalls - historyToolsTotal).coerceAtLeast(0)
                )
            } else null

            VerticalBarChart(
                sessionHistory = sessionUsage.sessionHistory,
                currentSession = currentSession,
                isLoading = isLoading,
                maxBars = 8,
                modifier = Modifier.weight(0.6f)
                    .padding(end = 8.dp, bottom = 16.dp, top = 4.dp)
             )
        }
    }
}

/**
 * Compact stats showing all 4 metrics in a single row.
 */
@Composable
private fun CompactStatsColumn(
    input: Int,
    output: Int,
    toolCalls: Int,
    isLoading: Boolean,
    pulsingAlpha: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceAround
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SatelliteAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Telemetry",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier.padding(start = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CompactStatItem(
                icon = Icons.AutoMirrored.Filled.Input,
                value = input,
                color = InputBarColor,
                isLoading = isLoading,
                pulsingAlpha = pulsingAlpha
            )
            CompactStatItem(
                icon = Icons.Filled.Output,
                value = output,
                color = OutputBarColor,
                isLoading = isLoading,
                pulsingAlpha = pulsingAlpha
            )
            CompactStatItem(
                icon = Icons.Default.Build,
                value = toolCalls,
                color = ToolBarColor,
                isLoading = isLoading,
                pulsingAlpha = pulsingAlpha
            )
        }
    }
}

@Composable
private fun CompactStatItem(
    icon: ImageVector,
    value: Int,
    color: Color,
    isLoading: Boolean = false,
    pulsingAlpha: Float = 1f,
) {
    val iconAlpha = if (isLoading) pulsingAlpha else 1f
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color.copy(alpha = iconAlpha),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = formatTokenCount(value),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Vertical bar chart showing the last N sessions.
 * When loading, also shows the current in-progress session.
 */
@Suppress("SameParameterValue")
@Composable
private fun VerticalBarChart(
    sessionHistory: List<AgentSessionStats>,
    currentSession: AgentSessionStats?,
    isLoading: Boolean,
    maxBars: Int,
    modifier: Modifier = Modifier,
) {
    // Build display list: history + current session if loading
    val displayBars = remember(sessionHistory, currentSession, maxBars) {
        val bars = sessionHistory.takeLast(if (currentSession != null) maxBars - 1 else maxBars).toMutableList()
        if (currentSession != null) {
            bars.add(currentSession)
        }
        bars
    }

    // Always render the row to reserve space, even if empty
    Row(
        modifier = modifier.fillMaxHeight(),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
    ) {
        if (displayBars.isEmpty()) {
            // Placeholder to reserve space
            Box(modifier = Modifier.width(16.dp))
        } else {
            // Calculate max value for scaling (with 20% headroom)
            val maxValue = remember(displayBars) {
                val maxTokens = displayBars.maxOfOrNull { it.inputTokens + it.outputTokens } ?: 0
                (maxTokens * 1.2f).coerceAtLeast(120f)
            }

            displayBars.forEachIndexed { index, stats ->
                VerticalSessionBar(
                    stats = stats,
                    maxValue = maxValue,
                    isLatest = index == displayBars.lastIndex,
                    isLoading = isLoading && index == displayBars.lastIndex,
                    barWidth = 16.dp,
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    }
}

/**
 * A single vertical bar representing one session.
 * Shows stacked segments for input (bottom), output (middle), and tools (top).
 */
@Composable
private fun VerticalSessionBar(
    stats: AgentSessionStats,
    maxValue: Float,
    isLatest: Boolean,
    isLoading: Boolean,
    barWidth: Dp,
    modifier: Modifier = Modifier,
) {
    // Animate bar height
    val animatedScale by animateFloatAsState(
        targetValue = if (isLatest && isLoading) 0.7f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "barScale"
    )

    // Pulsing animation for loading
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val pulsingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val alpha = if (isLatest && isLoading) pulsingAlpha else 1f

    val totalTokens = stats.inputTokens + stats.outputTokens
    val totalFraction = if (maxValue > 0) (totalTokens / maxValue).coerceIn(0.1f, 1f) else 0.1f

    // Calculate proportions for each segment
    val inputProportion = if (totalTokens > 0) stats.inputTokens.toFloat() / totalTokens else 0.5f
    val outputProportion = if (totalTokens > 0) stats.outputTokens.toFloat() / totalTokens else 0.5f
    val hasTools = stats.toolCalls > 0

    Box(
        modifier = modifier
            .width(barWidth),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(totalFraction * animatedScale)
                .width(barWidth)
                .shadow(
                    elevation = 3.dp,
                    shape = RoundedCornerShape(barWidth / 2),
                    ambientColor = Color.Black.copy(alpha = 0.2f),
                    spotColor = Color.Black.copy(alpha = 0.25f)
                )
                .clip(RoundedCornerShape(barWidth / 2)),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Tool segment (top) - small fixed height if tools used
            if (hasTools) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.1f.coerceAtLeast(0.01f))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    ToolBarColor.copy(alpha = alpha),
                                    ToolBarColor.copy(alpha = alpha * 0.8f)
                                )
                            )
                        )
                )
            }

            // Output segment (middle)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(outputProportion.coerceAtLeast(0.01f))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                OutputBarColor.copy(alpha = alpha),
                                OutputBarColor.copy(alpha = alpha * 0.8f)
                            )
                        )
                    )
            )

            // Input segment (bottom)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(inputProportion.coerceAtLeast(0.01f))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                InputBarColor.copy(alpha = alpha),
                                InputBarColor.copy(alpha = alpha * 0.8f)
                            )
                        )
                    )
            )
        }

        // Highlight overlay for 3D effect
        Box(
            modifier = Modifier
                .fillMaxHeight(totalFraction * animatedScale)
                .width(barWidth / 3)
                .align(Alignment.BottomStart)
                .clip(RoundedCornerShape(topStart = barWidth / 2, bottomStart = barWidth / 2))
                .background(Color.White.copy(alpha = 0.25f))
        )
    }
}

/**
 * Formats token count with K suffix for thousands
 */
private fun formatTokenCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format(Locale.getDefault(), "%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(Locale.getDefault(),"%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}

// ============================================
// Previews
// ============================================

@Preview(showBackground = true)
@Composable
private fun TelemetryWidgetPreview() {
    RecipesTheme {
        TelemetryWidget(
            sessionUsage = SessionUsage(
                inputTokens = 1250,
                outputTokens = 3750,
                toolCalls = 5,
                sessionHistory = listOf(
                    AgentSessionStats(inputTokens = 200, outputTokens = 800, toolCalls = 1),
                    AgentSessionStats(inputTokens = 350, outputTokens = 1200, toolCalls = 2),
                    AgentSessionStats(inputTokens = 300, outputTokens = 900, toolCalls = 0),
                    AgentSessionStats(inputTokens = 400, outputTokens = 850, toolCalls = 1),
                    AgentSessionStats(inputTokens = 200, outputTokens = 800, toolCalls = 1),
                    AgentSessionStats(inputTokens = 350, outputTokens = 1200, toolCalls = 2),
                    AgentSessionStats(inputTokens = 300, outputTokens = 900, toolCalls = 0),
                    AgentSessionStats(inputTokens = 400, outputTokens = 850, toolCalls = 1),
                    AgentSessionStats(inputTokens = 200, outputTokens = 800, toolCalls = 1),
                    AgentSessionStats(inputTokens = 350, outputTokens = 1200, toolCalls = 2),
                    AgentSessionStats(inputTokens = 300, outputTokens = 900, toolCalls = 0),
                    AgentSessionStats(inputTokens = 400, outputTokens = 850, toolCalls = 1),
                )
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TelemetryWidgetEmptyPreview() {
    RecipesTheme {
        TelemetryWidget(
            sessionUsage = SessionUsage.EMPTY,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TelemetryWidgetLoadingPreview() {
    RecipesTheme {
        TelemetryWidget(
            sessionUsage = SessionUsage(
                inputTokens = 500,
                outputTokens = 1500,
                toolCalls = 2,
                sessionHistory = listOf(
                    AgentSessionStats(inputTokens = 250, outputTokens = 750, toolCalls = 1),
                    AgentSessionStats(inputTokens = 250, outputTokens = 750, toolCalls = 1),
                )
            ),
            isLoading = true,
            modifier = Modifier.padding(16.dp)
        )
    }
}
