package org.balch.recipes.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.balch.recipes.ui.theme.RecipesTheme

@Preview
@Composable
fun PushUpToRevealPreview() {
    RecipesTheme(darkTheme = true) {
        PushUpToRevealBox(
            modifier = Modifier.fillMaxSize(),
            revealContent = {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    colors = cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.95f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Revealed Content",
                            style = typography.titleSmall
                        )
                    }
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorScheme.background)
            ) {
                Text(
                    text = "Main Content (Pull Up)",
                    modifier = Modifier.align(Alignment.Center),
                    color = colorScheme.onBackground
                )
            }
        }
    }
}
