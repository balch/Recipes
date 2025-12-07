package org.balch.recipes.core.random

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.random.Random

/**
 * Interface representing a random number generator.
 * Used for proper testing of code that uses random numbers.
 */
interface RandomProvider {
    fun nextFloat(): Float
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class RandomProviderImpl @Inject constructor() : RandomProvider {
    override fun nextFloat(): Float = Random.nextFloat()
}