package org.balch.recipes.ui

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.unveilIn
import androidx.compose.animation.veilOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults.floatingToolbarVerticalNestedScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveComponentOverrideApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AdaptStrategy
import androidx.compose.material3.adaptive.layout.DockedEdge
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffoldDefaults
import androidx.compose.material3.adaptive.layout.rememberDragToResizeState
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy.Companion.detailPane
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy.Companion.listPane
import androidx.compose.material3.adaptive.navigation3.SupportingPaneSceneStrategy.Companion.extraPane
import androidx.compose.material3.adaptive.navigation3.SupportingPaneSceneStrategy.Companion.mainPane
import androidx.compose.material3.adaptive.navigation3.SupportingPaneSceneStrategy.Companion.supportingPane
import androidx.compose.material3.adaptive.navigation3.rememberSupportingPaneSceneStrategy
import androidx.compose.material3.adaptive.navigationsuite.LocalNavigationSuiteScaffoldOverride
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.navigation3.ui.NavDisplay
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.balch.recipes.AiChatScreen
import org.balch.recipes.DetailRoute
import org.balch.recipes.Ideas
import org.balch.recipes.Info
import org.balch.recipes.NavItemRoute
import org.balch.recipes.RecipeRoute
import org.balch.recipes.Search
import org.balch.recipes.SearchRoute
import org.balch.recipes.TopLevelRoute
import org.balch.recipes.core.models.SearchType
import org.balch.recipes.core.navigation.NavigationRouter
import org.balch.recipes.core.navigation.decorators.rememberSharedTransitionDecorator
import org.balch.recipes.core.navigation.decorators.rememberViewModelStoreRecipeRouteDecorator
import org.balch.recipes.core.navigation.isCompact
import org.balch.recipes.core.navigation.isLastScreen
import org.balch.recipes.core.navigation.peek
import org.balch.recipes.core.navigation.pop
import org.balch.recipes.core.navigation.popTo
import org.balch.recipes.core.navigation.push
import org.balch.recipes.di.rememberAppGraph
import org.balch.recipes.features.agent.AgentScreen
import org.balch.recipes.features.agent.AgentViewModel
import org.balch.recipes.features.details.DetailScreen
import org.balch.recipes.features.details.DetailsViewModel
import org.balch.recipes.features.ideas.IdeasScreen
import org.balch.recipes.features.info.InfoScreen
import org.balch.recipes.features.search.SearchScreen
import org.balch.recipes.features.search.SearchViewModel
import org.balch.recipes.ui.theme.RecipesTheme


private const val TOP_LEVEL_ROUTE_KEY_PREFIX = "TOP_LEVEL_ROUTE_KEY_"

private fun Any.isTopLevelRouteKey() = toString().startsWith(TOP_LEVEL_ROUTE_KEY_PREFIX)
private fun String.toTopLevelRoutKey() = TOP_LEVEL_ROUTE_KEY_PREFIX + this

