package org.balch.recipes.features.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.balch.recipes.core.coroutines.DispatcherProvider
import javax.inject.Inject

/**
 * ViewModel that provides a list of URLs to be displayed and supports user-triggered reloading of data.
 **/
@HiltViewModel
class InfoViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private fun randomList(): List<String> =
        listOf(
            "https://github.com/balch/Recipes?#recipe-reference-app",
            "https://www.themealdb.com/api.php"
        ).shuffled()

    private val reloadIntentFlow = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val uiState: StateFlow<List<String>> =
        reloadIntentFlow.map { randomList() }
            .flowOn(dispatcherProvider.default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = randomList()
        )

    fun retry() {
        reloadIntentFlow.tryEmit(Unit)
    }
}
