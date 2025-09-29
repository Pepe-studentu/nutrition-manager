package org.example.project.service

import org.example.project.model.Food
import org.example.project.model.Model
import java.io.File

/**
 * Service for managing allergen detection based on food tags.
 * Reads allergen definitions from ~/NutritionApp/allergens.txt
 */
class AllergenService {

    companion object {
        private var cachedAllergens: Set<String>? = null

        /**
         * Checks if a food contains any allergen tags.
         * Uses effective tags which includes inheritance from components for compound foods.
         */
        fun isAllergenFood(food: Food): Boolean {
            val allergens = getAllergens()
            val effectiveTags = food.getEffectiveTags { foodName -> Model.getFoodByName(foodName) }
            return effectiveTags.any { tag -> allergens.contains(tag.lowercase()) }
        }

        /**
         * Gets the list of allergen tags from allergens.txt file.
         * Results are cached and refreshed when needed.
         */
        fun getAllergens(): Set<String> {
            if (cachedAllergens == null) {
                cachedAllergens = loadAllergensFromFile()
            }
            return cachedAllergens!!
        }

        /**
         * Forces a refresh of the allergens cache.
         * Call this if the allergens.txt file has been modified.
         */
        fun refreshAllergens() {
            cachedAllergens = null
        }

        private fun loadAllergensFromFile(): Set<String> {
            return try {
                if (DataManager.allergensFile.exists()) {
                    DataManager.allergensFile.readLines()
                        .map { it.trim().lowercase() }
                        .filter { it.isNotBlank() }
                        .toSet()
                } else {
                    emptySet()
                }
            } catch (e: Exception) {
                println("Warning: Could not read allergens file: ${e.message}")
                emptySet()
            }
        }
    }
}