@OptIn(
    ExperimentalHazeMaterialsApi::class,
    ExperimentalHazeApi::class,
    ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalMaterial3AdaptiveComponentOverrideApi::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalAnimationApi::class
)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainContent(
    isAgentEnabled: Boolean,
    navigationRouter: NavigationRouter,
) {

    val windowInfo = currentWindowAdaptiveInfo()
    val aiChatAvailableAsTopLevelRoute = isAgentEnabled && !windowInfo.isCompact()

    /**
     * The list of routes that are displayed in the navigation drawer.
     */
    val topLevelRoutes: List<NavItemRoute> = remember(aiChatAvailableAsTopLevelRoute) {
        listOfNotNull(
            Ideas,
            Search(SearchType.Search("")),
            Info,
            AiChatScreen.takeIf { aiChatAvailableAsTopLevelRoute }
        )
    }

    val backStack = rememberSerializable(
        windowInfo.isCompact(),
        serializer = NavBackStackSerializer(elementSerializer = NavKeySerializer())
    ) {
        val startRoutes = mutableStateListOf<RecipeRoute>(topLevelRoutes.first())
        NavBackStack(startRoutes)
    }

    val currentRoute by remember(backStack) {
        derivedStateOf { backStack.peek() }
    }

    /**
     * Conditionally adds nested scroll handling for showing/hiding bottom nav bar.
     */
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    fun Modifier.bottomNavNestedScroll(
        visible: Boolean,
        onShow: () -> Unit,
        onHide: () -> Unit,
    ): Modifier =
        if (currentRoute?.showBottomNav ?: false) {
            this then Modifier.floatingToolbarVerticalNestedScroll(
                expanded = visible,
                onExpand = onShow,
                onCollapse = onHide,
            )
        } else this

    val dragToResizeState = rememberDragToResizeState(
        dockedEdge = DockedEdge.Bottom,
        minSize = 120.dp,
    )

    val sceneStrategy = rememberSupportingPaneSceneStrategy<NavKey>(
        backNavigationBehavior = BackNavigationBehavior.PopUntilCurrentDestinationChange,
        adaptStrategies = SupportingPaneScaffoldDefaults.adaptStrategies(
            supportingPaneAdaptStrategy = AdaptStrategy.Levitate(
                alignment = Alignment.BottomEnd,
                dragToResizeState = dragToResizeState,
            ),
        ),
    )
    val hazeState = rememberHazeState()

    var bottomNavVisible by remember(currentRoute) {
        mutableStateOf(currentRoute?.showBottomNav ?: false)
    }
    // override back button behavior to prevent closing the app when
    // there is only one screen and the nav bar is down
    BackHandler(enabled = backStack.isLastScreen() && !bottomNavVisible) {
        bottomNavVisible = true
    }

    val aiToolbarVisible by rememberAiFlotatingToolbarVisible(
        navKey = currentRoute,
        windowInfo = windowInfo,
        isAgentEnabled = isAgentEnabled,
    )

    LaunchedEffect(Unit) {
        navigationRouter.navigationRoute.collect { navInfo ->
            // TODO - fix this for non compact screens
            backStack.push(navInfo.recipeRoute)
        }
    }

    val appGraph = rememberAppGraph()

    CompositionLocalProvider(
        LocalMetroViewModelFactory provides appGraph.metroViewModelFactory,
    ) {
        // Now metroViewModel() can access the factory
        val agentViewModel: AgentViewModel = metroViewModel()

        RecipesTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                CompositionLocalProvider(
                    LocalNavigationSuiteScaffoldOverride provides
                            MainNavSuiteScaffoldOverride(
                                hazeState = hazeState,
                                bottomNavVisible = bottomNavVisible,
                                aiToolbarVisible = aiToolbarVisible,
                                moodTintColor = agentViewModel.moodTintColor ?: Color.Transparent,
                                onNavigateTo = { route, isFromAgent ->
                                    navigationRouter.navigateTo(route, isFromAgent)
                                }
                            )
                ) {
                    NavigationSuiteScaffold(
                        containerColor = Color.Transparent,
                        navigationSuiteColors = NavigationSuiteDefaults.colors(
                            navigationBarContainerColor = Color.Transparent,
                            shortNavigationBarContainerColor = Color.Transparent,
                            navigationDrawerContainerColor = Color.Transparent,
                        ),
                        navigationSuiteItems = {
                            navigationSuiteItems(
                                topLevelRoutes = topLevelRoutes,
                                currentRoute = currentRoute,
                                navigationRouter = navigationRouter,
                                backStack = backStack,
                                isCompact = windowInfo.isCompact()
                            )
                        }
                    ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .bottomNavNestedScroll(
                                visible = bottomNavVisible,
                                onShow = { bottomNavVisible = true },
                                onHide = { bottomNavVisible = false },
                            )
                    ) {
                        SharedTransitionLayout {
                            val veilColor = MaterialTheme.colorScheme.surface
                            val matchSize = true
                            NavDisplay(
                                modifier = Modifier
                                    .hazeSource(hazeState)
                                    .imePadding(),
                                backStack = backStack,
                                transitionSpec = {
                                    ContentTransform(
                                        fadeIn(),
                                        fadeOut()
                                                + veilOut(targetColor = veilColor, matchParentSize = matchSize),
                                    )
                                },
                                popTransitionSpec = {
                                    ContentTransform(
                                        fadeIn()
                                                + unveilIn(initialColor = veilColor, matchParentSize = matchSize),
                                        fadeOut()
                                    )
                                },
                                predictivePopTransitionSpec = {
                                    ContentTransform(
                                        fadeIn(
                                            spring(
                                                dampingRatio = 1.0f,
                                                stiffness = 1600.0f,
                                            )
                                        ) + unveilIn(initialColor = veilColor, matchParentSize =  matchSize),
                                        scaleOut(targetScale = 0.7f),
                                    )
                                },
                                sceneStrategy = sceneStrategy,
                                onBack = { backStack.pop() },
                                entryDecorators = listOf(
                                    rememberSaveableStateHolderNavEntryDecorator(),
                                    rememberViewModelStoreRecipeRouteDecorator(
                                        createChildViewModel = { key -> !key.isTopLevelRouteKey() }
                                    ),
                                    rememberSharedTransitionDecorator()
                                ),
                                entryProvider =
                                    entryProviderRouter(
                                        agentViewModel = agentViewModel,
                                        navigationRouter = navigationRouter,
                                        isCompact = windowInfo.isCompact()
                                    )
                            )
                        }
                    }
                }
            }
            }
        }
    }
}

