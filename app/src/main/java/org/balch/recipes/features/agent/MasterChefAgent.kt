package org.balch.recipes.features.agent

import ai.koog.agents.core.tools.ToolRegistry
import androidx.navigation3.runtime.NavKey
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.balch.recipes.AI
import org.balch.recipes.DetailRoute
import org.balch.recipes.Ideas
import org.balch.recipes.Info
import org.balch.recipes.Search
import org.balch.recipes.SearchRoute
import org.balch.recipes.core.ai.Agent
import org.balch.recipes.core.ai.AgentModel
import org.balch.recipes.core.ai.tools.NavigationTools
import org.balch.recipes.core.models.DetailType
import org.balch.recipes.core.models.Meal
import org.balch.recipes.core.models.SearchType

/**
 * AI Agent specifically designed for culinary, nutrition, and recipe assistance.
 * Uses Gemini Pro 2.5 Flash to provide expert advice on recipes and food-related questions.
 */
@Singleton
class MasterChefAgent @Inject constructor(): Agent() {

    private val navigationTools = NavigationTools()

    val navigationFlow = navigationTools.navigationRoute

    override val agentModel: AgentModel = AgentModel.GEMINI_FLASH
    override val toolRegistry: ToolRegistry = ToolRegistry {
//        navigationTools
    }

    override val systemInstruction = """
        You are a master chef, culinary expert, nutritionist, and food scientist with decades of experience.
                
        Your role is to:
        1. Answer questions about specific recipes, ingredients, techniques, and nutrition
        2. Provide modifications to provided recipes to them healthier, spicier, vegetarian, etc.
        3. Suggest ingredient substitutions
        4. Explain cooking techniques and tips
        5. Provide nutritional information and dietary considerations
        
        Always be friendly, encouraging, and educational. Keep responses concise but informative.
    """.trimIndent()

    private fun mealInstruction(meal: Meal) = """
        You are currently helping a user with the following recipe:
        
        Recipe Name: ${meal.name}
        Category: ${meal.category}
        Area/Cuisine: ${meal.area}
        
        Ingredients:
        ${meal.ingredientsWithMeasures.joinToString("\n") { (ingredient, measure) -> "- $measure $ingredient" }}
        
        Instructions:
        ${meal.instructions}
        
        Your role is to:
        1. Answer questions about this recipe, its ingredients, techniques, and nutrition
        2. Provide modifications to make the recipe healthier, spicier, vegetarian, etc.
        
        When suggesting modifications to the recipe:
        - Be specific about ingredient changes with quantities
        - Explain why the modification achieves the desired goal
        - Consider flavor balance and cooking techniques
        - Provide clear, actionable instructions
    """.trimIndent()

    suspend fun chat(userMessage: String, meal: Meal? = null): String =
        super.chat(
            buildString {
                append(userMessage.trim())
                append("\n\n")
                meal?.let { append(mealInstruction(it)) }
            }
        )

    /**
     * Parse the AI response to detect if it contains a recipe modification
     * This is a simple implementation that can be enhanced based on response patterns
     */
    fun containsRecipeModification(response: String): Boolean {
        val modificationKeywords = listOf(
            "modified recipe",
            "new recipe",
            "updated recipe",
            "ingredients:",
            "instructions:",
            "replace",
            "substitute"
        )
        return modificationKeywords.any { keyword ->
            response.lowercase().contains(keyword)
        }
    }

    companion object {
        /**
         * Converts the current NavKey to a descriptive context string for the AI agent
         */
        fun NavKey.toContext(): String = when (this) {
            is Ideas -> "The user is currently browsing recipe ideas and categories"
            is Search -> "The user is currently searching for recipes with query: ${search.searchText}"
            is SearchRoute -> when (searchType) {
                is SearchType.Category -> "The user is browsing recipes in category: ${searchType.searchText}"
                is SearchType.Area -> "The user is browsing recipes from area: ${searchType.searchText}"
                is SearchType.Ingredient -> "The user is browsing recipes with ingredient: ${searchType.searchText}"
                is SearchType.Search -> "The user is searching for: ${searchType.searchText}"
            }
            is DetailRoute -> when (detailType) {
                is DetailType.MealLookup -> "The user is viewing a recipe: ${detailType.mealSummary.name}"
                is DetailType.MealContent -> "The user is viewing a recipe: ${detailType.meal.name}"
                is DetailType.RandomRecipe -> "The user is viewing a random recipe"
                is DetailType.CodeRecipeContent -> "The user is viewing code recipe: ${detailType.codeRecipe.title}"
            }
            is Info -> "The user is viewing the app information screen"
            is AI -> "The user is in the AI assistant screen"
            else -> "The user is browsing the Recipes app"
        }
    }
}