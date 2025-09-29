package org.example.project

import org.example.project.model.Model
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

/**
 * Test helper for managing Model singleton state and file isolation during testing.
 * Handles setup/teardown for integration tests that need clean Model state.
 */
object ModelTestHelper {
    private var testDirectory: Path? = null
    private var originalFoodsFile: File? = null
    private var originalMealsFile: File? = null
    private var originalMultiDayMenusFile: File? = null
    
    /**
     * Sets up isolated test environment with temporary files.
     * Call this in @BeforeEach to ensure each test starts clean.
     */
    fun setupTestEnvironment() {
        // Create temporary directory for this test session
        testDirectory = Files.createTempDirectory("model_integration_test_")
        
        // Store references to original files (if they exist)
        backupOriginalFiles()
        
        // Redirect Model to use test files
        val testFoodsFile = testDirectory!!.resolve("test_foods.json").toFile()
        val testMealsFile = testDirectory!!.resolve("test_meals.json").toFile()
        val testMultiDayMenusFile = testDirectory!!.resolve("test_multi_day_menus.json").toFile()
        
        Model.setTestFilePaths(testFoodsFile, testMealsFile, testMultiDayMenusFile)
        
        // Clear Model's in-memory state to start fresh
        Model.clearAllData()
    }
    
    /**
     * Cleans up test environment and restores original state.
     * Call this in @AfterEach to prevent test contamination.
     */
    @OptIn(ExperimentalPathApi::class)
    fun teardownTestEnvironment() {
        try {
            // Delete temporary test directory and files
            testDirectory?.deleteRecursively()
            
            // Restore Model to use original files
            restoreOriginalFiles()
            
            // Reload original data if files existed
            if (originalFoodsFile?.exists() == true || 
                originalMealsFile?.exists() == true || 
                originalMultiDayMenusFile?.exists() == true) {
                Model.loadFromFiles()
            } else {
                // If no original files, ensure Model is clean
                Model.clearAllData()
            }
        } finally {
            // Reset references
            testDirectory = null
            originalFoodsFile = null
            originalMealsFile = null
            originalMultiDayMenusFile = null
        }
    }
    
    private fun backupOriginalFiles() {
        // Note: We're not copying file contents, just storing references
        // The Model will handle file path switching, we just need to restore paths
        originalFoodsFile = File("foods.json")
        originalMealsFile = File("meals.json")
        originalMultiDayMenusFile = File("multi_day_menus.json")
    }
    
    private fun restoreOriginalFiles() {
        originalFoodsFile?.let { foodsFile ->
            originalMealsFile?.let { mealsFile ->
                originalMultiDayMenusFile?.let { multiDayMenusFile ->
                    Model.setTestFilePaths(foodsFile, mealsFile, multiDayMenusFile)
                }
            }
        }
    }
    
    /**
     * Utility to get the current test directory path for debugging
     */
    fun getTestDirectoryPath(): String? = testDirectory?.toString()
    
    /**
     * Utility to check if we're currently in test mode
     */
    fun isInTestMode(): Boolean = testDirectory != null
}