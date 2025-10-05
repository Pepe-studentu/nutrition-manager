package org.example.project.service

import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.io.File
import java.io.InputStream

/**
 * Manages data storage location and initial data seeding.
 *
 * Strategy:
 * - User data stored in ~/NutritionApp/data/ (safe from app updates)
 * - Initial foods.json shipped as app resource
 * - On first run, copy initial foods.json to user directory
 * - Other data files (meals, menus, settings) created empty on first run
 * - On subsequent runs, leave user data untouched
 */
class DataManager {

    companion object {
        // User data directory - safe from app updates
        private val userDataDir = File(System.getProperty("user.home"), "NutritionApp/data")

        // Main NutritionApp directory for user-accessible files
        private val nutritionAppDir = File(System.getProperty("user.home"), "NutritionApp")

        // Data files in user directory
        val foodsFile = File(userDataDir, "foods.json")
        val mealsFile = File(userDataDir, "meals.json")
        val multiDayMenusFile = File(userDataDir, "multi_day_menus.json")
        val settingsFile = File(userDataDir, "settings.json")

        // User-editable files in main directory
        val allergensFile = File(nutritionAppDir, "allergens.txt")

        // Resource paths for initial data
        private const val INITIAL_FOODS = "initial_foods.json"
        private const val INITIAL_MENU_TEMPLATE = "menu_template.html"
        private const val INITIAL_SIGNATURE_TEMPLATE = "signature_template.html"

        /**
         * Ensures user data directory exists and seeds initial data if needed.
         * Call this on app startup before loading data.
         */
        fun initializeUserData() {
            ensureUserDataDirectoryExists()
            ensureNutritionAppDirectoryExists()
            seedInitialFoodsIfNeeded()
            seedTemplateFilesIfNeeded()
            ensureAllergensFileExists()
        }

        private fun ensureUserDataDirectoryExists() {
            if (!userDataDir.exists()) {
                userDataDir.mkdirs()
                println("Created user data directory: ${userDataDir.absolutePath}")
            }
        }

        private fun ensureNutritionAppDirectoryExists() {
            if (!nutritionAppDir.exists()) {
                nutritionAppDir.mkdirs()
                println("Created NutritionApp directory: ${nutritionAppDir.absolutePath}")
            }
        }

        private fun seedInitialFoodsIfNeeded() {
            if (!foodsFile.exists()) {
                println("First run detected - seeding initial foods...")
                seedInitialFoodsData()
            }
        }

        private fun seedTemplateFilesIfNeeded() {
            val menuTemplateFile = File(nutritionAppDir, "menu_template.html")
            val signatureTemplateFile = File(nutritionAppDir, "signature_template.html")

            if (!menuTemplateFile.exists()) {
                println("Seeding initial menu template...")
                try {
                    copyResourceToFile(INITIAL_MENU_TEMPLATE, menuTemplateFile)
                } catch (e: Exception) {
                    println("Warning: Could not seed menu template: ${e.message}")
                }
            }

            if (!signatureTemplateFile.exists()) {
                println("Seeding initial signature template...")
                try {
                    copyResourceToFile(INITIAL_SIGNATURE_TEMPLATE, signatureTemplateFile)
                } catch (e: Exception) {
                    println("Warning: Could not seed signature template: ${e.message}")
                }
            }
        }

        private fun ensureAllergensFileExists() {
            if (!allergensFile.exists()) {
                val defaultAllergens = listOf(
                    "gluten",
                    "lactose",
                    "nuts",
                    "eggs",
                    "soy",
                    "mustard",
                    "shellfish"
                )
                allergensFile.writeText(defaultAllergens.joinToString("\n"))
                // Refresh allergen cache since we just created the file
                AllergenService.refreshAllergens()
            }
        }

        private fun seedInitialFoodsData() {
            try {
                // Try to copy initial foods.json from resources
                copyResourceToFile(INITIAL_FOODS, foodsFile)
                println("Initial data seeded successfully")
            } catch (e: Exception) {
                println("Warning: Could not seed initial foods data: ${e.message}")
                // Create empty foods file as fallback for now
                if (!foodsFile.exists()) foodsFile.writeText("[]")
                println("Created empty foods file as fallback")
            }

            // Create empty files for other data types
            if (!mealsFile.exists()) mealsFile.writeText("[]")
            if (!multiDayMenusFile.exists()) multiDayMenusFile.writeText("[]")
            if (!settingsFile.exists()) settingsFile.writeText("{}")
        }

        private fun copyResourceToFile(resourcePath: String, targetFile: File) {
            val inputStream: InputStream? = DataManager::class.java.classLoader.getResourceAsStream(resourcePath)

            if (inputStream != null) {
                inputStream.use { input ->
                    targetFile.writeBytes(input.readAllBytes())
                }
                println("Copied $resourcePath to ${targetFile.name}")
            } else {
                throw RuntimeException("Critical resource missing: $resourcePath not found in app bundle")
            }
        }

        /**
         * Gets the user data directory path for display to user
         */
        fun getUserDataPath(): String {
            return userDataDir.absolutePath
        }

        /**
         * Checks if user data directory exists and is accessible
         */
        fun isUserDataAccessible(): Boolean {
            return userDataDir.exists() && userDataDir.canRead() && userDataDir.canWrite()
        }
    }
}