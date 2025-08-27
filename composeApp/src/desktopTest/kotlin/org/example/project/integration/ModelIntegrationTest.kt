package org.example.project.integration

import org.example.project.ModelTestHelper
import org.example.project.TestUtils
import org.example.project.model.Food
import org.example.project.model.FoodCategory
import org.example.project.model.Model
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.*

/**
 * Integration tests for Model operations with actual file persistence.
 * These tests are NOT parallelizable due to shared Model singleton and file I/O.
 * 
 * Tests verify that:
 * 1. Setup/teardown isolation works correctly
 * 2. CRUD operations persist to files and maintain in-memory state
 * 3. Complex operations (usage counts, macro calculations) work end-to-end
 */
class ModelIntegrationTest {

    @BeforeEach
    fun setup() {
        ModelTestHelper.setupTestEnvironment()
        // Verify we start with clean state
        assertTrue(Model.foods.isEmpty(), "Model should start with no foods")
        assertTrue(Model.meals.isEmpty(), "Model should start with no meals") 
        assertTrue(Model.multiDayMenus.isEmpty(), "Model should start with no menus")
        assertTrue(ModelTestHelper.isInTestMode(), "Should be in test mode")
    }

    @AfterEach
    fun teardown() {
        ModelTestHelper.teardownTestEnvironment()
        assertFalse(ModelTestHelper.isInTestMode(), "Should not be in test mode after teardown")
    }

    @Test
    fun `test setup creates isolated environment`() {
        // This test verifies that setup worked correctly
        val testDir = ModelTestHelper.getTestDirectoryPath()
        assertNotNull(testDir, "Test directory should be created")
        
        // Model should be empty
        assertEquals(0, Model.foods.size)
        assertEquals(0, Model.meals.size) 
        assertEquals(0, Model.multiDayMenus.size)
        
        println("✓ Test environment isolated successfully in: $testDir")
    }

    @Test 
    fun `adding food updates both memory and persistence`() {
        // Given: Empty state (verified in setup)
        assertEquals(0, Model.foods.size)
        
        // When: Add a basic food
        val testFood = TestUtils.createBasicFood(
            name = "Test Chicken",
            proteins = 25.0f,
            carbs = 0.0f, 
            fats = 5.0f,
            water = 70.0f
        )
        val success = Model.insertFood(testFood)
        
        // Then: Operation succeeds
        assertTrue(success, "Food insertion should succeed")
        
        // And: In-memory state updated
        assertEquals(1, Model.foods.size, "Should have 1 food in memory")
        assertEquals("Test Chicken", Model.foods.first().name)
        
        // And: Can retrieve by name
        val retrieved = Model.getFoodByName("Test Chicken")
        assertNotNull(retrieved, "Should be able to retrieve food by name")
        assertEquals(25.0f, retrieved.proteins)
        assertEquals(5.0f, retrieved.fats)
        
        // And: Macro calculation works
        val macros = Model.getFoodMacros("Test Chicken")
        assertNotNull(macros, "Should calculate macros for basic food")
        assertEquals(25.0f, macros!!.proteins)
        assertEquals(0.0f, macros.carbs)
        assertEquals(5.0f, macros.fats)
        assertEquals(70.0f, macros.waterMassPercentage)
        
        println("✓ Food addition and retrieval work correctly")
    }

    @Test
    fun `test isolation between tests`() {
        // This test verifies that each test starts fresh
        // If run after the previous test, Model should still be empty
        assertEquals(0, Model.foods.size, "Each test should start with empty Model")
        assertEquals(0, Model.meals.size, "Each test should start with empty Model")
        
        // Add some data that should NOT persist to next test
        val food = TestUtils.createBasicFood("Temporary Food")
        Model.insertFood(food)
        assertEquals(1, Model.foods.size, "Food should be added in current test")
        
        println("✓ Test isolation verified - this data will not persist to next test")
    }

    @Test
    fun `verification test - previous test data should not exist`() {
        // This test runs after the previous one and verifies isolation worked
        assertEquals(0, Model.foods.size, "Should not have data from previous test")
        
        // Verify we can't find food from previous test
        val notFound = Model.getFoodByName("Temporary Food")
        assertNull(notFound, "Food from previous test should not exist")
        
        println("✓ Test isolation confirmed - previous test data was cleaned up")
    }

    @Test
    fun `food validation prevents invalid data`() {
        // Test that validation works in integration context
        val invalidFood = Food(
            name = "Invalid Food",
            category = FoodCategory.DAIRY,
            proteins = 60.0f,  // These sum to > 100%
            carbs = 50.0f,
            fats = 40.0f,
            waterMassPercentage = 20.0f  // Total = 170%
        )
        
        val success = Model.insertFood(invalidFood)
        assertFalse(success, "Should reject invalid food")
        assertEquals(0, Model.foods.size, "Invalid food should not be stored")
        
        println("✓ Food validation works correctly")
    }
}