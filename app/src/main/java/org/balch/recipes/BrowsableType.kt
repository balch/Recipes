package org.balch.recipes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FoodBank
import androidx.compose.ui.graphics.vector.ImageVector

enum class BrowsableType(
    val displayName: String,
    val imageVector: ImageVector
) {
    Area("Cuisine", Icons.Default.Flag),
    Category("Category", Icons.Default.Category),
    Ingredient("Ingredient", Icons.Default.FoodBank),
    CodeRecipe("Code", Icons.Default.Code),
}