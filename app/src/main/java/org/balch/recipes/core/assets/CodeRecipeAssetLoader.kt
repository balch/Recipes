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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CodeRecipeAssetLoader @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val json: Json,
    private val dispatcherProvider: DispatcherProvider,
) {
    private val logger = logging(this::class.simpleName)

    suspend fun loadRecipes(): List<CodeRecipe> = withContext(dispatcherProvider.io) {
        try {
            val indexJson = context.assets.open("code-recipes/recipes-index.json")
                .bufferedReader()
                .use { it.readText() }

            val index = json.decodeFromString<RecipeIndex>(indexJson)
            index.recipes
                .mapIndexed { index, codeRecipeRaw ->
                    val (description, codeSnippet) = loadRecipeContent(codeRecipeRaw.bodyMarkdown)
                    codeRecipeRaw.toCodeRecipe(index + 1, description, codeSnippet)
                }
                .also { logger.d { "Loaded ${it.size} code recipes from assets" }
            }
        } catch (e: Exception) {
            logger.e(e) { "Failed to load code recipes from assets" }
            emptyList()
        }
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
private data class CodeRecipeRaw(
    val area: CodeArea,
    val title: String,
    val bodyMarkdown: String,
    val fileName: String? = null,
) {
    fun toCodeRecipe(
        index: Int,
        description: String,
        codeSnippet: String,
    ) = CodeRecipe(
        index = index,
        area = area,
        title = title,
        description = description,
        fileName = fileName,
        codeSnippet = codeSnippet,
    )
}

@Serializable
private data class RecipeIndex(
    val recipes: List<CodeRecipeRaw>
)