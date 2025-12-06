package org.balch.recipes.features.agent.tools.meals

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.balch.recipes.core.models.Meal
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Reinforcement tool to remind the AI to create unique code recipes.
 */
class MealRecipeCreateTool @Inject internal constructor(
) : Tool<MealRecipeCreateTool.Args, MealRecipeCreateTool.Result>() {
    @Serializable
    data class Args(
        @property:LLMDescription("Optional contextual information from the calling agent.")
        val callingAgentContext: String?,

        @property:LLMDescription("The name of the meal or recipe")
        val name: String,

        @property:LLMDescription("The category this meal belongs to (e.g., 'Dessert', 'Seafood')")
        val category: String,

        @property:LLMDescription("The geographical area or cuisine type (e.g., 'Italian', 'Mexican')")
        val area: String,

        @property:LLMDescription("""            
        - Complete cooking instructions for preparing the meal. 
        - Use line breaks to delineate steps.
        - DO NOT prefix the steps with numbers.
        """
        )
        val instructions: String,

        @property:LLMDescription("Optional URL to the meal's thumbnail image")
        val thumbnail: String?,

        @property:LLMDescription("Optional YouTube video URL for the recipe tutorial")
        val youtube: String? = null,

        // Ingredients (up to 20)
        @property:LLMDescription("List of up to 20 ingredients/measure pairs")
        val ingredients: List<Pair<String,String>> = emptyList(),
    )

    @Serializable
    data class Result(
        @property:LLMDescription("Meal object used for displaying to the user")
        val meal: Meal,
    )

    override val argsSerializer = Args.serializer()
    override val resultSerializer: KSerializer<Result> = Result.serializer()

    override val name = "meal_recipe_create"
    override val description = "Returns a Meal for the ai to navigate to"

    @OptIn(ExperimentalTime::class)
    override suspend fun execute(args: Args): Result =
        Result(
            meal = Meal(
                id = CLOCK.now().epochSeconds.toString(),
                name = args.name,
                category = args.category,
                area = args.area,
                instructions = args.instructions,
                thumbnail = args.thumbnail,
                youtube = args.youtube,
                ingredient1 = args.ingredients.getOrNull(0)?.first,
                ingredient2 = args.ingredients.getOrNull(1)?.first,
                ingredient3 = args.ingredients.getOrNull(2)?.first,
                ingredient4 = args.ingredients.getOrNull(3)?.first,
                ingredient5 = args.ingredients.getOrNull(4)?.first,
                ingredient6 = args.ingredients.getOrNull(5)?.first,
                ingredient7 = args.ingredients.getOrNull(6)?.first,
                ingredient8 = args.ingredients.getOrNull(7)?.first,
                ingredient9 = args.ingredients.getOrNull(8)?.first,
                ingredient10 = args.ingredients.getOrNull(9)?.first,
                ingredient11 = args.ingredients.getOrNull(10)?.first,
                ingredient12 = args.ingredients.getOrNull(11)?.first,
                ingredient13 = args.ingredients.getOrNull(12)?.first,
                ingredient14 = args.ingredients.getOrNull(13)?.first,
                ingredient15 = args.ingredients.getOrNull(14)?.first,
                ingredient16 = args.ingredients.getOrNull(15)?.first,
                ingredient17 = args.ingredients.getOrNull(16)?.first,
                ingredient18 = args.ingredients.getOrNull(17)?.first,
                ingredient19 = args.ingredients.getOrNull(18)?.first,
                ingredient20 = args.ingredients.getOrNull(19)?.first,
                measure1 = args.ingredients.getOrNull(0)?.second,
                measure2 = args.ingredients.getOrNull(1)?.second,
                measure3 = args.ingredients.getOrNull(2)?.second,
                measure4 = args.ingredients.getOrNull(3)?.second,
                measure5 = args.ingredients.getOrNull(4)?.second,
                measure6 = args.ingredients.getOrNull(5)?.second,
                measure7 = args.ingredients.getOrNull(6)?.second,
                measure8 = args.ingredients.getOrNull(7)?.second,
                measure9 = args.ingredients.getOrNull(8)?.second,
                measure10 = args.ingredients.getOrNull(9)?.second,
                measure11 = args.ingredients.getOrNull(10)?.second,
                measure12 = args.ingredients.getOrNull(11)?.second,
                measure13 = args.ingredients.getOrNull(12)?.second,
                measure14 = args.ingredients.getOrNull(13)?.second,
                measure15 = args.ingredients.getOrNull(14)?.second,
                measure16 = args.ingredients.getOrNull(15)?.second,
                measure17 = args.ingredients.getOrNull(16)?.second,
                measure18 = args.ingredients.getOrNull(17)?.second,
                measure19 = args.ingredients.getOrNull(18)?.second,
                measure20 = args.ingredients.getOrNull(19)?.second,
            )
        )

    companion object {
        @OptIn(ExperimentalTime::class)
        private val CLOCK = Clock.System
    }

}
