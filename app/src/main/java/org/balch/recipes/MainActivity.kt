package org.balch.recipes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import dagger.hilt.android.AndroidEntryPoint
import org.balch.recipes.core.ai.GeminiKeyProvider
import org.balch.recipes.core.navigation.NavigationRouter
import org.balch.recipes.ui.MainContent
import org.balch.recipes.ui.utils.setEdgeToEdgeConfig
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var geminiKeyProvider: GeminiKeyProvider

    @Inject
    lateinit var navigationRouter: NavigationRouter

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            MainContent(
                isAgentEnabled = geminiKeyProvider.isApiKeySet,
                activityViewModelStoreOwner = this@MainActivity,
                navigationRouter = navigationRouter
            )
        }
    }
}
