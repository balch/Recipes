package org.balch.recipes.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

private val DarkColorScheme = darkColorScheme(
    primary = WarmOrangeDark,
    onPrimary = DarkText,
    primaryContainer = RichBrown,
    onPrimaryContainer = LightText,
    secondary = ForestGreen,
    onSecondary = DarkText,
    secondaryContainer = MutedSage,
    onSecondaryContainer = DarkText,
    tertiary = GoldenYellow,
    onTertiary = DarkText,
    background = DarkBackground,
    onBackground = LightText,
    surface = DarkSurface,
    onSurface = LightText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = MediumLightText,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    inverseSurface = LightSurface,
    inverseOnSurface = DarkText,
    inversePrimary = WarmOrange
)

private val LightColorScheme = lightColorScheme(
    primary = WarmOrange,
    onPrimary = Color.White,
    primaryContainer = DeepBrown,
    onPrimaryContainer = Color.White,
    secondary = FreshGreen,
    onSecondary = Color.White,
    secondaryContainer = SageGreen,
    onSecondaryContainer = Color.White,
    tertiary = AccentYellow,
    onTertiary = DarkText,
    background = LightBackground,
    onBackground = DarkText,
    surface = LightSurface,
    onSurface = DarkText,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = MediumText,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    inverseSurface = DarkSurface,
    inverseOnSurface = LightText,
    inversePrimary = WarmOrangeDark
)

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun RecipesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Limit FontScale so text fits when System Font size is cranked up
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = LocalDensity.current.density,
            fontScale = LocalDensity.current.fontScale.coerceIn(.8f, 1.10f)
        ),
        LocalHazeStyle provides HazeMaterials.thin(colorScheme.surface)
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}