package org.balch.recipes.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveComponentOverrideApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuite
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldOverride
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldOverrideScope
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldValue
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import org.balch.recipes.RecipeRoute
import org.balch.recipes.ui.widgets.AiFloatingToolbar

private val NoWindowInsets = WindowInsets(0, 0, 0, 0)

/**
 * [NavigationSuiteScaffoldOverride] that allows the Recipe App to use
 * haze in the bottom navigation bar.
 */
@OptIn(ExperimentalMaterial3AdaptiveComponentOverrideApi::class)
class MainNavSuiteScaffoldOverride(
    private val hazeState: HazeState,
    private val bottomNavVisible: Boolean,
    private val aiToolbarVisible: Boolean = true,
    private val moodTintColor: Color,
    private val onNavigateTo: (RecipeRoute, Boolean) -> Unit,
) : NavigationSuiteScaffoldOverride {
    @Composable
    override fun NavigationSuiteScaffoldOverrideScope.NavigationSuiteScaffold() {
        if (layoutType == NavigationSuiteType.NavigationBar) {
            Box(Modifier.fillMaxSize()) {
                content()
                Column(
                    modifier = Modifier.fillMaxSize().align(Alignment.BottomCenter),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    if (aiToolbarVisible) {
                        AiFloatingToolbar(
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(16.dp),
                            expanded = bottomNavVisible,
                            onNavigateTo = { recipeRoute -> onNavigateTo(recipeRoute, true) },
                            moodTintColor = moodTintColor,
                        )
                    }
                    AnimatedVisibility(
                        visible = bottomNavVisible && state.currentValue == NavigationSuiteScaffoldValue.Visible,
                        enter = slideInVertically { it },
                        exit = slideOutVertically { it },
                    ) {
                        NavigationSuite(
                            modifier = Modifier
                                .hazeEffect(state = hazeState, style = LocalHazeStyle.current) {
                                    HazeProgressive.verticalGradient(
                                        startIntensity = 1f,
                                        endIntensity = 0f,
                                    )
                                },
                            layoutType = layoutType,
                            colors = navigationSuiteColors,
                            content = navigationSuiteItems,
                        )
                    }
                }
            }
        } else {
            NavigationSuiteScaffoldLayout(
                navigationSuite = {
                    NavigationSuite(
                        modifier = Modifier
                            .hazeEffect(state = hazeState, style = LocalHazeStyle.current) {
                                HazeProgressive.verticalGradient(
                                    startIntensity = 1f,
                                    endIntensity = 0f,
                                )
                            },
                        layoutType = layoutType,
                        colors = navigationSuiteColors,
                        content = navigationSuiteItems,
                    )
                },
                state = state,
                layoutType = layoutType,
                content = {
                    Box(
                        Modifier
                            .imePadding()
                            .consumeWindowInsets(
                            if (
                                state.currentValue == NavigationSuiteScaffoldValue.Hidden &&
                                !state.isAnimating
                            ) {
                                NoWindowInsets
                            } else {
                                when (layoutType) {
                                    NavigationSuiteType.NavigationBar ->
                                        NavigationBarDefaults.windowInsets.only(
                                            WindowInsetsSides.Bottom
                                        )

                                    NavigationSuiteType.NavigationRail ->
                                        NavigationRailDefaults.windowInsets.only(
                                            WindowInsetsSides.Start
                                        )

                                    NavigationSuiteType.NavigationDrawer ->
                                        DrawerDefaults.windowInsets.only(WindowInsetsSides.Start)

                                    else -> NoWindowInsets
                                }
                            }
                        )
                    ) {
                        content()
                    }
                },
            )
        }
    }
}