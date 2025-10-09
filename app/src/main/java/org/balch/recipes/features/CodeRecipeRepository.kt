package org.balch.recipes.features

import androidx.annotation.VisibleForTesting
import com.diamondedge.logging.logging
import org.balch.recipes.core.assets.CodeRecipeAssetLoader
import org.balch.recipes.core.models.CodeRecipe
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CodeRecipeRepository @Inject constructor(
    private val codeRecipeAssetLoader: CodeRecipeAssetLoader,
) {

    private val logger = logging(this::class.simpleName)

    private lateinit var _rawRecipes: List<CodeRecipe>

    private suspend fun rawRecipes(): List<CodeRecipe> {
        if (!::_rawRecipes.isInitialized) {
            _rawRecipes = codeRecipeAssetLoader.loadRecipes()
            logger.d { "Loaded ${_rawRecipes.size} Code Recipes" }
        }
        return _rawRecipes
    }

    suspend fun searchRecipes(query: String): List<CodeRecipe> =
        rawRecipes().filter {
            it.title.contains(query, ignoreCase = true)
                || it.area.name.contains(query, ignoreCase = true)
        }.sortedBy { it.title }

    suspend fun sortedRecipes() =
        rawRecipes().sortedWith(
            compareBy<CodeRecipe> { it.area.name }
                .thenBy { it.title }
        ).also { logger.v { "Sorted Recipes: $it" } }

    @VisibleForTesting
    val randomRecipes = mutableListOf<CodeRecipe>()

    /**
     * Retrieves a specified number of random `CodeRecipe` objects.
     * The method attempts to return the requested count of recipes
     * and refills the pool of available random recipes if necessary.
     * All shuffled recipes are displayed before reshuffling occurs.
     *
     * @param count The number of `CodeRecipe` objects to retrieve.
     * @return A list of `CodeRecipe` objects, with a size of up to the specified count.
     */
    suspend fun getRandomRecipes(count: Int): List<CodeRecipe> {
        if (count <= 0) {
            logger.w { "Empty list returned for count: $count" }
            return emptyList()
        }

        if (randomRecipes.size < count) {
            randomRecipes.addAll(rawRecipes().shuffled())
            logger.d { "Reshuffled Code Recipes Pool" }
        }

        // use a Set to ensure unique entries when list roles over
        val result = mutableSetOf<CodeRecipe>()
        repeat(count) {
            var addedToResult = false
            while (!addedToResult) {
                val nextItem = randomRecipes.removeAt(0)
                addedToResult = result.add(nextItem)
                if (!addedToResult) {
                    // nextItem is in use, so add it to the end of the list
                    logger.d { "Edge Case Alert!!! - $nextItem already in use" }
                    randomRecipes.add(nextItem)
                }
            }
        }
        return result.toList()
            .also { list -> logger.v { "getRandomRecipes: ${list.map { it.title }}" } }
    }
}
