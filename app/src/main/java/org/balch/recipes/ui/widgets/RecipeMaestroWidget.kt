package org.balch.recipes.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import org.balch.recipes.ui.utils.sharedBounds


@Composable
fun RecipeMaestroWidget(
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    iconTint: Color? = null,
) {
    Box(
        modifier = modifier
            .sharedBounds("RecipeMaestroWidget")
    ) {
        Text(
            text = "👨‍🍳",
            fontSize = fontSize,
            modifier = Modifier
                .align(Alignment.Center)
                .background(
                    color = iconTint?.copy(alpha = 0.4f) ?: Color.Transparent,
                    shape = RoundedCornerShape(50)
                )
        )
    }
}
