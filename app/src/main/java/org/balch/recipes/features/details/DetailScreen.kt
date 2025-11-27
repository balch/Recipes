package org.balch.recipes.features.details

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.balch.recipes.core.models.Meal
import org.balch.recipes.core.navigation.LocalSharedTransition
import org.balch.recipes.ui.nav.PreviewNavigationEventDispatcherOwner
import org.balch.recipes.ui.nav.isCompact
import org.balch.recipes.ui.theme.RecipesTheme
import org.balch.recipes.ui.theme.ThemePreview
import org.balch.recipes.ui.widgets.FoodLoadingIndicator
import org.balch.recipes.ui.widgets.MealImageBadge
import org.balch.recipes.ui.widgets.PlayerStatus
import org.balch.recipes.ui.widgets.YouTubePlayerState
import org.balch.recipes.ui.widgets.YouTubePlayerWidget
import org.balch.recipes.ui.widgets.rememberYouTubePlayer

/**
 * Represents the different view modes for recipe.
 */
enum class DetailViewMode(val showSteps: Boolean) {
    List(false),
    StepByStep(true),
    Video(false)
}

/**
 * State holder for the Detail Screen that encapsulates all UI state related to
 * viewing and interacting with recipe details.
 *
 * @property detailViewMode Current view mode for recipe instructions (List or StepByStep)
 * @property currentStepIndex Current step index when in StepByStep mode
 * @property listState LazyList scroll state for the detail content
 * @property showMealTitleInHeader Whether the title should be displayed in the header
 */
