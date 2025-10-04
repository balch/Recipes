package org.balch.recipes.core.random

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RandomModule {

    @Binds
    @Singleton
    abstract fun bindRandomProvider(
        randomProviderImpl: RandomProviderImpl
    ): RandomProvider
}