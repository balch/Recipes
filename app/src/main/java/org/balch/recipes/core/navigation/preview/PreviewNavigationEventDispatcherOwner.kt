package org.balch.recipes.core.navigation.preview

import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner

/**
 * An implementation of [androidx.navigationevent.NavigationEventDispatcherOwner] for use in previews.
 */
class PreviewNavigationEventDispatcherOwner : NavigationEventDispatcherOwner {
    override val navigationEventDispatcher: NavigationEventDispatcher = NavigationEventDispatcher()
}