/**
 * Creates an entry provider function to route [NaveKey]s to corresponding
 * composable screens. The layer manages any metadata and biz logic necessary
 * to handle ViewModels and screen orchestration
 *
 * @param agentViewModel The ViewModel used for managing state and interactions of the Agent screen.
 * @param navigationRouter The router responsible for handling navigation between routes.
 * @return A function that takes a navigation key and returns a corresponding navigation entry.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun entryProviderRouter(
    agentViewModel: AgentViewModel,
    navigationRouter: NavigationRouter,
    isCompact: Boolean,
): (key: NavKey) -> NavEntry<NavKey> =
    entryProvider {
        entry<Ideas>(
            { "IdeasRoute".toTopLevelRoutKey() },
            metadata = mainPane() + listPane()
        ) {
            IdeasScreen(
                viewModel = assistedMetroViewModel(),
                onNavigateTo = { navigationRouter.navigateTo(it) },
            )
        }
        entry<SearchRoute>(
            metadata = mainPane() + listPane()
        ) { searchRoute ->
            val viewModel =
                assistedMetroViewModel<SearchViewModel, SearchViewModel.Factory> {
                    create(searchRoute.searchType)
                }
            SearchScreen(
                viewModel = viewModel,
                onNavigateTo = { navigationRouter.navigateTo(it) },
            )
        }
        entry<Search>(
            { "SearchRoute".toTopLevelRoutKey() },
            metadata = mainPane() + listPane()
        ) {
            val viewModel =
                assistedMetroViewModel<SearchViewModel, SearchViewModel.Factory> {
                    create(SearchType.Search(""))
                }
            SearchScreen(
                viewModel = viewModel,
                onNavigateTo = { navigationRouter.navigateTo(it) },
            )
        }
        entry<DetailRoute>(
            metadata = detailPane() + extraPane()
        ) { detailRoute ->
            val viewModel =
                assistedMetroViewModel<DetailsViewModel, DetailsViewModel.Factory> {
                    create(detailRoute.detailType)
                }

            DetailScreen(viewModel = viewModel)
        }
        entry<AiChatScreen>(
            { "AiChatScreen".toTopLevelRoutKey() },
            metadata = if (isCompact) mainPane() else supportingPane()
        ) {
            AgentScreen(viewModel = agentViewModel)
        }
        entry<Info>(
            { "InfoRoute".toTopLevelRoutKey() },
            metadata = mainPane() + extraPane()
        ) {
            InfoScreen(viewModel = metroViewModel())
        }
    }

/**
 * Configures the nav items displayed used for app navigation. How and where the
 * items are displayed are determined by the [NavigationSuiteScaffold] based on
 * Scene, NavigationSuite, and WindowAdaptiveInfo. For example, the items can
 * be displayed either in a BottomNav, Rail or Drawer depending on the window size.
 *
 * This method is responsible for backstack management logic when an item in the
 * navigation suite is selected.
 **/
private fun NavigationSuiteScope.navigationSuiteItems(
    topLevelRoutes: List<NavItemRoute>,
    currentRoute: RecipeRoute?,
    navigationRouter: NavigationRouter,
    backStack: NavBackStack<RecipeRoute>,
    isCompact: Boolean,
) {
    topLevelRoutes.forEach { route ->
        item(
            selected = route == currentRoute,
            onClick = {
                val navigateTo = if (isCompact) {
                    backStack.popTo(topLevelRoutes.first())
                    backStack.peek() != route
                } else {
                    if (backStack.peek() !is TopLevelRoute) {
                        backStack.pop()
                        true
                    } else {
                        backStack.peek() != route
                    }
                }
                if (navigateTo) {
                    navigationRouter.navigateTo(route)
                }
            },
            icon = {
                Icon(
                    imageVector = route.icon,
                    contentDescription = route.contentDescription
                )
            }
        )
    }
}

/**
 * Biz logic to determine when the floating toolbar should be visible.
 */
@Composable
fun rememberAiFlotatingToolbarVisible(
    navKey: RecipeRoute?,
    isAgentEnabled: Boolean,
    windowInfo: WindowAdaptiveInfo
): State<Boolean> {
    val visible = when {
        !windowInfo.isCompact() -> false
        !isAgentEnabled -> false
        navKey == null -> false
        navKey is DetailRoute -> true
        navKey is SearchRoute -> true
        navKey is Ideas -> true
        navKey is Info -> true
        else -> false
    }
    return rememberUpdatedState(visible)
}
