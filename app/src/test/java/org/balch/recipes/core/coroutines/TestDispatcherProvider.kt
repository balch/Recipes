package org.balch.recipes.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * A test implementation of the [DispatcherProvider] interface that provides a single [TestDispatcher]
 * instance for all coroutine dispatchers. This is typically used for testing purposes to control
 * the execution of coroutines and run them synchronously in tests.
 *
 * @param testDispatcher The [TestDispatcher] instance to be used for all coroutine dispatchers.
 * Defaults to [UnconfinedTestDispatcher], allowing immediate execution of coroutines.
 */
class TestDispatcherProvider @OptIn(ExperimentalCoroutinesApi::class) constructor(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : DispatcherProvider {
    override val main: CoroutineDispatcher = testDispatcher
    override val io: CoroutineDispatcher = testDispatcher
    override val default: CoroutineDispatcher = testDispatcher
    override val unconfined: CoroutineDispatcher = testDispatcher
}