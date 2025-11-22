package org.balch.recipes.features.agent.ai

/**
 * Holds the contextual data based on the users
 * current screen and content
 */
data class AppContextData(
    val displayText: String,
    val prompt: String,
)