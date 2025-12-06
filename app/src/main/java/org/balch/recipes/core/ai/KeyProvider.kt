package org.balch.recipes.core.ai

import dev.zacsweers.metro.SingleIn
import org.balch.recipes.BuildConfig
import org.balch.recipes.di.AppScope
import javax.inject.Inject


interface KeyProvider {

    val apiKey: String?
    val isApiKeySet: Boolean
        get() = (apiKey != null)
}

@SingleIn(AppScope::class)
class GeminiKeyProvider @Inject constructor():  KeyProvider {
    override val apiKey: String? = BuildConfig.GEMINI_API_KEY.takeIf { it.isNotEmpty() }
}