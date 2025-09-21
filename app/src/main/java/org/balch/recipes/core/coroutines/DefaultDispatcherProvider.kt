package org.balch.recipes.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides default implementations of [DispatcherProvider] to supply coroutine dispatchers.
 *
 * This class is marked as a [Singleton], ensuring there is only one instance throughout
 * the lifecycle of the application. It provides the standard coroutine dispatchers:
 *
 * - [Dispatchers.Main]: A coroutine dispatcher confined to the main thread for UI-related tasks.
 * - [Dispatchers.IO]: A coroutine dispatcher optimized for IO-bound operations like file and network access.
 * - [Dispatchers.Default]: A coroutine dispatcher optimized for CPU-intensive work.
 * - [Dispatchers.Unconfined]: A coroutine dispatcher that is not confined to any specific thread.
 *
 * The [DefaultDispatcherProvider] implementation is designed to be injected wherever an instance
 * of [DispatcherProvider] is required, making it suitable for dependency injection frameworks
 * such as Dagger or Hilt.
 */
@Singleton
class DefaultDispatcherProvider @Inject constructor() : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}