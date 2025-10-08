package org.balch.recipes.core.random

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Interface representing a random number generator.
 * Used for proper testing of code that uses random numbers.
 */
interface RandomProvider {
    fun nextFloat(): Float
}

@Singleton
class RandomProviderImpl @Inject constructor() : RandomProvider {
    override fun nextFloat(): Float = Random.nextFloat()
}