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

        // Data files in user directory
        val foodsFile = File(userDataDir, "foods.json")
        val mealsFile = File(userDataDir, "meals.json")
        val multiDayMenusFile = File(userDataDir, "multi_day_menus.json")
        val settingsFile = File(userDataDir, "settings.json")

        // Resource path for initial foods data
        private const val INITIAL_FOODS = "initial_foods.json"

        /**
         * Ensures user data directory exists and seeds initial data if needed.
         * Call this on app startup before loading data.
         */
        fun initializeUserData() {
            ensureUserDataDirectoryExists()
            seedInitialDataIfNeeded()
        }

        private fun ensureUserDataDirectoryExists() {
            if (!userDataDir.exists()) {
                userDataDir.mkdirs()
                println("Created user data directory: ${userDataDir.absolutePath}")
            }
        }

        private fun seedInitialDataIfNeeded() {
            // Only seed if no user data exists yet (first run or clean install)
            if (!hasExistingUserData()) {
                println("First run detected - seeding initial data...")
                seedInitialData()
            } else {
                println("Existing user data found - preserving user data")
            }
        }

        private fun hasExistingUserData(): Boolean {
            // Consider user data exists if foods.json exists (main indicator)
            return foodsFile.exists()
        }

        private fun seedInitialData() {
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