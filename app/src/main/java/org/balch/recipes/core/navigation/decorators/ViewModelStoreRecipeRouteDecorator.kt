package org.balch.recipes.core.navigation.decorators

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.navigation3.ViewModelStoreNavEntryDecoratorDefaults
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.compose.LocalSavedStateRegistryOwner

/**
 * A [NavEntryDecorator] that optionally creates a [ViewModelStore] for each [NavEntry].
 * The [Naventry.contentKey] is passed to a callback to determine whether a [ViewModelStore] should be created
 * for the [NavEntry].
 *
 * This pattern was derived from:
 * [androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator]
 *
 * This optimizes the original functionality to retain ViewModelStores as needed.
 * Still evaluation this solution as there may be other ways to achieve the same results
 *    - rememberSavable (screen data too large to persist)
 *    - declaring ViewModels outside of NavEntry (wastes child viewmodels)
 */
@Composable
fun <T : Any> rememberViewModelStoreRecipeRouteDecorator(
    viewModelStoreOwner: ViewModelStoreOwner =
        checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        },
    removeViewModelStoreOnPop: () -> Boolean =
        ViewModelStoreNavEntryDecoratorDefaults.removeViewModelStoreOnPop(),
    createChildViewModel: (Any) -> Boolean
): ViewModelStoreRecipeRouteDecorator<T> {
    val currentRemoveViewModelStoreOnPop = rememberUpdatedState(
        removeViewModelStoreOnPop
    )

    return remember(viewModelStoreOwner, currentRemoveViewModelStoreOnPop) {
        ViewModelStoreRecipeRouteDecorator(
            viewModelStore = viewModelStoreOwner.viewModelStore,
            removeViewModelStoreOnPop = removeViewModelStoreOnPop,
            createChildViewModel = createChildViewModel
        )
    }
}

class ViewModelStoreRecipeRouteDecorator<T : Any> internal constructor(
    viewModelStore: ViewModelStore,
    private val removeViewModelStoreOnPop: () -> Boolean,
    private val createChildViewModel: (Any) -> Boolean,
) : NavEntryDecorator<T>(
    onPop = { key ->
        if (createChildViewModel(key) && removeViewModelStoreOnPop()) {
            viewModelStore.getEntryViewModel().clearViewModelStoreOwnerForKey(key)
        }
    },
    decorate = { entry ->
        if (createChildViewModel(entry.contentKey)) {
            val viewModelStore =
                viewModelStore.getEntryViewModel().viewModelStoreForKey(entry.contentKey)

            val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
            val childViewModelStoreOwner = remember(viewModelStore, savedStateRegistryOwner) {
                object :
                    ViewModelStoreOwner,
                    SavedStateRegistryOwner by savedStateRegistryOwner,
                    HasDefaultViewModelProviderFactory {
                    override val viewModelStore: ViewModelStore
                        get() = viewModelStore

                    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
                        get() = SavedStateViewModelFactory()

                    override val defaultViewModelCreationExtras: CreationExtras
                        get() =
                            MutableCreationExtras().also {
                                it[SAVED_STATE_REGISTRY_OWNER_KEY] = this
                                it[VIEW_MODEL_STORE_OWNER_KEY] = this
                            }

                    init {
                        require(this.lifecycle.currentState == Lifecycle.State.INITIALIZED) {
                            "The Lifecycle state is already beyond INITIALIZED. The " +
                                    "ViewModelStoreNavEntryDecorator requires adding the " +
                                    "SavedStateNavEntryDecorator to ensure support for " +
                                    "SavedStateHandles."
                        }
                        enableSavedStateHandles()
                    }
                }
            }
            CompositionLocalProvider(LocalViewModelStoreOwner provides childViewModelStoreOwner) {
                entry.Content()
            }
        } else {
            entry.Content()
        }
    },
)

private class EntryViewModel : ViewModel() {
    private val owners = mutableMapOf<Any, ViewModelStore>()

    fun viewModelStoreForKey(key: Any): ViewModelStore = owners.getOrPut(key) { ViewModelStore() }

    fun clearViewModelStoreOwnerForKey(key: Any) {
        owners.remove(key)?.clear()
    }

    override fun onCleared() {
        owners.forEach { (_, store) -> store.clear() }
    }
}

private fun ViewModelStore.getEntryViewModel(): EntryViewModel {
    val provider =
        ViewModelProvider.create(
            store = this,
            factory = viewModelFactory { initializer { EntryViewModel() } },
        )
    return provider[EntryViewModel::class]
}
