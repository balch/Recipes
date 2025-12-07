package org.balch.recipes

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.android.ActivityKey
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import org.balch.recipes.core.ai.GeminiKeyProvider
import org.balch.recipes.core.navigation.NavigationRouter
import org.balch.recipes.ui.MainContent
import org.balch.recipes.ui.utils.setEdgeToEdgeConfig

@ContributesIntoMap(AppScope::class, binding<Activity>())
@ActivityKey(MainActivity::class)
@Inject
class MainActivity(
    private val metroVmf: MetroViewModelFactory,
    private val geminiKeyProvider: GeminiKeyProvider,
    private val navigationRouter: NavigationRouter
) : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            MainContent(
                metroVmf = metroVmf,
                isAgentEnabled = geminiKeyProvider.isApiKeySet,
                navigationRouter = navigationRouter,
            )
        }
    }
}
