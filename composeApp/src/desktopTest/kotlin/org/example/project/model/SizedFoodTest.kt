package org.example.project.model

import org.example.project.TestUtils
import kotlin.test.*

class SizedFoodTest {

    @Test
    fun `create SizedFood with valid data`() {
        val sizedFood = SizedFood(
            foodName = "Chicken Breast",
            grams = 150.0f
        )
        
        assertEquals("Chicken Breast", sizedFood.foodName)
        assertEquals(150.0f, sizedFood.grams)
    }

    @Test
    fun `SizedFood validation passes with valid data`() {
        val validSizedFood = SizedFood("Rice", 200.0f)
        assertTrue(validSizedFood.validate())
    }

    @Test
    fun `SizedFood validation passes with small positive amounts`() {
        val smallAmount = SizedFood("Salt", 0.1f)
        assertTrue(smallAmount.validate())
    }

    @Test
    fun `SizedFood validation fails with zero grams`() {
        val zeroGrams = SizedFood("Rice", 0.0f)
        assertFalse(zeroGrams.validate())
    }

    @Test
    fun `SizedFood validation fails with negative grams`() {
        val negativeGrams = SizedFood("Rice", -5.0f)
        assertFalse(negativeGrams.validate())
    }

    @Test
    fun `SizedFood validation fails with blank food name`() {
        val blankName = SizedFood("", 100.0f)
        assertFalse(blankName.validate())
    }

    @Test
    fun `SizedFood validation fails with empty food name`() {
        val emptyName = SizedFood("   ", 100.0f)
        assertFalse(emptyName.validate())
    }

    @Test
    fun `SizedFood validation passes with food name containing spaces`() {
        val nameWithSpaces = SizedFood("Brown Rice", 150.0f)
        assertTrue(nameWithSpaces.validate())
    }

    @Test
    fun `SizedFood equality works correctly`() {
        val sizedFood1 = SizedFood("Rice", 100.0f)
        val sizedFood2 = SizedFood("Rice", 100.0f)
        val sizedFood3 = SizedFood("Rice", 150.0f)
        val sizedFood4 = SizedFood("Beans", 100.0f)
        
        assertEquals(sizedFood1, sizedFood2)
        assertNotEquals(sizedFood1, sizedFood3) // Different grams
        assertNotEquals(sizedFood1, sizedFood4) // Different food name
    }

    @Test
    fun `SizedFood toString contains relevant information`() {
        val sizedFood = SizedFood("Oats", 50.5f)
        val toString = sizedFood.toString()
        
        assertTrue(toString.contains("Oats"))
        assertTrue(toString.contains("50.5"))
    }

    @Test
    fun `SizedFood copy functionality works`() {
        val original = SizedFood("Original Food", 100.0f)
        val copied = original.copy()
        val modifiedCopy = original.copy(grams = 200.0f)
        val renamedCopy = original.copy(foodName = "Modified Food")
        
        assertEquals(original, copied)
        assertEquals("Original Food", modifiedCopy.foodName)
        assertEquals(200.0f, modifiedCopy.grams)
        assertEquals("Modified Food", renamedCopy.foodName)
        assertEquals(100.0f, renamedCopy.grams)
    }

    @Test
    fun `SizedFood can handle very large amounts`() {
        val largePortion = SizedFood("Bulk Rice", 5000.0f) // 5kg
        assertTrue(largePortion.validate())
        assertEquals(5000.0f, largePortion.grams)
    }

    @Test
    fun `SizedFood can handle very small amounts`() {
        val spice = SizedFood("Black Pepper", 0.001f) // 1mg
        assertTrue(spice.validate())
        assertEquals(0.001f, spice.grams, 0.0001f)
    }

    @Test
    fun `SizedFood handles decimal precision correctly`() {
        val preciseAmount = SizedFood("Olive Oil", 15.25f)
        assertTrue(preciseAmount.validate())
        assertEquals(15.25f, preciseAmount.grams, 0.01f)
    }

    @Test
    fun `TestUtils createSizedFood works correctly`() {
        val testFood = TestUtils.createBasicFood("Test Food")
        val sizedFood = TestUtils.createSizedFood(testFood, 250.0f)
        
        assertEquals("Test Food", sizedFood.foodName)
        assertEquals(250.0f, sizedFood.grams)
        assertTrue(sizedFood.validate())
    }

    @Test
    fun `TestUtils createSizedFood with defaults works`() {
        val sizedFood = TestUtils.createSizedFood()
        
        assertEquals("Test Food", sizedFood.foodName) // Default from createBasicFood
        assertEquals(100.0f, sizedFood.grams) // Default grams
        assertTrue(sizedFood.validate())
    }

    @Test
    fun `SizedFood can represent typical food portions`() {
        val portions = listOf(
            SizedFood("Apple", 180.0f),       // Medium apple
            SizedFood("Banana", 120.0f),      // Medium banana
            SizedFood("Bread Slice", 25.0f),  // Single slice
            SizedFood("Milk", 250.0f),        // Glass of milk
            SizedFood("Pasta", 85.0f),        // Dry pasta serving
            SizedFood("Chicken Breast", 150.0f) // Typical serving
        )
        
        portions.forEach { portion ->
            assertTrue(portion.validate(), "Failed validation for ${portion.foodName}")
            assertTrue(portion.grams > 0, "${portion.foodName} should have positive grams")
        }
    }

    @Test
    fun `SizedFood foodName is case sensitive`() {
        val food1 = SizedFood("rice", 100.0f)
        val food2 = SizedFood("Rice", 100.0f)
        val food3 = SizedFood("RICE", 100.0f)
        
        assertNotEquals(food1, food2)
        assertNotEquals(food2, food3)
        assertNotEquals(food1, food3)
        
        // All should be valid though
        assertTrue(food1.validate())
        assertTrue(food2.validate())
        assertTrue(food3.validate())
    }
}