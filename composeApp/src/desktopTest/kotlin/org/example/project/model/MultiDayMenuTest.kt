package org.example.project.model

import org.example.project.TestUtils
import kotlin.test.*

class MultiDayMenuTest {

    @Test
    fun `create MultiDayMenu with valid data`() {
        val dailyMenus = listOf(
            TestUtils.createDailyMenu(id = "day1", name = "Day 1 Menu"),
            TestUtils.createDailyMenu(id = "day2", name = "Day 2 Menu")
        )
        
        val multiDayMenu = MultiDayMenu(
            id = "multi-menu-123",
            description = "Weekly Menu Plan",
            days = 7,
            dailyMenus = dailyMenus
        )
        
        assertEquals("multi-menu-123", multiDayMenu.id)
        assertEquals("Weekly Menu Plan", multiDayMenu.description)
        assertEquals(7, multiDayMenu.days)
        assertEquals(2, multiDayMenu.dailyMenus.size)
        assertEquals("Day 1 Menu", multiDayMenu.dailyMenus[0].name)
        assertEquals("Day 2 Menu", multiDayMenu.dailyMenus[1].name)
    }

    @Test
    fun `create empty MultiDayMenu`() {
        val emptyMenu = MultiDayMenu(
            id = "empty-multi-menu",
            description = "Empty Menu Plan",
            days = 5,
            dailyMenus = emptyList()
        )
        
        assertEquals("empty-multi-menu", emptyMenu.id)
        assertEquals("Empty Menu Plan", emptyMenu.description)
        assertEquals(5, emptyMenu.days)
        assertTrue(emptyMenu.dailyMenus.isEmpty())
    }

    @Test
    fun `MultiDayMenu validation passes with valid data`() {
        val validMenu = MultiDayMenu(
            description = "Valid Menu",
            days = 7,
            dailyMenus = listOf(TestUtils.createDailyMenu())
        )
        
        assertTrue(validMenu.validate())
    }

    @Test
    fun `MultiDayMenu validation passes with empty daily menus`() {
        val emptyDaysMenu = MultiDayMenu(
            description = "Planning Menu",
            days = 7,
            dailyMenus = emptyList()
        )
        
        assertTrue(emptyDaysMenu.validate())
    }

    @Test
    fun `MultiDayMenu validation passes when dailyMenus size equals days`() {
        val fullMenu = MultiDayMenu(
            description = "Complete 3-Day Menu",
            days = 3,
            dailyMenus = listOf(
                TestUtils.createDailyMenu(id = "day1"),
                TestUtils.createDailyMenu(id = "day2"),
                TestUtils.createDailyMenu(id = "day3")
            )
        )
        
        assertTrue(fullMenu.validate())
    }

    @Test
    fun `MultiDayMenu validation fails with blank description`() {
        val blankDescriptionMenu = MultiDayMenu(
            description = "",
            days = 7,
            dailyMenus = listOf(TestUtils.createDailyMenu())
        )
        
        assertFalse(blankDescriptionMenu.validate())
    }

    @Test
    fun `MultiDayMenu validation fails with whitespace-only description`() {
        val whitespaceDescriptionMenu = MultiDayMenu(
            description = "   ",
            days = 7,
            dailyMenus = listOf(TestUtils.createDailyMenu())
        )
        
        assertFalse(whitespaceDescriptionMenu.validate())
    }

    @Test
    fun `MultiDayMenu validation fails with zero days`() {
        val zeroDaysMenu = MultiDayMenu(
            description = "Invalid Menu",
            days = 0,
            dailyMenus = emptyList()
        )
        
        assertFalse(zeroDaysMenu.validate())
    }

    @Test
    fun `MultiDayMenu validation fails with negative days`() {
        val negativeDaysMenu = MultiDayMenu(
            description = "Invalid Menu",
            days = -5,
            dailyMenus = emptyList()
        )
        
        assertFalse(negativeDaysMenu.validate())
    }

    @Test
    fun `MultiDayMenu validation fails when dailyMenus size exceeds days`() {
        val tooManyDaysMenu = MultiDayMenu(
            description = "Overpacked Menu",
            days = 2,
            dailyMenus = listOf(
                TestUtils.createDailyMenu(id = "day1"),
                TestUtils.createDailyMenu(id = "day2"),
                TestUtils.createDailyMenu(id = "day3") // Too many!
            )
        )
        
        assertFalse(tooManyDaysMenu.validate())
    }

