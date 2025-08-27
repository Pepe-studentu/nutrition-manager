package org.example.project.model

import org.example.project.TestUtils
import kotlin.test.*

class MealTest {

    @Test
    fun `create Meal with valid data`() {
        val sizedFoods = listOf(
            SizedFood("Chicken Breast", 150.0f),
            SizedFood("Rice", 100.0f)
        )
        
        val meal = Meal(
            id = "meal-123",
            description = "Chicken and Rice",
            foods = sizedFoods
        )
        
        assertEquals("meal-123", meal.id)
        assertEquals("Chicken and Rice", meal.description)
        assertEquals(2, meal.foods.size)
        assertEquals("Chicken Breast", meal.foods[0].foodName)
        assertEquals(150.0f, meal.foods[0].grams)
    }

    @Test
    fun `create empty Meal`() {
        val emptyMeal = Meal(
            id = "empty-meal",
            description = "Empty Meal",
            foods = emptyList()
        )
        
        assertEquals("empty-meal", emptyMeal.id)
        assertEquals("Empty Meal", emptyMeal.description)
        assertTrue(emptyMeal.foods.isEmpty())
    }

    @Test
    fun `Meal equality works correctly`() {
        val foods = listOf(SizedFood("Rice", 100.0f))
        
        val meal1 = Meal("meal-1", "Test Meal", foods)
        val meal2 = Meal("meal-1", "Test Meal", foods)
        val meal3 = Meal("meal-2", "Test Meal", foods)
        val meal4 = Meal("meal-1", "Different Description", foods)
        
        assertEquals(meal1, meal2)
        assertNotEquals(meal1, meal3) // Different ID
        assertNotEquals(meal1, meal4) // Different description
    }

    @Test
    fun `Meal copy functionality works`() {
        val originalFoods = listOf(SizedFood("Original Food", 100.0f))
        val original = Meal("original-id", "Original Meal", originalFoods)
        
        val copied = original.copy()
        val modifiedCopy = original.copy(description = "Modified Meal")
        val newFoods = listOf(SizedFood("New Food", 150.0f))
        val foodsCopy = original.copy(foods = newFoods)
        
        assertEquals(original, copied)
        assertEquals("original-id", modifiedCopy.id)
        assertEquals("Modified Meal", modifiedCopy.description)
        assertEquals(originalFoods, modifiedCopy.foods)
        assertEquals("New Food", foodsCopy.foods[0].foodName)
    }

    @Test
    fun `Meal toString contains relevant information`() {
        val meal = Meal(
            id = "test-id",
            description = "Test Description",
            foods = listOf(SizedFood("Test Food", 100.0f))
        )
        val toString = meal.toString()
        
        assertTrue(toString.contains("test-id"))
        assertTrue(toString.contains("Test Description"))
    }

    @Test
    fun `TestUtils createMeal works correctly`() {
        val testMeal = TestUtils.createMeal(
            id = "custom-id",
            description = "Custom Meal",
            foods = listOf(SizedFood("Custom Food", 200.0f))
        )
        
        assertEquals("custom-id", testMeal.id)
        assertEquals("Custom Meal", testMeal.description)
        assertEquals(1, testMeal.foods.size)
        assertEquals("Custom Food", testMeal.foods[0].foodName)
        assertEquals(200.0f, testMeal.foods[0].grams)
    }

    @Test
    fun `TestUtils createMeal with defaults works`() {
        val defaultMeal = TestUtils.createMeal()
        
        assertEquals("test-meal-id", defaultMeal.id)
        assertEquals("Test Meal", defaultMeal.description)
        assertEquals(1, defaultMeal.foods.size)
        assertEquals("Test Food", defaultMeal.foods[0].foodName) // From default createSizedFood
    }

    @Test
    fun `Meal can contain multiple foods with different amounts`() {
        val mixedFoods = listOf(
            SizedFood("Rice", 100.0f),
            SizedFood("Chicken", 150.0f),
            SizedFood("Vegetables", 80.0f),
            SizedFood("Olive Oil", 10.0f)
        )
        
        val meal = Meal("complex-meal", "Complex Meal", mixedFoods)
        
        assertEquals(4, meal.foods.size)
        
        // Verify all foods are present with correct amounts
        val riceFood = meal.foods.find { it.foodName == "Rice" }
        assertNotNull(riceFood)
        assertEquals(100.0f, riceFood.grams)
        
        val chickenFood = meal.foods.find { it.foodName == "Chicken" }
        assertNotNull(chickenFood)
        assertEquals(150.0f, chickenFood.grams)
        
        val oilFood = meal.foods.find { it.foodName == "Olive Oil" }
        assertNotNull(oilFood)
        assertEquals(10.0f, oilFood.grams)
    }

    @Test
    fun `Meal can be serialized`() {
        val meal = Meal(
            id = "serializable-meal",
            description = "Serializable Test",
            foods = listOf(SizedFood("Test Food", 100.0f))
        )
        
        // This test verifies that @Serializable annotation works
        // The actual serialization would require kotlinx.serialization.json
        // but we're just testing that the structure is properly annotated
        assertNotNull(meal)
        assertTrue(meal.javaClass.isAnnotationPresent(kotlinx.serialization.Serializable::class.java))
    }

    @Test
    fun `Meal handles duplicate food names`() {
        val duplicateFoods = listOf(
            SizedFood("Rice", 50.0f),
            SizedFood("Rice", 75.0f), // Same food, different amount
            SizedFood("Beans", 100.0f)
        )
        
        val meal = Meal("duplicate-meal", "Meal with Duplicates", duplicateFoods)
        
        assertEquals(3, meal.foods.size) // Should contain all entries
        
        val riceFoods = meal.foods.filter { it.foodName == "Rice" }
        assertEquals(2, riceFoods.size)
        assertEquals(50.0f, riceFoods[0].grams)
        assertEquals(75.0f, riceFoods[1].grams)
    }

    @Test
    fun `Meal can represent common meal types`() {
        // Breakfast
        val breakfast = Meal(
            "breakfast-1",
            "Morning Breakfast",
            listOf(
                SizedFood("Oats", 50.0f),
                SizedFood("Milk", 200.0f),
                SizedFood("Banana", 120.0f),
                SizedFood("Honey", 15.0f)
            )
        )
        
        // Lunch
        val lunch = Meal(
            "lunch-1", 
            "Healthy Lunch",
            listOf(
                SizedFood("Grilled Chicken", 150.0f),
                SizedFood("Quinoa", 80.0f),
                SizedFood("Mixed Vegetables", 120.0f),
                SizedFood("Olive Oil", 10.0f)
            )
        )
        
        // Snack
        val snack = Meal(
            "snack-1",
            "Afternoon Snack", 
            listOf(SizedFood("Apple", 150.0f), SizedFood("Almonds", 30.0f))
        )
        
        assertTrue(breakfast.foods.size == 4)
        assertTrue(lunch.foods.size == 4)
        assertTrue(snack.foods.size == 2)
        
        assertEquals("Morning Breakfast", breakfast.description)
        assertEquals("Healthy Lunch", lunch.description)
        assertEquals("Afternoon Snack", snack.description)
    }
}