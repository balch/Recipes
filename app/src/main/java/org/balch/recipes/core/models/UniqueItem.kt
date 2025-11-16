package org.balch.recipes.core.models

import ai.koog.agents.core.tools.annotations.LLMDescription

/**
 * An interface to represent entities with a unique identifier.
 *
 * Classes implementing this interface are required to have a unique `id` property.
 * This is useful for identifying and differentiating entities within a system.
 */
@LLMDescription("An interface for entities that have a unique identifier")
interface UniqueItem {
    @property:LLMDescription("Unique identifier string for this item")
    val id: String
}