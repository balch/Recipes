package org.balch.recipes.features.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells.Adaptive
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.balch.recipes.DetailRoute
import org.balch.recipes.RecipeRoute
import org.balch.recipes.core.models.CodeRecipe
import org.balch.recipes.core.models.DetailType
import org.balch.recipes.core.models.MealSummary
import org.balch.recipes.core.models.SearchType
import org.balch.recipes.core.navigation.preview.PreviewNavigationEventDispatcherOwner
import org.balch.recipes.ui.theme.DeepBrown
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.theme.ThemePreview
import org.balch.recipes.ui.utils.sharedBounds
import org.balch.recipes.ui.widgets.CodeRecipeCard
import org.balch.recipes.ui.widgets.FoodLoadingIndicator
import org.balch.recipes.ui.widgets.MealImageBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateTo: (RecipeRoute) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    SearchLayout(
        uiState = uiState,
        modifier = modifier,
        searchText = uiState.searchText,
        onMealClick = { onNavigateTo(DetailRoute(DetailType.MealLookup(it))) },
        onCodeClick = { onNavigateTo(DetailRoute(DetailType.CodeRecipeContent(it))) },
        onRandom = { onNavigateTo(DetailRoute(DetailType.RandomRecipe)) },
        clearSearch = viewModel::clearSearch,
        onSearch = viewModel::updateSearchQuery,
    )
}

@ThemePreview
@Composable
private fun SearchLayoutPreview(
    @PreviewParameter(SearchStateProvider::class) uiState: SearchUiState,
) {
    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides PreviewNavigationEventDispatcherOwner()
    ) {
        RecipesTheme {
            SearchLayout(
                uiState = uiState,
                searchText = "",
                onMealClick = {},
                onCodeClick = {},
                onSearch = {},
                onRandom = {},
                clearSearch = {},
            )
        }
    }
}

