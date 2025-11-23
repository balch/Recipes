package org.balch.recipes.core.assets

import android.content.Context
import com.diamondedge.logging.logging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.balch.recipes.core.coroutines.DispatcherProvider
import org.balch.recipes.core.models.CodeArea
import org.balch.recipes.core.models.CodeRecipe
import org.balch.recipes.core.models.CodeRecipeSummary
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CodeRecipeAssetLoader @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val json: Json,
    private val dispatcherProvider: DispatcherProvider,
) {
    private val logger = logging(this::class.simpleName)

    suspend fun loadRecipesAreaMap(): Map<CodeArea, List<CodeRecipeSummary>> = withContext(dispatcherProvider.io) {
        loadRecipesRaw()
            .groupBy(
                keySelector = { it.area },
                valueTransform = { it }
            )
    }

    suspend fun getCodeRecipe(recipeSummary: CodeRecipeSummary): CodeRecipe = withContext(dispatcherProvider.io) {
        recipeSummary.loadRecipe(0)
    }

    private suspend fun loadRecipesRaw(): List<CodeRecipeSummary> = withContext(dispatcherProvider.io) {
        try {
            val indexJson = context.assets.open("code-recipes/recipes-index.json")
                .bufferedReader()
                .use { it.readText() }

            json.decodeFromString<RecipeIndex>(indexJson).recipes
        } catch (e: Exception) {
            logger.e(e) { "Failed to load code recipes from assets" }
            emptyList()
        }
    }

    suspend fun loadRecipes(): List<CodeRecipe> = withContext(dispatcherProvider.io) {
        try {
            loadRecipesRaw()
                .mapIndexed { index, codeRecipeRaw -> codeRecipeRaw.loadRecipe(index) }
                .also { logger.d { "Loaded ${it.size} code recipes from assets" }
            }
        } catch (e: Exception) {
            logger.e(e) { "Failed to load code recipes from assets" }
            emptyList()
        }
    }

    private suspend fun CodeRecipeSummary.loadRecipe(index: Int) =
        loadRecipeContent(markdownAsset)
            .let { (description, codeSnippet) ->
                toCodeRecipe(index + 1, description, codeSnippet)
            }

    suspend fun loadRecipeContent(bodyMarkdown: String): Pair<String, String> = withContext(Dispatchers.IO) {
        try {
            val content = context.assets.open("code-recipes/$bodyMarkdown")
                .bufferedReader()
                .use { it.readText() }

            parseMarkdownContent(content)
        } catch (e: Exception) {
            logger.e(e) { "Failed to load content from $bodyMarkdown" }
            "???" to "???"
        }
    }

    private fun parseMarkdownContent(markdown: String): Pair<String, String> {
        val sections = markdown.split("## Code Snippet")
        val description = sections.first()
            .removePrefix("## Description")
            .trim()

        val codeSnippet = if (sections.size > 1) {
            sections[1].trim()
        } else ""

        return description to codeSnippet
    }
}

@Serializable
private data class RecipeIndex(
    val recipes: List<CodeRecipeSummary>
)