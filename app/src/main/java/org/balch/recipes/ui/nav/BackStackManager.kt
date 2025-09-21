package org.balch.recipes.ui.nav

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import com.diamondedge.logging.logging
import dagger.hilt.android.scopes.ActivityRetainedScoped
import org.balch.recipes.Ideas
import javax.inject.Inject


@ActivityRetainedScoped
class BackStackManager @Inject constructor() {
    private val _backStack : SnapshotStateList<NavKey> = mutableStateListOf(Ideas)

    val backStack: List<NavKey>
        get() = _backStack.toList()

    private val logger = logging("BackStackManager")

    fun push(destination: NavKey){
        logger.d { "push: $destination" }
        _backStack.add(destination)
    }

    fun pop(){
        _backStack.removeLastOrNull()
            .also { logger.d { "pop: $it" } }
    }

    fun peek(): NavKey? =
        _backStack.lastOrNull()
            .also { logger.d { "pop: $it" } }
}