@Composable
private fun SearchLayout(
    uiState: SearchUiState,
    modifier: Modifier = Modifier,
    searchText: String,
    onMealClick: (MealSummary) -> Unit,
    onCodeClick: (CodeRecipe) -> Unit,
    onSearch: (String) -> Unit,
    onRandom: () -> Unit,
    clearSearch: () -> Unit,
) {
    val hazeState = rememberHazeState()
    var query by rememberSaveable { mutableStateOf(searchText) }
    LaunchedEffect(Unit) {
        if (query != searchText) {
            // rerun the search if the reload after Process Death
            onSearch(query)
        }
    }

    Scaffold(
        topBar = {
            val showSearchBar = when (uiState) {
                is SearchUiState.Show -> uiState.searchType is SearchType.Search
                is SearchUiState.Welcome -> true
                is SearchUiState.Loading -> uiState.showSearchBar
                else -> false
            }
            
            val topModifier =
                (uiState as? SearchUiState.Show)
                    ?.let { uiState ->
                        if (uiState.searchType !is SearchType.Search) {
                            val key = when (uiState.searchType) {
                                is SearchType.Area -> "search_area_${uiState.searchType.searchText}"
                                is SearchType.Ingredient -> "search_ingredient_${uiState.searchType.searchText}"
                                is SearchType.Category -> "search_category_${uiState.searchType.searchText}"
                                else -> "???"
                            }
                            Modifier
                                .sharedBounds(key)
                        } else null
                    } ?: Modifier

            TopBar(
                modifier = topModifier
                    .hazeEffect(state = hazeState, style = LocalHazeStyle.current) {
                        HazeProgressive.verticalGradient(
                            startIntensity = 0f,
                            endIntensity = 1f,
                        )
                    },
                showSearchBar = showSearchBar,
                searchText = query,
                onSearch = {
                    query = it
                    onSearch(it)
                },
                onRandom = onRandom,
                clearSearch = {
                    query = ""
                    clearSearch()
                },
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .hazeSource(state = hazeState),
        ) {

            // Content based on state
            when (uiState) {
                is SearchUiState.Welcome -> {
                    WelcomeMessage()
                }

                is SearchUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        FoodLoadingIndicator()
                    }
                }

                is SearchUiState.Error -> {
                    ErrorMessage(
                        errorMessage = uiState.message,
                        searchText = searchText,
                        onTryAgain = {
                            if (searchText.isNotEmpty()) {
                                onSearch(searchText)
                            } else {
                                onRandom()
                            }
                        }
                    )
                }

                is SearchUiState.Show -> {
                    SearchResults(
                        isFetching = uiState.isFetching,
                        items = uiState.items,
                        onMealClick = onMealClick,
                        onCodeClick = onCodeClick,
                        paddingValues = innerPadding,
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Search for your favorite recipes\nor try a random one!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    modifier: Modifier = Modifier,
    showSearchBar: Boolean = true,
    searchText: String = "",
    onSearch: (String) -> Unit = {},
    onRandom: () -> Unit = {},
    clearSearch: () -> Unit = {},
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        TopAppBar(
            title = {
                TitleBar(
                    showSearchBar = showSearchBar,
                    searchText = searchText,
                    onSearch = onSearch,
                    onRandom = onRandom,
                    clearSearch = clearSearch,
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
            )
        )
    }
}

@Composable
private fun TitleBar(
    showSearchBar: Boolean,
    searchText: String,
    onSearch: (String) -> Unit,
    onRandom: () -> Unit,
    clearSearch: () -> Unit,
) {
    if (showSearchBar) {
        SearchBarRow(
            query = searchText,
            onSearch = onSearch,
            onRandom = onRandom,
            clearSearch = clearSearch,
        )
    } else {
        Text(
            text = searchText,
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBarRow(
    query: String,
    onSearch: (String) -> Unit,
    onRandom: () -> Unit,
    clearSearch: () -> Unit,
) {
    SearchBar(
        expanded = false,
        onExpandedChange = {  },
        colors = SearchBarDefaults.colors(
            containerColor = Color.Transparent,
        ),
        inputField = {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                SearchBarDefaults.InputField(
                    modifier = Modifier.weight(.75F)
                        .graphicsLayer(alpha = .8f),
                    query = query,
                    enabled = true,
                    onQueryChange = onSearch,
                    onSearch = onSearch,
                    expanded = false,
                    onExpandedChange = {  },
                    placeholder = { Text("Search recipes...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"

                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = clearSearch) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    }
                )

                Button(
                    onClick = onRandom,
                    modifier = Modifier.height(52.dp)
                        .graphicsLayer(alpha = .8f)
                        .weight(.24f),
                    shape = RoundedCornerShape(23.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepBrown,
                    )

                ) {
                    BasicText(
                        autoSize = TextAutoSize.StepBased(maxFontSize = 12.sp),
                        text = "🎲🎲",
                        maxLines = 1
                    )
                }
            }
        }
    ) {
        // `expanded always = false, content is displayed in Scaffold
    }
}

@Composable
private fun ErrorMessage(
    errorMessage: String,
    searchText: String,
    onTryAgain: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
            .padding(horizontal = 64.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(bottom = 8.dp)
                    .fillMaxWidth(),
                text = "Oops! Something went wrong search for \"$searchText\"",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                modifier = Modifier.padding(top = 8.dp),
                onClick = { onTryAgain() }
            ) {
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun SearchResults(
    isFetching: Boolean,
    items: List<ItemType>,
    onMealClick: (MealSummary) -> Unit,
    onCodeClick: (CodeRecipe) -> Unit,
    paddingValues: PaddingValues,
) {

    val gridState = rememberLazyStaggeredGridState()

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (items.isNotEmpty()) {
            LazyVerticalStaggeredGrid(
                state = gridState,
                columns = Adaptive(160.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 6.dp,
                modifier = Modifier.fillMaxSize(),
                contentPadding = paddingValues
            ) {
                items(
                    items = items,
                    key = { item -> item.id }
                ) { item ->
                    when (item) {
                        is ItemType.MealType -> MealImageBadge(
                            meal = item.meal,
                            showBadge = true,
                            onClick = { onMealClick(item.meal) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        is ItemType.CodeRecipeType -> CodeRecipeCard(
                            codeRecipe = item.codeRecipe,
                            onClick = { onCodeClick(item.codeRecipe) },
                            modifier = Modifier.fillMaxWidth(),
                            center = false,
                        )
                    }
                }
            }
        } else if (!isFetching) {
            Text(
                text = "No results found",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        if (isFetching) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                FoodLoadingIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
