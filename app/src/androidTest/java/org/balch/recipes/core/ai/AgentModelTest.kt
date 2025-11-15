package org.balch.recipes.core.ai

import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.executor.clients.google.GoogleModels
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for Agent class to verify correct Gemini model names.
 * This test requires GEMINI_API_KEY to be set in local.properties.
 * 
 * Tests verify that:
 * 1. The correct model names are used (gemini-1.5-pro, gemini-1.5-flash)
 * 2. Messages can be sent successfully to both models
 * 3. Valid responses are received from the API
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AgentModelTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
        
        // Skip tests if API key is not configured
        assumeTrue(
            "GEMINI_API_KEY is not set. Skipping API integration tests.",
            Agent.isApiKeySet
        )
    }

    @Test
    fun testGeminiModel_sendsMessageSuccessfully() = runBlocking {
        println("[DEBUG_LOG] Testing GEMINI_PRO model")

        // Create a test agent using GEMINI_PRO model
        val agent = object : Agent() {
            override val systemInstruction: String = "You are a helpful assistant."
            override val agentModel: AgentModel = AgentModel.GEMINI_PRO
            override val toolRegistry: ToolRegistry  = ToolRegistry { }
        }

        // Verify the model is ready
        assertTrue("Agent model should be ready when API key is set", agent.isModelReady)
        println("[DEBUG_LOG] Agent model is ready: ${agent.isModelReady}")

        // Send a simple message to the model
        val testMessage = "Say 'Hello' in one word."
        println("[DEBUG_LOG] Sending test message: $testMessage")

        val response = agent.chat(testMessage)

        println("[DEBUG_LOG] Received response: $response")

        // Verify we got a non-empty response
        assertNotNull("Response should not be null", response)
        assertFalse("Response should not be empty", response.isEmpty())

        // Verify we didn't get an error message
        assertFalse(
            "Response should not contain error message",
            response.contains("I'm sorry, I encountered an error")
        )
        assertFalse(
            "Response should not contain failure message",
            response.contains("I couldn't generate a response")
        )

        println("[DEBUG_LOG] GEMINI_PRO test passed successfully")
    }


    @Test
    fun testGeminiProModel_sendsMessageSuccessfully() = runBlocking {
        println("[DEBUG_LOG] Testing GEMINI_PRO model")
        
        // Create a test agent using GEMINI_PRO model
        val agent = object : Agent() {
            override val systemInstruction: String = "You are a helpful assistant."
            override val agentModel: AgentModel = AgentModel.GEMINI_PRO
            override val toolRegistry: ToolRegistry  = ToolRegistry { }
        }

        // Verify the model is ready
        assertTrue("Agent model should be ready when API key is set", agent.isModelReady)
        println("[DEBUG_LOG] Agent model is ready: ${agent.isModelReady}")
        
        // Send a simple message to the model
        val testMessage = "Say 'Hello' in one word."
        println("[DEBUG_LOG] Sending test message: $testMessage")
        
        val response = agent.chat(testMessage)
        
        println("[DEBUG_LOG] Received response: $response")
        
        // Verify we got a non-empty response
        assertNotNull("Response should not be null", response)
        assertFalse("Response should not be empty", response.isEmpty())
        
        // Verify we didn't get an error message
        assertFalse(
            "Response should not contain error message",
            response.contains("I'm sorry, I encountered an error")
        )
        assertFalse(
            "Response should not contain failure message",
            response.contains("I couldn't generate a response")
        )
        
        println("[DEBUG_LOG] GEMINI_PRO test passed successfully")
    }

    @Test
    fun testGeminiFlashModel_sendsMessageSuccessfully() = runBlocking {
        println("[DEBUG_LOG] Testing GEMINI_FLASH model")
        
        // Create a test agent using GEMINI_FLASH model
        val agent = object : Agent() {
            override val systemInstruction: String = "You are a helpful assistant."
            override val agentModel: AgentModel = AgentModel.GEMINI_FLASH
            override val toolRegistry: ToolRegistry  = ToolRegistry { }
        }

        // Verify the model is ready
        assertTrue("Agent model should be ready when API key is set", agent.isModelReady)
        println("[DEBUG_LOG] Agent model is ready: ${agent.isModelReady}")
        
        // Send a simple message to the model
        val testMessage = "Say 'Hello' in one word."
        println("[DEBUG_LOG] Sending test message: $testMessage")
        
        val response = agent.chat(testMessage)
        
        println("[DEBUG_LOG] Received response: $response")
        
        // Verify we got a non-empty response
        assertNotNull("Response should not be null", response)
        assertFalse("Response should not be empty", response.isEmpty())
        
        // Verify we didn't get an error message
        assertFalse(
            "Response should not contain error message",
            response.contains("I'm sorry, I encountered an error")
        )
        assertFalse(
            "Response should not contain failure message",
            response.contains("I couldn't generate a response")
        )
        
        println("[DEBUG_LOG] GEMINI_FLASH test passed successfully")
    }

    @Test
    fun testModelNames_areCorrect() {
        println("[DEBUG_LOG] Verifying model names are correct")
        
        // Verify GEMINI_PRO uses the correct model name
        val proModelName = AgentModel.GEMINI_PRO.llm
        println("[DEBUG_LOG] GEMINI_PRO model name: $proModelName")
        assertTrue(
            "GEMINI_PRO uses GoogleModels.Gemini2_5Pro",
            proModelName == GoogleModels.Gemini2_5Pro
        )
        
        // Verify GEMINI_FLASH uses the correct model name
        val flashModelName = AgentModel.GEMINI_FLASH.llm
        println("[DEBUG_LOG] GEMINI_FLASH model name: $flashModelName")
        assertTrue(
            "GEMINI_FLASH uses GoogleModels.Gemini2_5Flash",
            flashModelName == GoogleModels.Gemini2_5Flash
        )
        
        println("[DEBUG_LOG] Model names verification passed")
    }
}