@Stable
class DetailScreenState(
    initialDetailViewMode: DetailViewMode,
    initialStepIndex: Int,
    val uiState: UiState,
    val listState: LazyListState,
    val animatedVisibilityScope: AnimatedVisibilityScope?,
) {

    private var _showCodeRecipeTitle by mutableStateOf(false)

    val showTitleInHeader by derivedStateOf {
        when {
            (animatedVisibilityScope?.transition?.isRunning == true) -> false
            uiState is UiState.ShowMeal -> showMealTitleInHeader || detailViewMode == DetailViewMode.Video
            uiState is UiState.ShowCodeRecipe -> _showCodeRecipeTitle || uiState.codeRecipe.aiGenerated
            else -> false
        }
    }

    private var _detailViewMode by mutableStateOf(initialDetailViewMode)
    val detailViewMode: DetailViewMode
        get() = _detailViewMode

    private var _currentStepIndex by mutableIntStateOf(initialStepIndex)
    val currentStepIndex: Int
        get() = _currentStepIndex

    val showMealTitleInHeader: Boolean by derivedStateOf {
        listState.firstVisibleItemIndex > 0
            || detailViewMode == DetailViewMode.StepByStep
    }

    fun setShowCodeRecipeTitle(isVisible: Boolean) {
        _showCodeRecipeTitle = isVisible
    }

    /**
     * Updates the step view mode and resets the current step index
     */
    fun setDetailViewMode(mode: DetailViewMode) {
        _detailViewMode = mode
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
 * Keys by stable content identity (meal.id or codeRecipe.id) rather than the entire uiState
 * to avoid resetting state when uiState changes identity but content remains the same.
 *
 * @param initialDetailViewMode Initial view mode for instructions (default: List)
 * @param initialStepIndex Initial step index (default: 0)
 */
@Composable
fun rememberDetailScreenState(
    initialDetailViewMode: DetailViewMode = DetailViewMode.List,
    initialStepIndex: Int = 0,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    uiState: UiState,
): DetailScreenState {
    val listState = rememberLazyListState()
    
    // Key by stable content identity to preserve state across transient uiState updates
    val stateKey = when (uiState) {
        is UiState.ShowMeal -> "meal:${uiState.meal.id}"
        is UiState.ShowCodeRecipe -> "code:${uiState.codeRecipe.id}"
        else -> "transient" // Loading/Error
    }
    
    return remember(stateKey) {
        DetailScreenState(
            initialDetailViewMode = initialDetailViewMode,
            initialStepIndex = initialStepIndex,
            listState = listState,
            animatedVisibilityScope = animatedVisibilityScope,
            uiState = uiState,
        )
    }
}

@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    viewModel: DetailsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    DetailLayout(
        uiState = uiState,
        modifier = modifier,
        onBack = onBack,
    )
}

@Composable
fun DetailLayout(
    uiState: UiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit ,
) {
    val hazeState = rememberHazeState()
    val detailState = rememberDetailScreenState(
        animatedVisibilityScope = LocalSharedTransition.current.animatedVisibilityScope,
        uiState = uiState,
    )

    // Return to List View if back out from Video View
    BackHandler(enabled = detailState.detailViewMode != DetailViewMode.List) {
        detailState.setDetailViewMode(DetailViewMode.List)
    }

    val instructionSteps =
        (uiState as? UiState.ShowMeal)?.meal?.instructionSteps
            ?:emptyList()

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
                onBack = {
                    if (detailState.detailViewMode != DetailViewMode.List) {
                        detailState.setDetailViewMode(DetailViewMode.List)
                    } else {
                        onBack()
                    }
                },
            )
        },
        bottomBar = {
            if (detailState.detailViewMode.showSteps) {
                Column {
                    RecipeInstructionsHeader(
                        modifier = modifier,
                        onDetailViewModeChange = { detailState.setDetailViewMode(it) },
                        detailViewMode = detailState.detailViewMode,
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
                    )
                }

                is UiState.ShowMeal -> {
                    MealDetailItem(
                        modifier = modifier.hazeSource(hazeState),
                        meal = uiState.meal,
                        detailViewMode = detailState.detailViewMode,
                        instructionSteps = instructionSteps,
                        onDetailViewModeChange = { detailState.setDetailViewMode(it) },
                        listState = detailState.listState,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDetailItem(
    modifier: Modifier = Modifier,
    meal: Meal,
    detailViewMode: DetailViewMode,
    instructionSteps: List<String>,
    onDetailViewModeChange: (DetailViewMode) -> Unit,
    listState: LazyListState,
) {

    val playerState = rememberYouTubePlayer(
        key = meal.id,
        allowFullScreen = false,
    )
    val playerStatus by playerState.status

    // Use LaunchedEffect to react to changes in videoId or the player instance
    LaunchedEffect(meal.youtube, playerStatus) {
        if (playerStatus == PlayerStatus.IDLE) {
            if (meal.youtube != null && meal.youtube.isNotEmpty()) {
                playerState.cueVideo(meal.youtube)
            } else {
                playerState.clear()
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        state = listState,
    ) {
        when (detailViewMode) {
            DetailViewMode.List -> {
                listViewItems(
                    meal = meal,
                    playerStatus = playerStatus,
                    instructionSteps = instructionSteps,
                    showCompactIngredients = true,
                    onPlayVideo = {
                        playerState.play()
                        onDetailViewModeChange(DetailViewMode.Video)
                    },
                    onDetailViewModeChange = onDetailViewModeChange,
                )
            }
            DetailViewMode.StepByStep -> {
                stepByStepViewItems(
                    meal = meal,
                    modifier = modifier,
                )
            }
            DetailViewMode.Video -> {
                videoViewItems(
                    playerState = playerState,
                    meal = meal,
                    modifier = modifier,
                    instructionSteps = instructionSteps,
                )
            }
        }
    }
}

private fun LazyListScope.videoViewItems(
    playerState: YouTubePlayerState,
    meal: Meal,
    instructionSteps: List<String>,
    modifier: Modifier = Modifier,
) {
    stickyHeader {
        YouTubePlayerWidget(
            playerState,
            modifier = Modifier
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        )
    }
    item {
        CrossfadeIngredients(modifier, meal, false)
        RecipeInstructionsHeader(modifier, { }, DetailViewMode.Video)
        Spacer(modifier = modifier.height(16.dp))
    }

    itemsIndexed(instructionSteps) { index, step ->
        RecipeInstructionListStepCard(modifier, step, index)
    }
}

private fun LazyListScope.stepByStepViewItems(
    meal: Meal,
    modifier: Modifier = Modifier,
) {
    item { CrossfadeIngredients(modifier, meal, true) }
}

private fun LazyListScope.listViewItems(
    meal: Meal,
    playerStatus: PlayerStatus,
    instructionSteps: List<String>,
    showCompactIngredients: Boolean,
    onPlayVideo: () -> Unit,
    onDetailViewModeChange: (DetailViewMode) -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    item {
        Box {
            MealImageBadge(
                meal = meal.toMealSummary(),
                showBadge = false,
                modifier = modifier,
                onClick = {},
            )

            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.Center),
                visible = playerStatus == PlayerStatus.LOADED,
                enter = scaleIn() + expandVertically(expandFrom = Alignment.CenterVertically),
                exit = scaleOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically),
            ) {
                FloatingActionButton(
                    onClick = onPlayVideo,
                    modifier = Modifier
                        .width(120.dp)
                        .height(85.dp),
                    containerColor = Color(0xBBCD201F),
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play videos",
                        modifier = Modifier.size(42.dp),
                    )
                }
            }
        }
    }
    item { RecipeInfoCard(modifier, meal) }

    stickyHeader {
        Column(
            modifier = modifier
                .background(MaterialTheme.colorScheme.surfaceContainer),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CrossfadeIngredients(modifier, meal, showCompactIngredients)
            RecipeInstructionsHeader(modifier, onDetailViewModeChange, DetailViewMode.List)
            Spacer(modifier = modifier.height(16.dp))
        }
    }

    itemsIndexed(instructionSteps) { index, step ->
        RecipeInstructionListStepCard(modifier, step, index)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    modifier: Modifier,
    uiState: UiState,
    showTitleInHeader: Boolean,
    onBack: () -> Unit,
) {

    val defaultTitle = uiState.defaultTitle()
    val overrideTitle = uiState.overrideTitle()

    // Show meal title when sticky header is stuck, otherwise show default
    val shouldShowOverrideTitle = showTitleInHeader && overrideTitle != null

    val showBack = currentWindowAdaptiveInfo().isCompact()

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
            if (showBack) {
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
    onDetailViewModeChange: (DetailViewMode) -> Unit,
    detailViewMode: DetailViewMode,
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

            if (detailViewMode == DetailViewMode.StepByStep ||
                detailViewMode == DetailViewMode.List) {
                SingleChoiceSegmentedButtonRow(
                    modifier = modifier.weight(8f),
                ) {
                    SegmentedButton(
                        modifier = modifier.height(50.dp),
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                        onClick = { onDetailViewModeChange(DetailViewMode.List) },
                        selected = detailViewMode == DetailViewMode.List,
                        label = { Text("List") }
                    )

                    SegmentedButton(
                        modifier = modifier.height(50.dp),
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                        onClick = { onDetailViewModeChange(DetailViewMode.StepByStep) },
                        selected = detailViewMode == DetailViewMode.StepByStep,
                        label = { Text("Steps", maxLines = 1, overflow = TextOverflow.Clip) }
                    )
                }
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
                modifier = modifier
                    .padding(bottom = 24.dp)
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


@ThemePreview
@Composable
private fun DetailScreenPreview(
    @PreviewParameter(DetailStateProvider::class) uiState: UiState
) {
    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides PreviewNavigationEventDispatcherOwner()
    ) {
        RecipesTheme {
            DetailLayout(
                uiState = uiState,
                onBack = {}
            )
        }
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

@ThemePreview
@Composable
private fun ListViewItemsPreview() {
    val meal = DetailStateProvider.previewMeal

    RecipesTheme {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            listViewItems(
                meal = meal,
                playerStatus = PlayerStatus.LOADED,
                instructionSteps = meal.instructionSteps,
                showCompactIngredients = false,
                onPlayVideo = {},
                onDetailViewModeChange = {},
                modifier = Modifier,
                sharedTransitionScope = null,
                animatedVisibilityScope = null,
            )
        }
    }
}

@ThemePreview
@Composable
private fun VideoViewItemsPreview() {
    val meal = DetailStateProvider.previewMeal
    val playerState = rememberYouTubePlayer(
        key = meal.id,
        allowFullScreen = false,
    )
    
    RecipesTheme {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            videoViewItems(
                playerState = playerState,
                meal = meal,
                instructionSteps = meal.instructionSteps,
                modifier = Modifier,
            )
        }
    }
}

/**
 * Default title when the sticky header is not stuck.
 */
private fun UiState.defaultTitle(): String = when (this) {
    is UiState.ShowMeal -> "Meal Recipe #${meal.id}"
    is UiState.Loading -> "Meal Recipe #${mealSummary?.id ?: "Loading"}"
    is UiState.ShowCodeRecipe -> "Code Recipe #${codeRecipe.index}"
    is UiState.Error -> "Error"
}


/**
 * Show the item name when the sticky header is stuck.
 */
private fun UiState.overrideTitle(): String? = when (this) {
    is UiState.ShowMeal -> meal.name
    is UiState.ShowCodeRecipe -> codeRecipe.title
    else -> null
}