    @Test
    fun `MultiDayMenu equality works correctly`() {
        val dailyMenus = listOf(TestUtils.createDailyMenu())
        
        val menu1 = MultiDayMenu("id1", "Menu 1", 7, dailyMenus)
        val menu2 = MultiDayMenu("id1", "Menu 1", 7, dailyMenus)
        val menu3 = MultiDayMenu("id2", "Menu 1", 7, dailyMenus)
        val menu4 = MultiDayMenu("id1", "Menu 2", 7, dailyMenus)
        val menu5 = MultiDayMenu("id1", "Menu 1", 5, dailyMenus)
        
        assertEquals(menu1, menu2)
        assertNotEquals(menu1, menu3) // Different ID
        assertNotEquals(menu1, menu4) // Different description
        assertNotEquals(menu1, menu5) // Different days
    }

    @Test
    fun `MultiDayMenu copy functionality works`() {
        val originalDailyMenus = listOf(TestUtils.createDailyMenu())
        val original = MultiDayMenu("original-id", "Original Menu", 7, originalDailyMenus)
        
        val copied = original.copy()
        val descriptionChanged = original.copy(description = "Modified Menu")
        val daysChanged = original.copy(days = 14)
        val newDailyMenus = listOf(TestUtils.createDailyMenu(id = "new-day"))
        val dailyMenusChanged = original.copy(dailyMenus = newDailyMenus)
        
        assertEquals(original, copied)
        assertEquals("Modified Menu", descriptionChanged.description)
        assertEquals(14, daysChanged.days)
        assertEquals("new-day", dailyMenusChanged.dailyMenus[0].id)
    }

    @Test
    fun `MultiDayMenu toString contains relevant information`() {
        val menu = MultiDayMenu(
            "test-id", "Test Menu", 7, listOf(TestUtils.createDailyMenu())
        )
        val toString = menu.toString()
        
        assertTrue(toString.contains("test-id"))
        assertTrue(toString.contains("Test Menu"))
        assertTrue(toString.contains("7"))
    }

    @Test
    fun `TestUtils createMultiDayMenu works correctly`() {
        val customDailyMenus = listOf(
            TestUtils.createDailyMenu(id = "custom1", name = "Custom Day 1")
        )
        
        val testMenu = TestUtils.createMultiDayMenu(
            id = "custom-id",
            description = "Custom Multi-Day Menu",
            days = 14,
            dailyMenus = customDailyMenus
        )
        
        assertEquals("custom-id", testMenu.id)
        assertEquals("Custom Multi-Day Menu", testMenu.description)
        assertEquals(14, testMenu.days)
        assertEquals(1, testMenu.dailyMenus.size)
        assertEquals("custom1", testMenu.dailyMenus[0].id)
        assertEquals("Custom Day 1", testMenu.dailyMenus[0].name)
    }

    @Test
    fun `TestUtils createMultiDayMenu with defaults works`() {
        val defaultMenu = TestUtils.createMultiDayMenu()
        
        assertEquals("test-multi-day-menu-id", defaultMenu.id)
        assertEquals("Test Multi-Day Menu", defaultMenu.description)
        assertEquals(7, defaultMenu.days)
        assertEquals(1, defaultMenu.dailyMenus.size)
        assertEquals("test-daily-menu-id", defaultMenu.dailyMenus[0].id) // From createDailyMenu default
    }

    @Test
    fun `MultiDayMenu can be serialized`() {
        val menu = MultiDayMenu(
            "serializable-id", "Serializable Menu", 7, listOf(TestUtils.createDailyMenu())
        )
        
        assertTrue(menu.javaClass.isAnnotationPresent(kotlinx.serialization.Serializable::class.java))
    }

