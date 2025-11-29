package org.balch.recipes

import ai.koog.agents.core.tools.annotations.LLMDescription
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
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
@Serializable
@LLMDescription("A route used to navigate in the Recipe App")
sealed interface RecipeRoute: NavKey {
    val contentDescription: String

    val showBottomNav: Boolean get() = true
}

/**
 * A route that should be placed in the Bottom Nav
 */
@Serializable
@LLMDescription("A route used for items in the bottom navigation bar")
sealed interface TopLevelRoute: NavItemRoute {
}

@Serializable
@LLMDescription("A route used for items in the bottom navigation bar")
sealed interface NavItemRoute: RecipeRoute {
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
@LLMDescription("The route for a single recipe detail page")
data class DetailRoute(
    @property:LLMDescription("The type of recipe to display")
    val detailType: DetailType
): RecipeRoute {
    override val contentDescription = "Detail"
}

@Serializable
data class SearchRoute(val searchType: SearchType) : RecipeRoute {
    override val contentDescription = "Search"
}
@Serializable
data object AiChatScreen : NavItemRoute {
    override val contentDescription = "AI"
    override val showBottomNav: Boolean = false
    override val icon: ImageVector = Icons.Filled.ChatBubble

}
