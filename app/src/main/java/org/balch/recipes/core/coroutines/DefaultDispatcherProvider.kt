package org.balch.recipes.core.coroutines

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.balch.recipes.di.AppScope
import javax.inject.Inject

/**
 * Provides default implementations of [DispatcherProvider] to supply coroutine dispatchers.
 *
 * This class is marked as a singleton via Metro's @SingleIn, ensuring there is only one instance
 * throughout the lifecycle of the application. It provides the standard coroutine dispatchers:
 *
 * - [Dispatchers.Main]: A coroutine dispatcher confined to the main thread for UI-related tasks.
 * - [Dispatchers.IO]: A coroutine dispatcher optimized for IO-bound operations like file and network access.
 * - [Dispatchers.Default]: A coroutine dispatcher optimized for CPU-intensive work.
 * - [Dispatchers.Unconfined]: A coroutine dispatcher that is not confined to any specific thread.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultDispatcherProvider @Inject constructor() : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}