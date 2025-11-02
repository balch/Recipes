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

sealed interface TopLevelRoute: NavKey {
    val icon: ImageVector
    val contentDescription: String
}

@Serializable
data object Ideas : TopLevelRoute {
    override val icon = Icons.Default.Lightbulb
    override val contentDescription = "Ideas"
}
@Serializable
data object Info : TopLevelRoute {
    override val icon = Icons.Default.Info
    override val contentDescription = "Info"
}

@Serializable
data class Search(val search: SearchType.Search) : TopLevelRoute {
    @Transient override val icon = Icons.Default.Search
    override val contentDescription = "Search"
}

@Serializable
data class DetailRoute(val detailType: DetailType) : NavKey

@Serializable
data class SearchRoute(
    val searchType: SearchType,
) : NavKey
