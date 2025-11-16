package org.balch.recipes.core.ai

import org.balch.recipes.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton


interface KeyProvider {

    val apiKey: String?
    val isApiKeySet: Boolean
        get() = (apiKey != null)
}

@Singleton
class GeminiKeyProvider @Inject constructor():  KeyProvider {
    override val apiKey: String? = BuildConfig.GEMINI_API_KEY.takeIf { it.isNotEmpty() }
}