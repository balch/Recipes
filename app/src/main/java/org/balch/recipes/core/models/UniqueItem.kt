package org.balch.recipes.core.models

/**
 * An interface to represent entities with a unique identifier.
 *
 * Classes implementing this interface are required to have a unique `id` property.
 * This is useful for identifying and differentiating entities within a system.
 */
interface UniqueItem {
    val id: String
}