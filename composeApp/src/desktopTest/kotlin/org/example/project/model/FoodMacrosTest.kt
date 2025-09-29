package org.example.project.model

import org.example.project.TestUtils
import kotlin.test.*

class FoodMacrosTest {

    @Test
    fun `create FoodMacros with valid values`() {
        val macros = FoodMacros(20.0f, 30.0f, 10.0f, 40.0f)
        
        assertEquals(20.0f, macros.proteins)
        assertEquals(30.0f, macros.carbs)
        assertEquals(10.0f, macros.fats)
        assertEquals(40.0f, macros.waterMassPercentage)
    }

    @Test
    fun `create FoodMacros with zero values`() {
        val macros = FoodMacros(0.0f, 0.0f, 0.0f, 0.0f)
        
        assertEquals(0.0f, macros.proteins)
        assertEquals(0.0f, macros.carbs)
        assertEquals(0.0f, macros.fats)
        assertEquals(0.0f, macros.waterMassPercentage)
    }

    @Test
    fun `create FoodMacros with maximum valid values`() {
        val macros = FoodMacros(100.0f, 0.0f, 0.0f, 0.0f)
        assertEquals(100.0f, macros.proteins)
        
        val macros2 = FoodMacros(0.0f, 100.0f, 0.0f, 0.0f)
        assertEquals(100.0f, macros2.carbs)
        
        val macros3 = FoodMacros(0.0f, 0.0f, 100.0f, 0.0f)
        assertEquals(100.0f, macros3.fats)
        
        val macros4 = FoodMacros(0.0f, 0.0f, 0.0f, 100.0f)
        assertEquals(100.0f, macros4.waterMassPercentage)
    }

    @Test
    fun `FoodMacros equality works correctly`() {
        val macros1 = FoodMacros(20.0f, 30.0f, 10.0f, 40.0f)
        val macros2 = FoodMacros(20.0f, 30.0f, 10.0f, 40.0f)
        val macros3 = FoodMacros(20.0f, 30.0f, 10.0f, 39.0f)
        
        assertEquals(macros1, macros2)
        assertNotEquals(macros1, macros3)
    }

    @Test
    fun `FoodMacros toString works correctly`() {
        val macros = FoodMacros(20.5f, 30.2f, 10.8f, 38.5f)
        val toString = macros.toString()
        
        assertTrue(toString.contains("20.5"))
        assertTrue(toString.contains("30.2"))
        assertTrue(toString.contains("10.8"))
        assertTrue(toString.contains("38.5"))
    }

    @Test
    fun `FoodMacros copy functionality works`() {
        val original = FoodMacros(20.0f, 30.0f, 10.0f, 40.0f)
        val copied = original.copy()
        val modifiedCopy = original.copy(proteins = 25.0f)
        
        assertEquals(original, copied)
        assertEquals(25.0f, modifiedCopy.proteins)
        assertEquals(30.0f, modifiedCopy.carbs) // other values unchanged
        assertNotEquals(original, modifiedCopy)
    }

    @Test
    fun `FoodMacros handles decimal precision correctly`() {
        val macros = FoodMacros(20.123f, 30.456f, 10.789f, 38.632f)
        
        assertEquals(20.123f, macros.proteins, 0.001f)
        assertEquals(30.456f, macros.carbs, 0.001f)
        assertEquals(10.789f, macros.fats, 0.001f)
        assertEquals(38.632f, macros.waterMassPercentage, 0.001f)
    }

    @Test
    fun `FoodMacros can represent high water content foods`() {
        // Like lettuce with very high water content
        val lettuce = FoodMacros(1.4f, 2.9f, 0.2f, 95.6f)
        
        assertEquals(1.4f, lettuce.proteins)
        assertEquals(95.6f, lettuce.waterMassPercentage)
    }

    @Test
    fun `FoodMacros can represent high fat content foods`() {
        // Like oils or nuts with high fat content
        val oil = FoodMacros(0.0f, 0.0f, 100.0f, 0.0f)
        val nuts = FoodMacros(20.0f, 20.0f, 50.0f, 5.0f)
        
        assertEquals(100.0f, oil.fats)
        assertEquals(50.0f, nuts.fats)
        assertEquals(95.0f, nuts.proteins + nuts.carbs + nuts.fats + nuts.waterMassPercentage)
    }

    @Test
    fun `FoodMacros can represent high protein foods`() {
        // Like protein powder or lean meats
        val proteinPowder = FoodMacros(90.0f, 5.0f, 2.0f, 3.0f)
        val leanMeat = FoodMacros(30.0f, 0.0f, 3.0f, 65.0f)
        
        assertEquals(90.0f, proteinPowder.proteins)
        assertEquals(30.0f, leanMeat.proteins)
        assertEquals(0.0f, leanMeat.carbs)
    }
}