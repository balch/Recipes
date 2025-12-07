package org.balch.recipes.core.assets

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.json.Json

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