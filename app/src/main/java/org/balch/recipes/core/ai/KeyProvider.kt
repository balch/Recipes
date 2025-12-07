package org.balch.recipes.core.ai

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import org.balch.recipes.BuildConfig


interface KeyProvider {

    val apiKey: String?
    val isApiKeySet: Boolean
        get() = (apiKey != null)
}

@SingleIn(AppScope::class)
class GeminiKeyProvider @Inject constructor():  KeyProvider {
    override val apiKey: String? = BuildConfig.GEMINI_API_KEY.takeIf { it.isNotEmpty() }
}