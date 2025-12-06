package org.balch.recipes

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import coil3.util.Logger
import com.diamondedge.logging.KmLogging
import com.diamondedge.logging.LogLevel
import dev.zacsweers.metro.createGraphFactory
import org.balch.recipes.di.AppGraph

class RecipesApplication : Application(), SingletonImageLoader.Factory {

    lateinit var graph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        
        // Create the Metro dependency graph
        graph = createGraphFactory<AppGraph.Factory>().create(this)
        
        KmLogging.setLogLevel(if (BuildConfig.DEBUG) LogLevel.Debug else LogLevel.Off)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory())
            }
            .logger(DebugLogger(Logger.Level.Info))
            .crossfade(true)
            .build()

    }
}