package org.balch.recipes.ui

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults.floatingToolbarVerticalNestedScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveComponentOverrideApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy.Companion.listPane
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigationsuite.LocalNavigationSuiteScaffoldOverride
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import org.balch.recipes.AiChatScreen
import org.balch.recipes.DetailRoute
import org.balch.recipes.Ideas
import org.balch.recipes.Info
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
import org.balch.recipes.core.navigation.rememberRecipeRouteBackStack
import org.balch.recipes.features.agent.AgentScreen
import org.balch.recipes.features.agent.AgentViewModel
import org.balch.recipes.features.details.DetailScreen
import org.balch.recipes.features.details.DetailsViewModel
import org.balch.recipes.features.ideas.IdeasScreen
import org.balch.recipes.features.info.InfoScreen
import org.balch.recipes.features.search.SearchScreen
import org.balch.recipes.features.search.SearchViewModel
import org.balch.recipes.ui.theme.RecipesTheme


private val TOP_LEVEL_ROUTES: List<TopLevelRoute> =
    listOf(
        Ideas,
        Search(SearchType.Search("")),
        Info
    )

private const val TOP_LEVEL_ROUTE_KEY_PREFIX = "TOP_LEVEL_ROUTE_KEY_"

private fun Any.isTopLevelRouteKey() = toString().startsWith(TOP_LEVEL_ROUTE_KEY_PREFIX)
private fun String.toTopLevelRoutKey() = TOP_LEVEL_ROUTE_KEY_PREFIX + this

@OptIn(
    ExperimentalHazeMaterialsApi::class,
    ExperimentalHazeApi::class,
    ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalMaterial3AdaptiveComponentOverrideApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainContent(
    isAgentEnabled: Boolean,
    navigationRouter: NavigationRouter,
) {

    val backStack = rememberRecipeRouteBackStack(TOP_LEVEL_ROUTES.first())

    /**
     * Conditionally adds nested scroll handling for showing/hiding bottom nav bar.
     */
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    fun Modifier.bottomNavNestedScroll(
        visible: Boolean,
        onShow: () -> Unit,
        onHide: () -> Unit,
    ): Modifier =
        if (backStack.peek()?.showBottomNav ?: false) {
            this then Modifier.floatingToolbarVerticalNestedScroll(
                expanded = visible,
                onExpand = onShow,
                onCollapse = onHide,
            )
        } else this

    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
    val windowInfo = currentWindowAdaptiveInfo()
    val hazeState = rememberHazeState()

    var bottomNavVisible by remember(backStack.peek()) {
        mutableStateOf(backStack.peek()?.showBottomNav ?: false)
    }
    // override back button behavior to prevent closing the app when
    // there is only one screen and the nav bar is down
    BackHandler(enabled = backStack.isLastScreen() && !bottomNavVisible) {
        bottomNavVisible = true
    }

    val aiToolbarVisible by rememberAiFlotatingToolbarVisible(
        navKey = backStack.peek(),
        windowInfo = windowInfo,
        isAgentEnabled = isAgentEnabled,
    )

    LaunchedEffect(Unit) {
        navigationRouter.navigationRoute.collect { navInfo ->
            backStack.push(navInfo.recipeRoute)
        }
    }

    val agentViewModel: AgentViewModel = hiltViewModel()

    RecipesTheme {
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
            val currentRoute = backStack.peek()
            NavigationSuiteScaffold(
                containerColor = Color.Transparent,
                navigationSuiteColors = NavigationSuiteDefaults.colors(
                    navigationBarContainerColor = Color.Transparent,
                    shortNavigationBarContainerColor = Color.Transparent,
                    navigationDrawerContainerColor = Color.Transparent,
                ),
                navigationSuiteItems = {
                    TOP_LEVEL_ROUTES.forEach { topLevelRoute ->
                        item(
                            selected = topLevelRoute == currentRoute,
                            onClick = {
                                backStack.popTo(TOP_LEVEL_ROUTES.first())
                                if (backStack.peek() != topLevelRoute) {
                                    navigationRouter.navigateTo(topLevelRoute)
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = topLevelRoute.icon,
                                    contentDescription = topLevelRoute.contentDescription
                                )
                            }
                        )
                    }
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
                        NavDisplay(
                            modifier = Modifier
                                .hazeSource(hazeState)
                                .imePadding(),
                            backStack = backStack,
                            sceneStrategy = listDetailStrategy,
                            onBack = { backStack.pop() },
                            entryDecorators = listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberViewModelStoreRecipeRouteDecorator(
                                    createChildViewModel = { key -> !key.isTopLevelRouteKey() }
                                ),
                                rememberSharedTransitionDecorator()
                            ),

                            entryProvider = entryProvider {
                                entry<Ideas>(
                                    { "IdeasRoute".toTopLevelRoutKey() },
                                    metadata = recipeListPane()
                                ) {
                                    IdeasScreen(
                                        viewModel = hiltViewModel(),
                                        onNavigateTo = { navigationRouter.navigateTo(it) },
                                    )
                                }
                                entry<SearchRoute>(metadata = recipeListPane()) { searchRoute ->
                                    val viewModel =
                                        hiltViewModel<SearchViewModel, SearchViewModel.Factory>(
                                            creationCallback = { factory ->
                                                factory.create(searchRoute.searchType)
                                            }
                                        )
                                    SearchScreen(
                                        viewModel = viewModel,
                                        onNavigateTo = { navigationRouter.navigateTo(it) },
                                    )
                                }
                                entry<Search>(
                                    { "SearchRoute".toTopLevelRoutKey() },
                                    metadata = recipeListPane()
                                ) {
                                    val viewModel =
                                        hiltViewModel<SearchViewModel, SearchViewModel.Factory>(
                                            creationCallback = { factory ->
                                                factory.create(SearchType.Search(""))
                                            },
                                        )
                                    SearchScreen(
                                        viewModel = viewModel,
                                        onNavigateTo = { navigationRouter.navigateTo(it) },
                                    )
                                }
                                entry<DetailRoute>(
                                    metadata = ListDetailSceneStrategy.detailPane()
                                ) { detailRoute ->
                                    val viewModel =
                                        hiltViewModel<DetailsViewModel, DetailsViewModel.Factory>(
                                            creationCallback = { factory ->
                                                factory.create(detailRoute.detailType)
                                            }
                                        )

                                    DetailScreen(viewModel = viewModel)
                                }
                                entry<AiChatScreen>(
                                    { "AiChatScreen".toTopLevelRoutKey() },
                                    metadata = listPane()
                                ) {
                                    AgentScreen(viewModel = agentViewModel)
                                }
                                entry<Info>(
                                    { "InfoRoute".toTopLevelRoutKey() },
                                    metadata = ListDetailSceneStrategy.extraPane()
                                ) {
                                    InfoScreen(viewModel = hiltViewModel())
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun rememberAiFlotatingToolbarVisible(
    navKey: RecipeRoute?,
    isAgentEnabled: Boolean,
    windowInfo: WindowAdaptiveInfo
): State<Boolean> =
    remember(navKey, windowInfo) {
        derivedStateOf {
            when {
                !windowInfo.isCompact() -> false
                !isAgentEnabled -> false
                navKey == null -> false
                navKey is DetailRoute -> true
                navKey is SearchRoute -> true
                navKey is Ideas -> true
                navKey is Info -> true
                else -> false
            }
        }
    }

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun recipeListPane() =
    listPane(
        detailPlaceholder = {
            Box {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "Choose an item from the list"
                )
            }
        }
    )

