package org.balch.recipes.features.details

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.balch.recipes.core.models.Meal
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.theme.ThemePreview
import org.balch.recipes.ui.widgets.FoodLoadingIndicator
import org.balch.recipes.ui.widgets.MealImageBadge
import org.balch.recipes.ui.widgets.YouTubePlayerWidget

enum class StepViewMode {
    List, StepByStep
}

/**
 * State holder for the Detail Screen that encapsulates all UI state related to
 * viewing and interacting with recipe details.
 *
 * @property stepViewMode Current view mode for recipe instructions (List or StepByStep)
 * @property currentStepIndex Current step index when in StepByStep mode
 * @property listState LazyList scroll state for the detail content
 * @property showMealTitleInHeader Whether the title should be displayed in the header
 */
@Stable
class DetailScreenState(
    initialStepViewMode: StepViewMode,
    initialStepIndex: Int,
    val uiState: UiState,
    val listState: LazyListState,
    val animatedVisibilityScope: AnimatedVisibilityScope?,
) {

    private var _showCodeRecipeTitle by mutableStateOf(false)

    val showTitleInHeader by derivedStateOf {
        (animatedVisibilityScope?.transition?.isRunning != true)
            && (
                (showMealTitleInHeader && uiState is UiState.ShowMeal)
                    || (_showCodeRecipeTitle && uiState is UiState.ShowCodeRecipe)
            )
    }

    private var _stepViewMode by mutableStateOf(initialStepViewMode)
    val stepViewMode: StepViewMode
        get() = _stepViewMode
    
    private var _currentStepIndex by mutableIntStateOf(initialStepIndex)
    val currentStepIndex: Int
        get() = _currentStepIndex

    val showMealTitleInHeader: Boolean by derivedStateOf {
        listState.firstVisibleItemIndex > 0
            || stepViewMode == StepViewMode.StepByStep
    }

    fun setShowCodeRecipeTitle(isVisible: Boolean) {
        _showCodeRecipeTitle = isVisible
    }

    /**
     * Updates the step view mode and resets the current step index
     */
    fun setStepViewMode(mode: StepViewMode) {
        _stepViewMode = mode
        _currentStepIndex = 0
    }
    
    /**
     * Navigates to a specific step index
     */
    fun navigateToStep(index: Int) {
        _currentStepIndex = index
    }
}

/**
 * Creates and remembers a [DetailScreenState] instance.
 *
 * @param initialStepViewMode Initial view mode for instructions (default: List)
 * @param initialStepIndex Initial step index (default: 0)
 */
