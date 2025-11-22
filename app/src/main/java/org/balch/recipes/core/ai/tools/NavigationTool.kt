package org.balch.recipes.core.ai.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.Tool
import com.diamondedge.logging.logging
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.balch.recipes.DetailRoute
import org.balch.recipes.Info
import org.balch.recipes.RecipeRoute
import org.balch.recipes.core.models.CodeArea
import org.balch.recipes.core.models.DetailType
import org.balch.recipes.features.agent.ai.code.CodeRecipeSearchTool
import javax.inject.Inject
import javax.inject.Singleton

@LLMDescription("Navigate to a Screen in the App from a RecipeRoute")
@Singleton
class NavigationTool @Inject constructor()
    : Tool<NavigationTool.Args, NavigationTool.Result>() {

    private val _navigationRoute = MutableSharedFlow<RecipeRoute>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val logger = logging("NavigationTool")

    val navigationRoute: SharedFlow<RecipeRoute> = _navigationRoute

    @Serializable
    data class Args(
        @property:LLMDescription("A route used to navigate the app to")
        val recipeRoute: RecipeRoute
    )

    @Serializable
    data class Result(
        @property:LLMDescription("A boolean indicating if the navigation was successful")
        val wasSuccessful: Boolean,
    )

    override val argsSerializer = Args.serializer()
    override val resultSerializer: KSerializer<Result> = Result.serializer()

    override val name = "app_navigation"
    override val description = """
        Allows the agent to navigate to a screen based on a user request. If 
        the agent has an object that can be navigated to (e.g. a codeRecipe or Meal), the
        this tool can be used to navigate to the screen that displays that object.
        
        Example RecipeRoutes: 
        DetailRoute(DetailType.CodeRecipeContent(codeRecipe))
        DetailRoute(DetailType.MealContent(meal))
        Info       
    """.trimIndent()


    override suspend fun execute(args: Args): Result =
        Result(_navigationRoute.tryEmit(args.recipeRoute)).also {
            logger.d { "Navigation to ${args.recipeRoute} was ${if (it.wasSuccessful) "successful" else "unsuccessful"}" }
        }
}