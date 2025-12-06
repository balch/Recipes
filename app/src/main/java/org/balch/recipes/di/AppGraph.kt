package org.balch.recipes.di

import android.content.Context
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import org.balch.recipes.MainActivity

/**
 * Main application dependency graph for Metro DI.
 * This graph aggregates all contributions from @ContributesTo(AppScope::class) interfaces.
 */
@SingleIn(AppScope::class)
@DependencyGraph(scope = AppScope::class)
interface AppGraph {
    /**
     * Member injection for MainActivity.
     */
    fun inject(activity: MainActivity)
    
    /**
     * Provides MetroViewModelFactory for Compose ViewModel injection.
     */
    val metroViewModelFactory: MetroViewModelFactory
    
    /**
     * Factory to create the AppGraph with application context.
     */
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides context: Context): AppGraph
    }
}
