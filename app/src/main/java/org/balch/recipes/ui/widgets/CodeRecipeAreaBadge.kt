package org.balch.recipes.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import org.balch.recipes.core.models.CodeRecipe
import org.balch.recipes.core.models.color
import org.balch.recipes.core.models.textColor
import org.balch.recipes.ui.preview.CodeRecipeProvider
import org.balch.recipes.ui.theme.ThemePreview
import org.balch.recipes.ui.utils.TransitionKeySuffix.KEY_CODE_RECIPE_BADGE
import org.balch.recipes.ui.utils.sharedBounds

@ThemePreview
@Composable
fun CodeRecipeAreaBadge(
    @PreviewParameter(CodeRecipeProvider::class) codeRecipe: CodeRecipe,
    modifier: Modifier = Modifier,
    largeFont: Boolean = false,
) {
    Box(
        modifier = modifier
            .sharedBounds("${KEY_CODE_RECIPE_BADGE}-${codeRecipe.id}"),
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = codeRecipe.area.color(),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = codeRecipe.area.name,
                style =
                    if (largeFont) MaterialTheme.typography.titleLarge
                    else MaterialTheme.typography.titleSmall,
                color = codeRecipe.area.textColor(),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

