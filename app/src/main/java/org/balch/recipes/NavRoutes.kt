package org.balch.recipes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.balch.recipes.core.models.DetailType
import org.balch.recipes.core.models.SearchType

/**
 * A Navigable Route in the Recipe App
 */
sealed interface RecipeRoute: NavKey {
    val contentDescription: String
}

/**
 * A route that should be placed in the Bottom Nav
 */
sealed interface TopLevelRoute: RecipeRoute {
    val icon: ImageVector
}

@Serializable
data object Ideas : TopLevelRoute {
    override val icon = Icons.Filled.Lightbulb
    override val contentDescription = "Ideas"
}

@Serializable
data object Info : TopLevelRoute {
    override val icon = Icons.Filled.Info
    override val contentDescription = "Info"
}

@Serializable
data class Search(val search: SearchType.Search) : TopLevelRoute {
    @Transient override val icon = Icons.Filled.Search
    override val contentDescription = "Search"
}

@Serializable
data class DetailRoute(val detailType: DetailType): RecipeRoute {
    override val contentDescription = "Detail"
}

@Serializable
data class SearchRoute(val searchType: SearchType) : RecipeRoute {
    override val contentDescription = "Search"
}
@Serializable
data object AiChatScreen : RecipeRoute {
    override val contentDescription = "AI"
}
