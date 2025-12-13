package org.balch.recipes.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import org.balch.recipes.R

@Composable
fun RecipeMaestroWidget(
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    iconTint: Color? = null,
) {
    Box(modifier = modifier) {
        val size = with(LocalDensity.current) { fontSize.toDp() }

        Icon(
            painter = painterResource(R.drawable.recipe_maestro),
            contentDescription = "Recipe Maestro",
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .size(size)
                    .clip(RoundedCornerShape(50))
                    .background(
                        color = iconTint?.copy(alpha = 0.4f) ?: Color.Transparent,
                        shape = RoundedCornerShape(50)
                    ),
            tint = Color.Unspecified
        )
    }
}