@Composable
fun rememberDetailScreenState(
    initialStepViewMode: StepViewMode = StepViewMode.List,
    initialStepIndex: Int = 0,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    uiState: UiState,
): DetailScreenState {
    val listState = rememberLazyListState()
    return remember(listState, uiState) {
        DetailScreenState(
            initialStepViewMode = initialStepViewMode,
            initialStepIndex = initialStepIndex,
            listState = listState,
            animatedVisibilityScope = animatedVisibilityScope,
            uiState = uiState,
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    viewModel: DetailsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val uiState by viewModel.uiState.collectAsState()
    DetailLayout(
        uiState = uiState,
        modifier = modifier,
        onBack = onBack,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DetailLayout(
    uiState: UiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit ,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val hazeState = rememberHazeState()
    val detailState = rememberDetailScreenState(
        animatedVisibilityScope = animatedVisibilityScope,
        uiState = uiState,
    )

    val instructionSteps = (uiState as? UiState.ShowMeal)?.meal
        ?.instructions?.split("\r\n", "\n", ". ")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?: emptyList()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        topBar = {
            TopBar(
                modifier = modifier
                    .hazeEffect(state = hazeState, style = LocalHazeStyle.current) {
                        HazeProgressive.verticalGradient(
                            startIntensity = 1f,
                            endIntensity = 0f,
                        )
                    },
                uiState = uiState,
                showTitleInHeader = detailState.showTitleInHeader,
                onBack = onBack,
            )
        },
        bottomBar = {
            if (detailState.stepViewMode == StepViewMode.StepByStep) {
                Column {
                    RecipeInstructionsHeader(
                        modifier = modifier,
                        onStepViewModeChange = { detailState.setStepViewMode(it) },
                        stepViewMode = detailState.stepViewMode,
                    )
                    RecipeInstructionByStepCard(
                        modifier = modifier,
                        currentStepIndex = detailState.currentStepIndex,
                        instructionSteps = instructionSteps,
                        onStepChange = { detailState.navigateToStep(it) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (uiState) {
                is UiState.ShowCodeRecipe -> {
                    CodeDetailItem(
                        modifier = modifier.hazeSource(hazeState),
                        codeRecipe = uiState.codeRecipe,
                        onTittleVisible = { detailState.setShowCodeRecipeTitle(!it) },
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                }

                is UiState.ShowMeal -> {
                    MealDetailItem(
                        modifier = modifier.hazeSource(hazeState),
                        meal = uiState.meal,
                        stepViewMode = detailState.stepViewMode,
                        instructionSteps = instructionSteps,
                        onStepViewModeChange = { detailState.setStepViewMode(it) },
                        listState = detailState.listState,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                }

                is UiState.Loading -> {
                    Column(
                        modifier = modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally

                    ) {
                        if (uiState.mealSummary != null) {
                            MealImageBadge(
                                meal = uiState.mealSummary,
                                showBadge = false,
                                onClick = {},
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                            )
                        }

                        FoodLoadingIndicator(
                            modifier = modifier.fillMaxSize()
                        )
                    }
                }

                is UiState.Error -> {
                    Box(
                        modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = uiState.message)
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun MealDetailItem(
    modifier: Modifier = Modifier,
    meal: Meal,
    stepViewMode: StepViewMode,
    instructionSteps: List<String>,
    onStepViewModeChange: (StepViewMode) -> Unit,
    listState: LazyListState,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    
    var playVideo by remember(stepViewMode, meal.youtube) { mutableStateOf(false) }

    /**
     * Show Compact Ingredients when the Ingredients card scrolls
     * to the top and we are in StepViewMode.List mode.
     *
     * Update [ingredientsCardPosition] when moving the stickyHeader element
     */
    val ingredientsCardPosition = 2
    val showCompactIngredients by remember(listState, stepViewMode) {
        derivedStateOf {
            listState.firstVisibleItemIndex >= ingredientsCardPosition
                    || stepViewMode == StepViewMode.StepByStep
                    || playVideo
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        state = listState,
    ) {
        if (stepViewMode == StepViewMode.List) {
            if (!playVideo) {
                item {
                    Box {
                        MealImageBadge(
                            meal = meal.toMealSummary(),
                            showBadge = false,
                            modifier = modifier,
                            onClick = {},
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                        )

                        if (meal.youtube?.isNotEmpty() ?: false) {
                            FloatingActionButton(
                                onClick = { playVideo = true },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Play videos"
                                )
                            }
                        }
                    }

                }
                item { RecipeInfoCard(modifier, meal) }
            }

            stickyHeader {
                Column(
                    modifier = modifier
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    if (playVideo) {
                        YouTubePlayerWidget(
                            meal.youtube!!,
                            modifier = Modifier
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        )
                    }

                    CrossfadeIngredients(modifier, meal, showCompactIngredients)
                    RecipeInstructionsHeader(modifier, onStepViewModeChange, stepViewMode)
                    Spacer(modifier = modifier.height(16.dp))
                }
            }

            itemsIndexed(instructionSteps) { index, step ->
                RecipeInstructionListStepCard(modifier, step, index)
            }
        } else {
            item { CrossfadeIngredients(modifier, meal, showCompactIngredients) }
        }
    }
}

@Composable
private fun CrossfadeIngredients(
    modifier: Modifier = Modifier,
    meal: Meal,
    showCompactIngredients: Boolean,
) {
    Crossfade(
        targetState = showCompactIngredients,
        animationSpec = tween(200)
    ) { isCompact ->
        RecipeIngredientsCard(
            modifier = modifier,
            meal = meal,
            showCompact = isCompact,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun TopBar(
    modifier: Modifier,
    uiState: UiState,
    showTitleInHeader: Boolean,
    onBack: () -> Unit,
) {
    // Determine the default title based on UI state
    val defaultTitle = when (uiState) {
        is UiState.ShowMeal -> "Meal Recipe #${uiState.meal.id}"
        is UiState.Loading -> "Meal Recipe #${uiState.mealSummary?.id ?: "Loading"}"
        is UiState.ShowCodeRecipe -> "Code Recipe #${uiState.codeRecipe.index}"
        is UiState.Error -> "Error"
    }

    // Determine the alternate title (meal name) when sticky header is stuck
    val overrideTitle = when (uiState) {
        is UiState.ShowMeal -> uiState.meal.name
        is UiState.ShowCodeRecipe -> uiState.codeRecipe.title
        else -> null
    }

    // Show meal title when sticky header is stuck, otherwise show default
    val shouldShowOverrideTitle = showTitleInHeader && overrideTitle != null

    TopAppBar(
        modifier = modifier,
        title = {
            Crossfade(
                targetState = shouldShowOverrideTitle,
                animationSpec = tween(300)
            ) { showMealTitle ->
                Text(
                    text = if (showMealTitle && overrideTitle != null) overrideTitle else defaultTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        )
    )
}

@Composable
private fun RecipeInfoCard(
    modifier: Modifier,
    meal: Meal
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Category: ${meal.category}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Cuisine: ${meal.area}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecipeIngredientsCard(
    meal: Meal,
    modifier: Modifier = Modifier,
    showCompact: Boolean,
) {

    val titleAlpha = if (showCompact) 0f else 1f

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
    ) {
        // Title that fades out when collapsing
        if (titleAlpha > 0f) {
            Text(
                text = "Ingredients",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .alpha(titleAlpha)
            )
        }

        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
        ) {
            RecipeIngredients(
                meal = meal,
                showCompact = showCompact
            )
        }
    }
}

@Composable
private fun RecipeIngredients(
    meal: Meal,
    modifier: Modifier = Modifier,
    showCompact: Boolean = false,
) {
    val outerPadding = if (showCompact) 8.dp else 16.dp
    val innerPadding = if (showCompact) 2.dp else 4.dp
    val textStyle = if (showCompact) MaterialTheme.typography.bodySmall
                    else MaterialTheme.typography.bodyMedium
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(outerPadding)
    ) {
        meal.ingredientsWithMeasures.forEach { (ingredient, measure) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = innerPadding),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier.weight(.4f),
                    text = ingredient,
                    style = textStyle,
                )
                Text(
                    modifier = Modifier.weight(.6f),
                    text = measure,
                    style = textStyle,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End
                )
            }

            if (!showCompact) {
                if (meal.ingredientsWithMeasures.indexOf(ingredient to measure) < meal.ingredientsWithMeasures.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 2.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeInstructionsHeader(
    modifier: Modifier,
    onStepViewModeChange: (StepViewMode) -> Unit,
    stepViewMode: StepViewMode,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Instructions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = modifier.weight(2f))

            SingleChoiceSegmentedButtonRow(
                modifier = modifier.weight(8f),
            ) {
                SegmentedButton(
                    modifier = modifier.height(50.dp),
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                    onClick = { onStepViewModeChange(StepViewMode.List) },
                    selected = stepViewMode == StepViewMode.List,
                    label = { Text("List") }
                )

                SegmentedButton(
                    modifier = modifier.height(50.dp),
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                    onClick = { onStepViewModeChange(StepViewMode.StepByStep) },
                    selected = stepViewMode == StepViewMode.StepByStep,
                    label = { Text("Steps", maxLines = 1, overflow = TextOverflow.Clip) }
                )
            }
        }
    }
}

@Composable
private fun RecipeInstructionListStepCard(
    modifier: Modifier,
    step: String,
    index: Int
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = "${index+1}. $step",
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier.padding(16.dp)
        )
    }
}

@Composable
private fun RecipeInstructionByStepCard(
    currentStepIndex: Int, 
    instructionSteps: List<String>,
    modifier: Modifier = Modifier,
    onStepChange: (Int) -> Unit,
) {

    // Step by Step View
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = modifier.padding(16.dp)
        ) {
            // Step counter
            Text(
                text = "Step ${currentStepIndex + 1} of ${instructionSteps.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = modifier.padding(bottom = 16.dp)
            )

            // Current step text
            Text(
                text = instructionSteps[currentStepIndex],
                style = MaterialTheme.typography.bodyLarge,
                modifier = modifier.padding(bottom = 24.dp)
                    .height(120.dp)
                    .verticalScroll(rememberScrollState())
            )

            // Navigation buttons
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        if (currentStepIndex > 0) {
                            onStepChange(currentStepIndex-1)
                        }
                    },
                    enabled = currentStepIndex > 0
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                        contentDescription = "Previous step"
                    )
                }

                Text(
                    text = "${currentStepIndex + 1}/${instructionSteps.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = modifier.align(Alignment.CenterVertically)
                )

                IconButton(
                    onClick = {
                        if (currentStepIndex < instructionSteps.size - 1) {
                            onStepChange(currentStepIndex+1)
                        }
                    },
                    enabled = currentStepIndex < instructionSteps.size - 1
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                        contentDescription = "Next step"
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalSharedTransitionApi::class)
@ThemePreview
@Composable
private fun DetailScreenPreview(
    @PreviewParameter(DetailStateProvider::class) uiState: UiState
) {

    RecipesTheme {
        DetailLayout(
            uiState = uiState,
            onBack = {}
        )
    }
}

@ThemePreview
@Composable
private fun RecipeIngredientsCardPreview(
) {
    RecipesTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RecipeIngredientsCard(
                meal = DetailStateProvider.previewMeal,
                showCompact = true,
            )
            RecipeIngredientsCard(
                meal = DetailStateProvider.previewMeal,
                showCompact = false,
            )
        }
    }

}
