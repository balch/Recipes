package org.balch.recipes.ui.widgets

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.balch.recipes.core.models.CodeRecipe
import org.balch.recipes.core.models.color
import org.balch.recipes.ui.utils.TransitionKeySuffix.KEY_CODE_RECIPE_TITLE
import org.balch.recipes.ui.utils.sharedBounds

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CodeRecipeCard(
    codeRecipe: CodeRecipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    center: Boolean,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val color = codeRecipe.area.color()
    Card(
        modifier = modifier
            .height(105.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.1f),
                            color.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(12.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = if (center) Arrangement.Center else Arrangement.Bottom,
                horizontalAlignment = if (center) Alignment.CenterHorizontally else Alignment.Start
            ) {
                CodeRecipeAreaBadge(
                    codeRecipe = codeRecipe,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                )

                Text(
                    modifier = Modifier
                        .sharedBounds(
                            key = "${KEY_CODE_RECIPE_TITLE}-${codeRecipe.id}",
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        ),
                    text = codeRecipe.title,
                    textAlign = if (center) TextAlign.Center else TextAlign.Start,
                    style = MaterialTheme.typography.labelMedium
                        .copy(MaterialTheme.colorScheme.onSurface),
                )
            }
        }
    }
}
