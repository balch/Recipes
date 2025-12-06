package org.balch.recipes.features.agent

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.balch.recipes.core.ai.GeminiKeyProvider
import org.balch.recipes.core.coroutines.DefaultDispatcherProvider
import org.balch.recipes.core.coroutines.DispatcherProvider
import org.balch.recipes.ui.theme.RecipesTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AgentScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()


    @Inject
    lateinit var config: RecipeMaestroConfig

    @Inject
    lateinit var keyProvider: GeminiKeyProvider

    @Inject
    lateinit var dispatcherProvider: DispatcherProvider

    private lateinit var testViewModel: AgentViewModel

    @Before
    fun setup() {
        hiltRule.inject()
        
        // Create a test ViewModel with mocked dependencies
        val testAgent = RecipeMaestroAgent(
            config = config,
            geminiKeyProvider = keyProvider,
            dispatcherProvider = dispatcherProvider,
        )
        testViewModel = AgentViewModel(
            agent = testAgent,
            dispatcherProvider = DefaultDispatcherProvider(),
        )
    }

    @SuppressLint("UnusedContentLambdaTargetStateParameter")
    @OptIn(ExperimentalSharedTransitionApi::class)
    @Test
    fun agentScreen_showsInitialMessage() {
        // Launch the Agent screen
        composeTestRule.setContent {
            RecipesTheme {
                SharedTransitionLayout {
                    AnimatedContent(targetState = true) {
                        AgentScreen(
                            viewModel = testViewModel,
                        )
                    }
                }
            }
        }
    }
}
