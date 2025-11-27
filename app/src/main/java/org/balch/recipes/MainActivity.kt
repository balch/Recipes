package org.balch.recipes

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import org.balch.recipes.core.ai.GeminiKeyProvider
import org.balch.recipes.core.models.SearchType
import org.balch.recipes.core.navigation.NavigationRouter
import org.balch.recipes.core.navigation.rememberSharedTransitionDecorator
import org.balch.recipes.features.agent.AgentScreen
import org.balch.recipes.features.agent.AgentViewModel
import org.balch.recipes.features.agent.ai.RecipeMaestroAgent
import org.balch.recipes.features.agent.ai.RecipeMaestroConfig
import org.balch.recipes.features.details.DetailScreen
import org.balch.recipes.features.details.DetailsViewModel
import org.balch.recipes.features.ideas.IdeasScreen
import org.balch.recipes.features.info.InfoScreen
import org.balch.recipes.features.search.SearchScreen
import org.balch.recipes.features.search.SearchViewModel
import org.balch.recipes.ui.nav.isCompact
import org.balch.recipes.ui.nav.isLastScreen
import org.balch.recipes.ui.nav.peek
import org.balch.recipes.ui.nav.pop
import org.balch.recipes.ui.nav.push
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.utils.setEdgeToEdgeConfig
import org.balch.recipes.ui.widgets.AiFloatingToolbar
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var geminiKeyProvider: GeminiKeyProvider

    @Inject
    lateinit var recipeAgent: RecipeMaestroAgent

    @Inject
    lateinit var recipeMaestroConfig: RecipeMaestroConfig

    @Inject
    lateinit var navigationRouter: NavigationRouter

    private val topLevelRoutes by lazy {
        listOf(
            Ideas,
            Search(SearchType.Search("")),
            Info
        )
    }

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            MainContent()
        }
    }

    @OptIn(
        ExperimentalHazeMaterialsApi::class,
        ExperimentalHazeApi::class,
        ExperimentalSharedTransitionApi::class,
        ExperimentalMaterial3AdaptiveApi::class
    )
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    private fun MainContent() {
        val hazeState = rememberHazeState()

        val backStack = rememberNavBackStack(topLevelRoutes.first())
        val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

        val aiToolbarVisible by rememberAiFlotatingToolbarVisible(
            navKey = backStack.peek(),
            windowInfo = currentWindowAdaptiveInfo()
        )

        var previousVisibleIndex by remember { mutableIntStateOf(0) }
        var firstVisibleIndex by remember { mutableIntStateOf(0) }
        var showNavigationBar by remember { mutableStateOf(true) }
        LaunchedEffect(firstVisibleIndex) {
            showNavigationBar = firstVisibleIndex == 0 || firstVisibleIndex < previousVisibleIndex
            previousVisibleIndex = firstVisibleIndex
        }
        LaunchedEffect(Unit) {
            navigationRouter.navigationRoute.collect { navInfo ->
                backStack.push(navInfo.recipeRoute)
            }
        }

        val agentViewModel: AgentViewModel = hiltViewModel(
            viewModelStoreOwner = this@MainActivity
        )

        // override back button behavior to prevent closing the app when
        // there is only one screen and the nav bar is down
        BackHandler(enabled = backStack.isLastScreen() && !showNavigationBar) {
            showNavigationBar = true
        }

        RecipesTheme {
            Scaffold(
                bottomBar = {
                    AnimatedVisibility(
                        visible = showNavigationBar && backStack.peek() is TopLevelRoute,
                        enter = slideInVertically { it },
                        exit = slideOutVertically { it },
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .hazeEffect(state = hazeState, style = LocalHazeStyle.current) {
                                    HazeProgressive.verticalGradient(
                                        startIntensity = 1f,
                                        endIntensity = 0f,
                                    )
                                },
                        ) {
                            topLevelRoutes.forEach { topLevelRoute ->
                                val isSelected = topLevelRoute == backStack.peek()
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        if (backStack.peek() != topLevelRoute) {
                                            // Navigate to root first if not there
                                            if (backStack.peek() != topLevelRoutes.first()) {
                                                backStack.pop()
                                            }
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
                    }
                },
                floatingActionButtonPosition = FabPosition.End,
                floatingActionButton = {
                    if (aiToolbarVisible) {
                        AiFloatingToolbar(
                            modifier = Modifier
                                .then(
                                    if (backStack.peek() is DetailRoute) Modifier.offset(y = -(380).dp)
                                    else Modifier
                                ),
                            expanded = showNavigationBar,
                            onNavigateTo = { recipeRoute ->
                                navigationRouter.navigateTo(recipeRoute)
                            },
                            moodTintColor = agentViewModel.moodTintColor,
                        )
                    }
                }
            ) { innerPadding ->
                SharedTransitionLayout {
                    NavDisplay(
                        modifier = Modifier
                            .imePadding()
                            .hazeSource(state = hazeState),
                        backStack = backStack,
                        sceneStrategy = listDetailStrategy,
                        onBack = { backStack.pop() },
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                            rememberSharedTransitionDecorator()
                        ),

                        // Use the Activities `ViewModelStoreOwner` for ViewModels belonging to
                        // `TopLevelRoute` NavEntry routes. This allows screens to maintain their last
                        // states when navigating back to a top level screen
                        entryProvider = entryProvider {
                            entry<Ideas>(metadata = listPane()) {
                                IdeasScreen(
                                    viewModel = hiltViewModel(viewModelStoreOwner = this@MainActivity),
                                    onNavigateTo = { navigationRouter.navigateTo(it) },
                                    onScrollChange = { firstVisibleIndex = it },
                                )
                            }
                            entry<SearchRoute>(metadata = listPane()) { searchRoute ->
                                // Note: We need a new ViewModel for every new SearchViewModel instance.
                                //
                                // tl;dr: Make sure you use rememberViewModelStoreNavEntryDecorator()
                                // if you want a new ViewModel for each new navigation key instance.
                                val viewModel =
                                    hiltViewModel<SearchViewModel, SearchViewModel.Factory>(
                                        creationCallback = { factory ->
                                            factory.create(searchRoute.searchType)
                                        }
                                    )
                                SearchScreen(
                                    viewModel = viewModel,
                                    onBack = { backStack.pop() },
                                    onNavigateTo = { navigationRouter.navigateTo(it) },
                                    onScrollChange = { firstVisibleIndex = it },
                                )
                            }
                            entry<Search>(metadata = listPane()) {
                                val viewModel =
                                    hiltViewModel<SearchViewModel, SearchViewModel.Factory>(
                                        creationCallback = { factory ->
                                            factory.create(SearchType.Search(""))
                                        },
                                        viewModelStoreOwner = this@MainActivity
                                    )
                                SearchScreen(
                                    viewModel = viewModel,
                                    onBack = { backStack.pop() },
                                    onNavigateTo = { navigationRouter.navigateTo(it) },
                                    onScrollChange = { firstVisibleIndex = it },
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

                                DetailScreen(
                                    viewModel = viewModel,
                                    onBack = { backStack.pop() },
                                )
                            }
                            entry<AiChatScreen>(metadata = listPane()) { aiRoute ->
                                AgentScreen(
                                    viewModel = agentViewModel,
                                    onBack = { backStack.pop() },
                                )
                            }
                            entry<Info>(
                                metadata = ListDetailSceneStrategy.extraPane()
                            ) {
                                InfoScreen(
                                    viewModel = hiltViewModel(viewModelStoreOwner = this@MainActivity)
                                )
                            }
                        },
                    )
                }
            }
        }
    }


    @Composable
    fun rememberAiFlotatingToolbarVisible(
        navKey: NavKey?,
        windowInfo: WindowAdaptiveInfo
    ): State<Boolean> =
        remember(navKey, windowInfo) {
            mutableStateOf(
                when {
                    !windowInfo.isCompact() -> false
                    !geminiKeyProvider.isApiKeySet -> false
                    navKey == null -> false
                    navKey is DetailRoute -> true
                    navKey is SearchRoute -> true
                    navKey is Ideas -> true
                    navKey is Info -> true
                    else -> false
                }
            )
        }

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    fun listPane() =
        ListDetailSceneStrategy.listPane(
            detailPlaceholder = {
                Box {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "Choose an item from the list"
                    )
                }
            }
        )
}
