package org.balch.recipes.features.ideas

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells.Fixed
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import org.balch.recipes.BrowsableType
import org.balch.recipes.core.models.Area
import org.balch.recipes.core.models.Category
import org.balch.recipes.core.models.Ingredient
import org.balch.recipes.core.models.UniqueItem
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.theme.ThemePreview
import org.balch.recipes.ui.widgets.FoodLoadingIndicator
import kotlin.random.Random

/**
 * Selects a theme color based on the item index for glass-like haze effects
 */
@Composable
private fun getHazeColorForIndex(index: Int): Color {
    val colorSet = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.inversePrimary
    )
    return colorSet[index % (colorSet.size-1)]
}

@Composable
fun IdeasScreen(
    modifier: Modifier = Modifier,
    viewModel: IdeasViewModel = hiltViewModel(),
    onCategoryClick: (Category) -> Unit,
    onAreaClick: (Area) -> Unit,
    onIngredientClick: (Ingredient) -> Unit,
    onScrollChange: (Int) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    IdeasLayout(
        uiState = uiState,
        onRetry = viewModel::retry,
        onAreaClick = onAreaClick,
        onCategoryClick = onCategoryClick,
        onIngredientClick = onIngredientClick,
        onScrollChange = onScrollChange,
        onBrowsableTypeChange = viewModel::changeBrowsableType,
        modifier = modifier
    )
}

@ThemePreview
@Composable
private fun IdeasLayoutPreview(
    @PreviewParameter(IdeasStateProvider ::class) uiState: IdeasUiState,
) {
    RecipesTheme {
        IdeasLayout(
            uiState = uiState,
            onRetry = { },
            onCategoryClick = { },
            onBrowsableTypeChange = { },
            onAreaClick = { },
            onIngredientClick = { },
            onScrollChange = { }
        )
    }
}
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun IdeasLayout(
    uiState: IdeasUiState,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
    onAreaClick: (Area) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onIngredientClick: (Ingredient) -> Unit,
    onBrowsableTypeChange: (BrowsableType) -> Unit,
    onScrollChange: (Int) -> Unit,
) {
    val hazeState = rememberHazeState()

    Scaffold(
        topBar = {
            TopBar(
                modifier = Modifier
                    .hazeEffect(state = hazeState, style = LocalHazeStyle.current) {
                        HazeProgressive.verticalGradient(
                            startIntensity = 0f,
                            endIntensity = 1f,
                        )
                    },

                browsableType = when (uiState) {
                    is IdeasUiState.Categories -> BrowsableType.Category
                    is IdeasUiState.Areas -> BrowsableType.Area
                    is IdeasUiState.Ingredients -> BrowsableType.Ingredient
                    else -> BrowsableType.Category
                },
                onBrowsableTypeChange = onBrowsableTypeChange
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .padding(innerPadding)
                .hazeSource(state = hazeState),
        )
        Box {
            if (uiState.imageUrl != null) {
                AsyncImage(
                    contentScale = ContentScale.FillHeight,
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uiState.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Background",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                    )
            }
            when (uiState) {
                is IdeasUiState.Loading -> {
                    FoodLoadingIndicator(
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is IdeasUiState.Error -> {
                    ErrorState(
                        error = uiState.message,
                        onRetry = onRetry,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is IdeasUiState.Categories -> {
                    ResultsGrid(
                        items = uiState.categories,
                        onScrollChange = onScrollChange,
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(state = hazeState),
                        paddingValues = innerPadding,
                    ) { _, item ->
                        CategoryCard(
                            category = item,
                            onClick = { onCategoryClick(item) }
                        )
                    }
                }

                is IdeasUiState.Areas -> {
                    ResultsGrid(
                        items = uiState.areas,
                        onScrollChange = onScrollChange,
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(state = hazeState),
                        paddingValues = innerPadding,
                    ) { _, item ->
                        AreaCard(
                            area = item,
                            onClick = { onAreaClick(item) },
                        )
                    }
                }

                is IdeasUiState.Ingredients -> {
                    ResultsGrid(
                        items = uiState.ingredients,
                        onScrollChange = onScrollChange,
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(state = hazeState),
                        paddingValues = innerPadding,
                    ) { _, item ->
                        IngredientCard(
                            ingredient = item,
                            onClick = { onIngredientClick(item) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    browsableType: BrowsableType,
    modifier: Modifier = Modifier,
    onBrowsableTypeChange: (BrowsableType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier,
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Clickable trigger area
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable(onClick = { expanded = !expanded })
                ) {
                    Text(
                        text = browsableType.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        IdeasDropDown(
                            expanded = expanded,
                            browsableType = browsableType,
                            onSelected = { browsableType ->
                                expanded = false
                                onBrowsableTypeChange(browsableType)
                            },
                            onDismiss = { expanded = false },
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        )
    )
}

@Composable
private fun IdeasDropDown(
    expanded: Boolean,
    browsableType: BrowsableType,
    onDismiss: () -> Unit,
    onSelected: (BrowsableType) -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceContainer,
                RoundedCornerShape(12.dp)
            )
    ) {
        BrowsableTypeMenuItem(
            browsableType = BrowsableType.Category,
            isSelected = browsableType == BrowsableType.Category,
            onSelected = onSelected
        )
        BrowsableTypeMenuItem(
            browsableType = BrowsableType.Area,
            isSelected = browsableType == BrowsableType.Area,
            onSelected = onSelected
        )
        BrowsableTypeMenuItem(
            browsableType = BrowsableType.Ingredient,
            isSelected = browsableType == BrowsableType.Ingredient,
            onSelected = onSelected
        )
    }
}

@Composable
private fun BrowsableTypeMenuItem(
    browsableType: BrowsableType,
    isSelected: Boolean,
    onSelected: (BrowsableType) -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text = browsableType.displayName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight =
                        if (isSelected) FontWeight.Bold
                        else FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        leadingIcon = {
            Icon(
                imageVector = browsableType.imageVector,
                contentDescription = browsableType.displayName,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
        },
        onClick = { onSelected(browsableType) },
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.
        padding(horizontal = 26.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "😞",
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            )
            Button(onClick = onRetry) {
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun <T: UniqueItem> ResultsGrid(
    items: List<T>,
    onScrollChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    content: @Composable (Int, T) -> Unit,
) {
    val gridState = rememberLazyStaggeredGridState()
    LaunchedEffect(gridState.firstVisibleItemIndex) {
        onScrollChange(gridState.firstVisibleItemIndex)
    }
    LazyVerticalStaggeredGrid(
        state = gridState,
        columns = Fixed(2),
        contentPadding = paddingValues,
        modifier = modifier,
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> item.id }
        ) { index, item -> content(index, item) }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(category.thumbnail)
                    .crossfade(true)
                    .build(),
                contentDescription = category.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )

            // Gradient overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 200f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .align(Alignment.BottomStart)
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (category.description.isNotBlank()) {
                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AreaCard(
    area: Area,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = getHazeColorForIndex(Math.abs((Random.nextInt())))
    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(.9f)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.1f),
                            color.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = area.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun IngredientCard(
    ingredient: Ingredient,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = getHazeColorForIndex(Math.abs((Random.nextInt())))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(.9f)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.1f),
                            color.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = ingredient.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
