package org.balch.recipes.di

import android.app.Application
import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.android.MetroAppComponentProviders
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

/**
 * Main application dependency graph for Metro DI.
 * This graph aggregates all contributions from @ContributesTo(AppScope::class) interfaces.
 */
@DependencyGraph(AppScope::class)
interface AppGraph : MetroAppComponentProviders, ViewModelGraph {
    @Provides fun provideApplicationContext(application: Application): Context = application

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides application: Application): AppGraph
    }
}
