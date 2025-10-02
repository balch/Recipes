package org.balch.recipes.ui.nav

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import com.diamondedge.logging.logging
import dagger.hilt.android.scopes.ActivityRetainedScoped
import org.balch.recipes.Ideas
import javax.inject.Inject


@ActivityRetainedScoped
class BackstackManager @Inject constructor() {
    private val _backstack : SnapshotStateList<NavKey> = mutableStateListOf(Ideas)

    val backstack: List<NavKey>
        get() = _backstack.toList()

    private val logger = logging(BackstackManager::class.simpleName)

    /**
     * Returns `true` if there is only one screen in the backstack
     * and the app will close when the back button is pressed
     */
    val isLastScreen: Boolean
        get() = _backstack.size == 1

    fun push(destination: NavKey){
        logger.d { "push: $destination" }
        _backstack.add(destination)
    }

    fun pop(){
        _backstack.removeLastOrNull()
            .also { logger.d { "pop: $it" } }
    }

    fun peek(): NavKey? =
        _backstack.lastOrNull()
            .also { logger.d { "pop: $it" } }
}