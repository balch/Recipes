package org.balch.recipes.core.assets

import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.json.Json
import org.balch.recipes.di.AppScope

@ContributesTo(AppScope::class)
interface AssetModule {

    companion object {
        @Provides
        @SingleIn(AppScope::class)
        fun provideJson(): Json {
            return Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        }
    }
}