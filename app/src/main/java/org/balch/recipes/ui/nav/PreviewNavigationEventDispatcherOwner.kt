package org.balch.recipes.ui.nav

import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner

/**
 * An implementation of [NavigationEventDispatcherOwner] for use in previews.
 */
class PreviewNavigationEventDispatcherOwner : NavigationEventDispatcherOwner {
    override val navigationEventDispatcher: NavigationEventDispatcher = NavigationEventDispatcher()
}
