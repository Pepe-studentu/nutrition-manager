package org.example.project.model

import org.example.project.TestUtils
import kotlin.test.*

class DailyMenuTest {

    @Test
    fun `create DailyMenu with valid data`() {
        val dailyMenu = DailyMenu(
            id = "daily-menu-123",
            name = "Monday Menu",
            breakfastId = "breakfast-1",
            snack1Id = "snack1-1",
            lunchId = "lunch-1",
            snack2Id = "snack2-1",
            dinnerId = "dinner-1"
        )
        
        assertEquals("daily-menu-123", dailyMenu.id)
        assertEquals("Monday Menu", dailyMenu.name)
        assertEquals("breakfast-1", dailyMenu.breakfastId)
        assertEquals("snack1-1", dailyMenu.snack1Id)
        assertEquals("lunch-1", dailyMenu.lunchId)
        assertEquals("snack2-1", dailyMenu.snack2Id)
        assertEquals("dinner-1", dailyMenu.dinnerId)
    }

    @Test
    fun `create DailyMenu with null name`() {
        val dailyMenu = DailyMenu(
            id = "unnamed-menu",
            name = null,
            breakfastId = "breakfast-1",
            snack1Id = "snack1-1",
            lunchId = "lunch-1",
            snack2Id = "snack2-1",
            dinnerId = "dinner-1"
        )
        
        assertEquals("unnamed-menu", dailyMenu.id)
        assertNull(dailyMenu.name)
    }

    @Test
    fun `DailyMenu equality works correctly`() {
        val dailyMenu1 = DailyMenu(
            "menu-1", "Test Menu", "b1", "s1", "l1", "s2", "d1"
        )
        val dailyMenu2 = DailyMenu(
            "menu-1", "Test Menu", "b1", "s1", "l1", "s2", "d1"
        )
        val dailyMenu3 = DailyMenu(
            "menu-2", "Test Menu", "b1", "s1", "l1", "s2", "d1"
        )
        val dailyMenu4 = DailyMenu(
            "menu-1", "Different Name", "b1", "s1", "l1", "s2", "d1"
        )
        val dailyMenu5 = DailyMenu(
            "menu-1", "Test Menu", "different-breakfast", "s1", "l1", "s2", "d1"
        )
        
        assertEquals(dailyMenu1, dailyMenu2)
        assertNotEquals(dailyMenu1, dailyMenu3) // Different ID
        assertNotEquals(dailyMenu1, dailyMenu4) // Different name
        assertNotEquals(dailyMenu1, dailyMenu5) // Different meal ID
    }

    @Test
    fun `DailyMenu copy functionality works`() {
        val original = DailyMenu(
            "original-id", "Original Menu", "b1", "s1", "l1", "s2", "d1"
        )
        
        val copied = original.copy()
        val nameChanged = original.copy(name = "Modified Menu")
        val breakfastChanged = original.copy(breakfastId = "new-breakfast")
        
        assertEquals(original, copied)
        assertEquals("Modified Menu", nameChanged.name)
        assertEquals("original-id", nameChanged.id) // Other fields unchanged
        assertEquals("new-breakfast", breakfastChanged.breakfastId)
        assertEquals("Original Menu", breakfastChanged.name) // Other fields unchanged
    }

    @Test
    fun `DailyMenu toString contains relevant information`() {
        val dailyMenu = DailyMenu(
            "test-id", "Test Menu", "b1", "s1", "l1", "s2", "d1"
        )
        val toString = dailyMenu.toString()
        
        assertTrue(toString.contains("test-id"))
        assertTrue(toString.contains("Test Menu"))
    }

    @Test
    fun `TestUtils createDailyMenu works correctly`() {
        val testMenu = TestUtils.createDailyMenu(
            id = "custom-id",
            name = "Custom Daily Menu",
            breakfastId = "custom-breakfast"
        )
        
        assertEquals("custom-id", testMenu.id)
        assertEquals("Custom Daily Menu", testMenu.name)
        assertEquals("custom-breakfast", testMenu.breakfastId)
        assertEquals("snack1-meal-id", testMenu.snack1Id) // Default value
        assertEquals("lunch-meal-id", testMenu.lunchId) // Default value
        assertEquals("snack2-meal-id", testMenu.snack2Id) // Default value
        assertEquals("dinner-meal-id", testMenu.dinnerId) // Default value
    }

