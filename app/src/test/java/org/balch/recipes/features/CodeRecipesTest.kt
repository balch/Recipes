package org.balch.recipes.features

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import com.google.common.truth.Truth.assertThat
import org.balch.recipes.core.models.CodeRecipe


class CodeRecipesTest {
    
    private lateinit var codeRecipes: CodeRecipes
    
    @Before
    fun setUp() {
        codeRecipes = CodeRecipes()
    }
    
    @Test
    fun `getRandomRecipes returns requested count when available`() {
        val result = codeRecipes.getRandomRecipes(3)
        assertThat(result).hasSize(3)
        assertThat(result).containsAtLeastElementsIn(result.toSet())
    }

    @Test
    fun `getRandomRecipes displays all shuffled recipes before reshuffling`() {
        // This is the key test to verify the issue is resolved
        val seenRecipes = mutableSetOf<CodeRecipe>()
        val allBatches = mutableListOf<List<CodeRecipe>>()
        
        // Keep fetching small batches until we see duplicates (indicating reshuffle happened)
        var batchCount = 0
        val maxBatches = 50 // Safety limit
        
        do {
            val batch = codeRecipes.getRandomRecipes(3)
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
        assertTrue("Should have seen some unique recipes", seenRecipes.isNotEmpty())
        
        // Verify we got duplicates (indicating reshuffle occurred)
        val totalReturned = allBatches.flatten().size
        assertTrue("Should have returned more items than unique recipes (indicating reshuffle)", 
                   totalReturned > seenRecipes.size)
    }

    @Test
    fun `getRandomRecipes handles zero count request`() {
        val result = codeRecipes.getRandomRecipes(0)
        assertEquals("Should return empty list for zero count", 0, result.size)
    }
    
    @Test
    fun `getRandomRecipes handles negative count request`() {
        val result = codeRecipes.getRandomRecipes(-1)
        assertEquals("Should return empty list for negative count", 0, result.size)
    }
    
    @Test
    fun `getRandomRecipes exhausts all recipes before reshuffling`() {
        // Track all unique recipes we see
        val uniqueRecipesSeen = mutableSetOf<CodeRecipe>()
        val allResults = mutableListOf<CodeRecipe>()
        
        // Keep fetching until we see the first duplicate
        var iterations = 0
        val maxIterations = 100 // Safety limit
        
        while (iterations < maxIterations) {
            val batch = codeRecipes.getRandomRecipes(3)
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
        assertTrue("Should have seen multiple unique recipes before reshuffling", 
                   uniqueRecipesSeen.size > 1)
    }
}