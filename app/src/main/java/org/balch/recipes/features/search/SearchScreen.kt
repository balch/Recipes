package org.balch.recipes.features.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import org.balch.recipes.core.models.Meal
import org.balch.recipes.core.models.MealDescriptor
import org.balch.recipes.core.models.SearchType
import org.balch.recipes.ui.theme.DeepBrown
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.theme.ThemePreview
import org.balch.recipes.ui.widgets.FoodLoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onRandomMeal: () -> Unit,
    onMealLookup: (String) -> Unit,
    onScrollChange: (Int) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    SearchLayout(
        uiState = uiState,
        modifier = modifier,
        searchText = uiState.searchText,
        onScrollChange = onScrollChange,
        onMealClick = onMealLookup,
        onRandom = onRandomMeal,
        clearSearch = viewModel::clearSearch,
        onSearch = viewModel::updateSearchQuery,
        onBack = onBack
    )
}

@ThemePreview
@Composable
private fun SearchLayoutPreview(
    @PreviewParameter(SearchStateProvider::class) uiState: SearchUiState,
) {
    RecipesTheme {
        SearchLayout(
            uiState = uiState,
            searchText = "",
            onMealClick = {},
            onSearch = {},
            onRandom = {},
            clearSearch = {},
            onBack = {},
            onScrollChange = {},
        )
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun SearchLayout(
    uiState: SearchUiState,
    modifier: Modifier = Modifier,
    searchText: String,
    onMealClick: (String) -> Unit,
    onSearch: (String) -> Unit,
    onRandom: () -> Unit,
    clearSearch: () -> Unit,
    onBack: () -> Unit,
    onScrollChange: (Int) -> Unit,
) {
    val hazeState = rememberHazeState()
    var query by rememberSaveable { mutableStateOf(searchText) }

    Scaffold(
        topBar = {
            val showSearchBar = when (uiState) {
                is SearchUiState.Show -> uiState.searchType is SearchType.Search
                is SearchUiState.Welcome -> true
                is SearchUiState.Loading -> uiState.showSearchBar
                else -> false
            }

            TopBar(
                modifier = Modifier
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
                clearSearch = clearSearch,
                onBack = onBack,
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
                                onRandom
                            }
                        }
                    )
                }

                is SearchUiState.Show -> {
                    SearchResults(
                        isFetching = uiState.isFetching,
                        meals = uiState.meals,
                        onMealClick = onMealClick,
                        onScrollChange = onScrollChange,
                        paddingValues = innerPadding
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
@Preview
@Composable
private fun TopBar(
    modifier: Modifier = Modifier,
    showSearchBar: Boolean = true,
    searchText: String = "",
    onSearch: (String) -> Unit = {},
    onRandom: () -> Unit = {},
    clearSearch: () -> Unit = {},
    onBack: () -> Unit = {}
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
            navigationIcon = {
                if (!showSearchBar) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back"
                        )
                    }
                }
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
    meals: List<MealDescriptor>,
    onMealClick: (String) -> Unit,
    onScrollChange: (Int) -> Unit,
    paddingValues: PaddingValues,
) {

    val gridState = rememberLazyStaggeredGridState()
    LaunchedEffect(gridState.firstVisibleItemIndex) {
        onScrollChange(gridState.firstVisibleItemIndex)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (meals.isNotEmpty()) {
            LazyVerticalStaggeredGrid(
                state = gridState,
                columns = Adaptive(160.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = paddingValues
            ) {
                items(
                    items = meals,
                    key = { meal -> meal.id }
                ) { meal ->
                    MealCard(
                        meal = meal,
                        onClick = { onMealClick(meal.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
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

@Composable
private fun MealCard(
    meal: MealDescriptor,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            AsyncImage(
                model = meal.thumbnail,
                contentDescription = meal.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))

                if (meal is Meal) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = meal.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )

                        Text(
                            text = meal.area,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