    @Test
    fun `TestUtils createDailyMenu with defaults works`() {
        val defaultMenu = TestUtils.createDailyMenu()
        
        assertEquals("test-daily-menu-id", defaultMenu.id)
        assertEquals("Test Daily Menu", defaultMenu.name)
        assertEquals("breakfast-meal-id", defaultMenu.breakfastId)
        assertEquals("snack1-meal-id", defaultMenu.snack1Id)
        assertEquals("lunch-meal-id", defaultMenu.lunchId)
        assertEquals("snack2-meal-id", defaultMenu.snack2Id)
        assertEquals("dinner-meal-id", defaultMenu.dinnerId)
    }

    @Test
    fun `DailyMenu can be serialized`() {
        val dailyMenu = DailyMenu(
            "serializable-menu", "Serializable Test", "b1", "s1", "l1", "s2", "d1"
        )
        
        // Verify that @Serializable annotation is present
        assertTrue(dailyMenu.javaClass.isAnnotationPresent(kotlinx.serialization.Serializable::class.java))
    }

    @Test
    fun `DailyMenu can represent different types of daily menus`() {
        // Weekday menu
        val weekdayMenu = DailyMenu(
            "weekday-1",
            "Monday Workday",
            "quick-breakfast",
            "morning-snack",
            "office-lunch",
            "afternoon-snack", 
            "home-dinner"
        )
        
        // Weekend menu
        val weekendMenu = DailyMenu(
            "weekend-1",
            "Saturday Relaxed",
            "big-breakfast",
            "light-snack",
            "restaurant-lunch",
            "fruit-snack",
            "family-dinner"
        )
        
        // Diet menu
        val dietMenu = DailyMenu(
            "diet-1",
            "Low Carb Day",
            "protein-breakfast",
            "nuts-snack",
            "salad-lunch",
            "veggie-snack",
            "lean-dinner"
        )
        
        assertEquals("Monday Workday", weekdayMenu.name)
        assertEquals("Saturday Relaxed", weekendMenu.name)
        assertEquals("Low Carb Day", dietMenu.name)
        
        // All should have all 5 meal slots
        assertEquals("quick-breakfast", weekdayMenu.breakfastId)
        assertEquals("morning-snack", weekdayMenu.snack1Id)
        assertEquals("office-lunch", weekdayMenu.lunchId)
        assertEquals("afternoon-snack", weekdayMenu.snack2Id)
        assertEquals("home-dinner", weekdayMenu.dinnerId)
    }

    @Test
    fun `DailyMenu handles empty or placeholder meal IDs`() {
        val menuWithEmptyMeals = DailyMenu(
            "sparse-menu",
            "Sparse Menu",
            "", // Empty breakfast
            "skip-snack", // Could be a "skip meal" placeholder
            "light-lunch",
            "", // Empty snack
            "big-dinner"
        )
        
        assertEquals("", menuWithEmptyMeals.breakfastId)
        assertEquals("skip-snack", menuWithEmptyMeals.snack1Id)
        assertEquals("", menuWithEmptyMeals.snack2Id)
        assertEquals("big-dinner", menuWithEmptyMeals.dinnerId)
    }

    @Test
    fun `DailyMenu meal IDs can reference the same meal`() {
        // Someone might want the same meal for multiple slots
        val repeatedMealMenu = DailyMenu(
            "repeated-meal",
            "Same Snack Menu",
            "varied-breakfast",
            "healthy-snack", // Same snack
            "main-lunch", 
            "healthy-snack", // Same snack repeated
            "varied-dinner"
        )
        
        assertEquals("healthy-snack", repeatedMealMenu.snack1Id)
        assertEquals("healthy-snack", repeatedMealMenu.snack2Id)
        assertNotEquals(repeatedMealMenu.breakfastId, repeatedMealMenu.snack1Id)
    }

    @Test
    fun `DailyMenu can have very long names`() {
        val longName = "Very Long Menu Name for Special Dietary Requirements and Specific Nutritional Goals"
        val longNameMenu = DailyMenu(
            "long-name-menu",
            longName,
            "breakfast", "snack1", "lunch", "snack2", "dinner"
        )
        
        assertEquals(longName, longNameMenu.name)
        assertTrue(longName.length > 50) // Verify it's actually a long name
    }

    @Test
    fun `DailyMenu meal IDs can contain special characters`() {
        val specialCharsMenu = DailyMenu(
            "special-chars-menu",
            "Special Menu",
            "breakfast-2024-01-01",
            "snack_1_protein",
            "lunch-v2.1",
            "snack@afternoon",
            "dinner#family"
        )
        
        assertEquals("breakfast-2024-01-01", specialCharsMenu.breakfastId)
        assertEquals("snack_1_protein", specialCharsMenu.snack1Id)
        assertEquals("lunch-v2.1", specialCharsMenu.lunchId)
        assertEquals("snack@afternoon", specialCharsMenu.snack2Id)
        assertEquals("dinner#family", specialCharsMenu.dinnerId)
    }
}