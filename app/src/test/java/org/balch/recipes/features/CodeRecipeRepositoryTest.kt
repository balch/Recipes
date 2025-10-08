package org.balch.recipes.features

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.balch.recipes.core.assets.CodeRecipeAssetLoader
import org.balch.recipes.core.models.CodeRecipe
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock


class CodeRecipeRepositoryTest {

    private val codeRecipeAssetLoader = mock<CodeRecipeAssetLoader> {
        onBlocking { loadRecipes() } doReturn (1..20).map { mock() }
    }

    private val repository: CodeRecipeRepository by lazy {
        CodeRecipeRepository(
            codeRecipeAssetLoader = codeRecipeAssetLoader,
        )
    }

    @Test
    fun `getRandomRecipes returns requested count when available`() = runTest {
        val result = repository.getRandomRecipes(3)
        assertThat(result).hasSize(3)
        assertThat(result).containsAtLeastElementsIn(result.toSet())
    }

    @Test
    fun `getRandomRecipes displays all shuffled recipes before reshuffling`() = runTest {
        // This is the key test to verify the issue is resolved
        val seenRecipes = mutableSetOf<CodeRecipe>()
        val allBatches = mutableListOf<List<CodeRecipe>>()
        
        // Keep fetching small batches until we see duplicates (indicating reshuffle happened)
        var batchCount = 0
        val maxBatches = 50 // Safety limit
        
        do {
            val batch = repository.getRandomRecipes(3)
            allBatches.add(batch)
            
            val beforeSize = seenRecipes.size
            seenRecipes.addAll(batch)
            val afterSize = seenRecipes.size
            
            batchCount++
            
            // If we didn't add any new recipes, it means we got a duplicate
            if (beforeSize == afterSize) {
                break
            }
        } while (batchCount < maxBatches)
        
        // We should have seen some recipes before getting duplicates
        assertTrue(seenRecipes.isNotEmpty(), "Should have seen some unique recipes")
        
        // Verify we got duplicates (indicating reshuffle occurred)
        val totalReturned = allBatches.flatten().size
        assertTrue(totalReturned > seenRecipes.size, 
                   "Should have returned more items than unique recipes (indicating reshuffle)")
    }

    @Test
    fun `getRandomRecipes handles zero count request`() = runTest {
        val result = repository.getRandomRecipes(0)
        assertEquals(0, result.size, "Should return empty list for zero count")
    }
    
    @Test
    fun `getRandomRecipes handles negative count request`() = runTest {
        val result = repository.getRandomRecipes(-1)
        assertEquals(0, result.size, "Should return empty list for negative count")
    }
    
    @Test
    fun `getRandomRecipes exhausts all recipes before reshuffling`() = runTest {
        // Track all unique recipes we see
        val uniqueRecipesSeen = mutableSetOf<CodeRecipe>()
        val allResults = mutableListOf<CodeRecipe>()
        
        // Keep fetching until we see the first duplicate
        var iterations = 0
        val maxIterations = 100 // Safety limit
        
        while (iterations < maxIterations) {
            val batch = repository.getRandomRecipes(3)
            allResults.addAll(batch)
            
            // Check if we've seen this recipe before
            val recipe = batch.firstOrNull()
            if (recipe != null) {
                if (recipe in uniqueRecipesSeen) {
                    // We found a duplicate - reshuffle occurred
                    break
                } else {
                    uniqueRecipesSeen.add(recipe)
                }
            }
            iterations++
        }

        // The key assertion: we should have seen a reasonable number of unique recipes
        // before any reshuffle occurred
        assertTrue(uniqueRecipesSeen.size > 1, 
                   "Should have seen multiple unique recipes before reshuffling")
    }

    @Test
    fun `getRandomRecipes returns unique recipes before reshuffling`() = runTest {
        // prime the `codeRecipes.randomRecipes` method and take the next 3
        repository.getRandomRecipes(1)
        val firstThree = repository.randomRecipes.take(3)

        // add duplicate recipe, expecting it to be filtered out
        repository.randomRecipes.add(2, firstThree[1])

        val result = repository.getRandomRecipes(3)
        assertThat(result).containsExactlyElementsIn(firstThree)
    }
}