    @Test
    fun `MultiDayMenu can represent different time periods`() {
        // Weekend menu (2 days)
        val weekendMenu = MultiDayMenu(
            description = "Weekend Special",
            days = 2,
            dailyMenus = listOf(
                TestUtils.createDailyMenu(name = "Saturday"),
                TestUtils.createDailyMenu(name = "Sunday")
            )
        )
        
        // Weekly menu (7 days)
        val weeklyMenu = MultiDayMenu(
            description = "Weekly Plan",
            days = 7,
            dailyMenus = listOf(TestUtils.createDailyMenu(name = "Monday"))
        )
        
        // Monthly template (30 days)
        val monthlyMenu = MultiDayMenu(
            description = "Monthly Template",
            days = 30,
            dailyMenus = emptyList() // Template to be filled
        )
        
        assertEquals(2, weekendMenu.days)
        assertEquals(7, weeklyMenu.days)
        assertEquals(30, monthlyMenu.days)
        
        assertTrue(weekendMenu.validate())
        assertTrue(weeklyMenu.validate())
        assertTrue(monthlyMenu.validate())
    }

    @Test
    fun `MultiDayMenu handles incomplete week plans`() {
        // Common scenario: plan first 3 days of the week
        val partialWeekMenu = MultiDayMenu(
            description = "Partial Week Plan",
            days = 7,
            dailyMenus = listOf(
                TestUtils.createDailyMenu(name = "Monday"),
                TestUtils.createDailyMenu(name = "Tuesday"),
                TestUtils.createDailyMenu(name = "Wednesday")
            )
        )
        
        assertEquals(7, partialWeekMenu.days)
        assertEquals(3, partialWeekMenu.dailyMenus.size)
        assertTrue(partialWeekMenu.validate()) // Valid: dailyMenus.size <= days
    }

    @Test
    fun `MultiDayMenu average calculations handle empty dailyMenus`() {
        val emptyMenu = MultiDayMenu(
            description = "Empty Menu",
            days = 7,
            dailyMenus = emptyList()
        )
        
        assertEquals(0f, emptyMenu.averageProteins)
        assertEquals(0f, emptyMenu.averageFats)
        assertEquals(0f, emptyMenu.averageCarbs)
        assertEquals(0f, emptyMenu.averageCalories)
    }

    @Test
    fun `MultiDayMenu can have long descriptions`() {
        val longDescription = "Very detailed multi-day menu plan designed for specific nutritional goals, dietary restrictions, and lifestyle requirements with careful consideration of macro and micronutrient balance"
        
        val detailedMenu = MultiDayMenu(
            description = longDescription,
            days = 14,
            dailyMenus = listOf(TestUtils.createDailyMenu())
        )
        
        assertEquals(longDescription, detailedMenu.description)
        assertTrue(longDescription.length > 100)
        assertTrue(detailedMenu.validate())
    }

    @Test
    fun `MultiDayMenu supports various planning scenarios`() {
        // Prep day (1 day meal prep)
        val prepDay = MultiDayMenu("prep", "Meal Prep Day", 1, listOf(TestUtils.createDailyMenu()))
        
        // Work week (5 days)
        val workWeek = MultiDayMenu("work", "Work Week Plan", 5, emptyList())
        
        // Vacation (10 days)
        val vacation = MultiDayMenu("vacation", "Vacation Eating Plan", 10, emptyList())
        
        // Competition prep (12 weeks = 84 days)
        val competitionPrep = MultiDayMenu("comp", "12-Week Competition Prep", 84, emptyList())
        
        listOf(prepDay, workWeek, vacation, competitionPrep).forEach { menu ->
            assertTrue(menu.validate(), "Menu '${menu.description}' should be valid")
            assertTrue(menu.days > 0, "Menu should have positive days")
            assertTrue(menu.description.isNotBlank(), "Menu should have non-blank description")
        }
    }

    @Test
    fun `MultiDayMenu ID generation works when not specified`() {
        val menu1 = MultiDayMenu(description = "Auto ID Menu 1", days = 7, dailyMenus = emptyList())
        val menu2 = MultiDayMenu(description = "Auto ID Menu 2", days = 7, dailyMenus = emptyList())
        
        assertNotNull(menu1.id)
        assertNotNull(menu2.id)
        assertNotEquals(menu1.id, menu2.id) // Should generate different IDs
        assertTrue(menu1.id.isNotBlank())
        assertTrue(menu2.id.isNotBlank())
    }
}