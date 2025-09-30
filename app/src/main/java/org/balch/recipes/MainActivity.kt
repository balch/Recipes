package org.balch.recipes

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.scene.rememberSceneSetupNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import org.balch.recipes.core.models.DetailType
import org.balch.recipes.core.models.SearchType
import org.balch.recipes.features.details.DetailScreen
import org.balch.recipes.features.details.DetailsViewModel
import org.balch.recipes.features.ideas.IdeasScreen
import org.balch.recipes.features.info.InfoScreen
import org.balch.recipes.features.search.SearchScreen
import org.balch.recipes.features.search.SearchViewModel
import org.balch.recipes.ui.nav.BackstackManager
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.utils.setEdgeToEdgeConfig
import javax.inject.Inject


private val TOP_LEVEL_ROUTES : List<TopLevelRoute> =
    listOf(Ideas, Search(SearchType.Search("")), Info)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var backstackManager: BackstackManager


    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            MainContent()
        }
    }

    @OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalHazeApi::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    private fun MainContent() {
        val hazeState = rememberHazeState()

        var previousVisibleIndex by remember { mutableIntStateOf(0) }
        var firstVisibleIndex by remember { mutableIntStateOf(0) }
        var showNavigationBar by remember { mutableStateOf(true) }
        LaunchedEffect(firstVisibleIndex) {
            showNavigationBar = firstVisibleIndex == 0 || firstVisibleIndex < previousVisibleIndex
            previousVisibleIndex = firstVisibleIndex
        }

        RecipesTheme {
            Scaffold(
                bottomBar = {
                    AnimatedVisibility(
                        visible = showNavigationBar && backstackManager.peek() is TopLevelRoute,
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
                            TOP_LEVEL_ROUTES.forEach { topLevelRoute ->
                                val isSelected = topLevelRoute == backstackManager.peek()
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        // pop the current screen off the backstack if it not the root
                                        if (backstackManager.peek() != TOP_LEVEL_ROUTES[0]) {
                                            backstackManager.pop()
                                        }
                                        // push the new route onto the backstack
                                        if (backstackManager.peek() != topLevelRoute) {
                                            backstackManager.push(topLevelRoute)
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
                }
            ) { innerPadding ->
                NavDisplay(
                    modifier = Modifier.hazeSource(state = hazeState),
                    backStack = backstackManager.backstack,
                    onBack = { repeat(it) { backstackManager.pop() } },
                    // In order to add the `ViewModelStoreNavEntryDecorator` (see comment below for why)
                    // we also need to add the default `NavEntryDecorator`s as well. These provide
                    // extra information to the entry's content to enable it to display correctly
                    // and save its state.
                    entryDecorators = listOf(
                        rememberSceneSetupNavEntryDecorator(),
                        rememberSavedStateNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = entryProvider {
                        entry<Ideas> {
                            IdeasScreen(
                                onCategoryClick = { category ->
                                    backstackManager.push(
                                        SearchRoute(SearchType.Category(category.name))
                                    )
                                },
                                onAreaClick = { area ->
                                    backstackManager.push(
                                        SearchRoute(SearchType.Area(area.name))
                                    )
                                },
                                onIngredientClick = { ingredient ->
                                    backstackManager.push(
                                        SearchRoute(SearchType.Ingredient(ingredient.name))
                                    )
                                },
                                onCodeRecipeClick = { codeRecipe ->
                                    backstackManager.push(
                                        DetailRoute(DetailType.CodeRecipeContent(codeRecipe))
                                    )
                                },
                                onScrollChange = { firstVisibleIndex = it }
                            )
                        }
                        entry<SearchRoute> { searchRoute ->
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
                                onBack = { backstackManager.pop() },
                                onMealLookup = { id ->
                                    backstackManager.push(
                                        DetailRoute(
                                            DetailType.MealLookup(
                                                id
                                            )
                                        )
                                    )
                                },
                                onScrollChange = { firstVisibleIndex = it },
                                onRandomMeal = { backstackManager.push(DetailRoute(DetailType.MealRandom)) }
                            )
                        }
                        entry<Search> {
                            val viewModel =
                                hiltViewModel<SearchViewModel, SearchViewModel.Factory>(
                                    creationCallback = { factory ->
                                        factory.create(SearchType.Search(""))
                                    }
                                )
                            SearchScreen(
                                viewModel = viewModel,
                                onBack = { backstackManager.pop() },
                                onMealLookup = { id ->
                                    backstackManager.push(
                                        DetailRoute(
                                            DetailType.MealLookup(
                                                id
                                            )
                                        )
                                    )
                                },
                                onScrollChange = { firstVisibleIndex = it },
                                onRandomMeal = { backstackManager.push(DetailRoute(DetailType.MealRandom)) }
                            )
                        }
                        entry<DetailRoute> { detailRoute ->
                            val viewModel =
                                hiltViewModel<DetailsViewModel, DetailsViewModel.Factory>(
                                    creationCallback = { factory ->
                                        factory.create(detailRoute.detailType)
                                    }
                                )

                            DetailScreen(
                                viewModel = viewModel,
                                onBack = { backstackManager.pop() }
                            )
                        }
                        entry<Info> { InfoScreen() }
                    },
                )
            }
        }
    }
}

