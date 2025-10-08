package org.balch.recipes.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Provides a contract for supplying coroutine dispatchers, promoting testability and consistency
 * by allowing different implementations for production and testing environments.
 *
 * This interface includes the following dispatchers:
 *
 * - `main`: Typically used for tasks targeting the UI thread.
 * - `io`: Optimized for IO-bound tasks like network requests or file operations.
 * - `default`: Suitable for CPU-intensive work or background processing.
 * - `unconfined`: Not confined to any specific thread, often used for advanced scenarios.
 *
 * Implementations of this interface can be used in conjunction with dependency injection frameworks
 * to manage and configure coroutine dispatcher assignments effectively.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}