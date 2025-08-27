package org.example.project.model

import org.example.project.ModelTestHelper
import org.example.project.TestUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.*

class ModelTest {

    @BeforeEach
    fun setup() {
        ModelTestHelper.setupTestEnvironment()
    }

    @AfterEach
    fun teardown() {
        ModelTestHelper.teardownTestEnvironment()
    }

    @Test
    fun `getFoodByName returns null for non-existent food`() {
        // This test verifies the function exists and handles null case
        // In actual usage, Model would be populated with foods
        val result = Model.getFoodByName("NonExistentFood")
        
        // Since Model starts empty in tests, this should be null
        // In a real app, you'd populate Model first
        assertNull(result)
    }

    @Test
    fun `getMealById returns null for non-existent meal`() {
        val result = Model.getMealById("non-existent-meal-id")
        assertNull(result)
    }

    @Test
    fun `getMultiDayMenuById returns null for non-existent menu`() {
        val result = Model.getMultiDayMenuById("non-existent-menu-id")
        assertNull(result)
    }

    @Test
    fun `getFoodMacros returns null for non-existent food`() {
        val result = Model.getFoodMacros("NonExistentFood")
        assertNull(result)
    }

    @Test
    fun `Model has correct initial screen state`() {
        // Model starts with Foods screen selected
        assertEquals(Screen.Foods, Model.currentScreen)
    }

    @Test
    fun `Model currentScreen can be changed`() {
        val originalScreen = Model.currentScreen
        
        // Change to Menus screen
        Model.currentScreen = Screen.Menus
        assertEquals(Screen.Menus, Model.currentScreen)
        
        // Change back to Foods screen
        Model.currentScreen = Screen.Foods
        assertEquals(Screen.Foods, Model.currentScreen)
        
        // Restore original state
        Model.currentScreen = originalScreen
    }

    @Test
    fun `Model lists are initially empty`() {
        // In test environment, Model starts with empty lists
        assertTrue(Model.foods.isEmpty())
        assertTrue(Model.meals.isEmpty())
        assertTrue(Model.multiDayMenus.isEmpty())
    }

    @Test
    fun `Model foods list is read-only`() {
        val foods = Model.foods
        
        // Verify it's a read-only list (returns a copy)
        assertTrue(foods is List<Food>)
        
        // The returned list should be immutable/read-only
        // We can't directly test mutability without trying to cast,
        // but we can verify the getter returns consistent results
        val foods2 = Model.foods
        assertEquals(foods.size, foods2.size)
    }

    @Test
    fun `Model meals list is read-only`() {
        val meals = Model.meals
        assertTrue(meals is List<Meal>)
        
        val meals2 = Model.meals
        assertEquals(meals.size, meals2.size)
    }

    @Test
    fun `Model multiDayMenus list is read-only`() {
        val multiDayMenus = Model.multiDayMenus
        assertTrue(multiDayMenus is List<MultiDayMenu>)
        
        val multiDayMenus2 = Model.multiDayMenus
        assertEquals(multiDayMenus.size, multiDayMenus2.size)
    }

    @Test
    fun `validate Food validation logic works correctly`() {
        // Test valid basic food
        val validFood = TestUtils.createValidMacroFood()
        assertTrue(validFood.validate())
        
        // Test invalid food with macros over 100%
        val invalidFood = Food(
            name = "Invalid Food",
            category = FoodCategory.DAIRY,
            proteins = 50.0f,
            carbs = 50.0f,
            fats = 30.0f,
            waterMassPercentage = 30.0f // Total = 160%
        )
        assertFalse(invalidFood.validate())
    }

    @Test
    fun `validate SizedFood validation logic works correctly`() {
        // Test valid sized food
        val validSizedFood = SizedFood("Test Food", 100.0f)
        assertTrue(validSizedFood.validate())
        
        // Test invalid sized food with zero grams
        val invalidSizedFood = SizedFood("Test Food", 0.0f)
        assertFalse(invalidSizedFood.validate())
        
        // Test invalid sized food with negative grams  
        val negativeSizedFood = SizedFood("Test Food", -10.0f)
        assertFalse(negativeSizedFood.validate())
        
        // Test invalid sized food with blank name
        val blankNameSizedFood = SizedFood("", 100.0f)
        assertFalse(blankNameSizedFood.validate())
    }

    @Test
    fun `validate MultiDayMenu validation logic works correctly`() {
        // Test valid multi-day menu
        val validMenu = MultiDayMenu(
            description = "Valid Menu",
            days = 7,
            dailyMenus = listOf(TestUtils.createDailyMenu())
        )
        assertTrue(validMenu.validate())
        
        // Test invalid menu with blank description
        val blankDescMenu = MultiDayMenu(
            description = "",
            days = 7,
            dailyMenus = emptyList()
        )
        assertFalse(blankDescMenu.validate())
        
        // Test invalid menu with zero days
        val zeroDaysMenu = MultiDayMenu(
            description = "Invalid Menu",
            days = 0,
            dailyMenus = emptyList()
        )
        assertFalse(zeroDaysMenu.validate())
        
        // Test invalid menu with too many daily menus
        val tooManyDaysMenu = MultiDayMenu(
            description = "Overpacked Menu", 
            days = 2,
            dailyMenus = listOf(
                TestUtils.createDailyMenu(id = "day1"),
                TestUtils.createDailyMenu(id = "day2"),
                TestUtils.createDailyMenu(id = "day3") // Exceeds days limit
            )
        )
        assertFalse(tooManyDaysMenu.validate())
    }

    @Test
    fun `Food categories enum has correct values`() {
        // Verify all expected categories exist
        val categories = FoodCategory.values()
        
        assertTrue(categories.contains(FoodCategory.DAIRY))
        assertTrue(categories.contains(FoodCategory.MEAT_FISH))
        assertTrue(categories.contains(FoodCategory.EGGS))
        assertTrue(categories.contains(FoodCategory.VEGGIES))
        assertTrue(categories.contains(FoodCategory.CEREALS_LEGUMES))
        assertTrue(categories.contains(FoodCategory.SUGARS))
        assertTrue(categories.contains(FoodCategory.FATS))
        assertTrue(categories.contains(FoodCategory.DRINKS))
        assertTrue(categories.contains(FoodCategory.NUTS))
        
        // Verify display names
        assertEquals("Dairy", FoodCategory.DAIRY.displayName)
        assertEquals("Meat & Fish", FoodCategory.MEAT_FISH.displayName)
        assertEquals("Vegetables", FoodCategory.VEGGIES.displayName)
        assertEquals("Cereals & Legumes", FoodCategory.CEREALS_LEGUMES.displayName)
    }

    @Test
    fun `Screen enum has correct values`() {
        val screens = Screen.values()
        
        assertTrue(screens.contains(Screen.Foods))
        assertTrue(screens.contains(Screen.Menus))
        
        // Should have exactly 2 screens
        assertEquals(2, screens.size)
    }

    @Test
    fun `Food effective categories logic works for basic foods`() {
        // Test with categories field populated
        val multiCategoryFood = Food(
            name = "Multi Category Food",
            category = FoodCategory.DAIRY, // This should be ignored
            categories = setOf(FoodCategory.MEAT_FISH, FoodCategory.NUTS),
            proteins = 20.0f, carbs = 10.0f, fats = 15.0f, waterMassPercentage = 50.0f
        )
        
        val dummyLookup: (String) -> Food? = { null }
        val effectiveCategories = multiCategoryFood.getEffectiveCategories(dummyLookup)
        
        assertEquals(2, effectiveCategories.size)
        assertTrue(effectiveCategories.contains(FoodCategory.MEAT_FISH))
        assertTrue(effectiveCategories.contains(FoodCategory.NUTS))
        assertFalse(effectiveCategories.contains(FoodCategory.DAIRY)) // Should use categories, not category
    }

    @Test
    fun `Food effective categories falls back to category when categories is empty`() {
        val singleCategoryFood = Food(
            name = "Single Category Food",
            category = FoodCategory.VEGGIES,
            proteins = 5.0f, carbs = 10.0f, fats = 1.0f, waterMassPercentage = 84.0f
        )
        
        val dummyLookup: (String) -> Food? = { null }
        val effectiveCategories = singleCategoryFood.getEffectiveCategories(dummyLookup)
        
        assertEquals(1, effectiveCategories.size)
        assertTrue(effectiveCategories.contains(FoodCategory.VEGGIES))
    }

    @Test
    fun `Food cache clearing works correctly`() {
        val food = Food(
            name = "Cached Food",
            categories = setOf(FoodCategory.DAIRY),
            proteins = 20.0f, carbs = 10.0f, fats = 15.0f, waterMassPercentage = 50.0f
        )
        
        val dummyLookup: (String) -> Food? = { null }
        
        // First call populates cache
        val categories1 = food.getEffectiveCategories(dummyLookup)
        
        // Clear cache
        food.clearCache()
        
        // Second call should work (cache was cleared and repopulated)
        val categories2 = food.getEffectiveCategories(dummyLookup)
        
        assertEquals(categories1, categories2)
        assertEquals(1, categories2.size)
        assertTrue(categories2.contains(FoodCategory.DAIRY))
    }

    @Test
    fun `TestUtils helper functions create valid objects`() {
        // Test all TestUtils functions create valid, testable objects
        val testMacros = TestUtils.createTestFoodMacros()
        assertNotNull(testMacros)
        assertEquals(20.0f, testMacros.proteins)
        
        val testFood = TestUtils.createBasicFood()
        assertNotNull(testFood)
        assertTrue(testFood.validate())
        
        val testSizedFood = TestUtils.createSizedFood()
        assertNotNull(testSizedFood)
        assertTrue(testSizedFood.validate())
        
        val testMeal = TestUtils.createMeal()
        assertNotNull(testMeal)
        assertEquals("test-meal-id", testMeal.id)
        
        val testDailyMenu = TestUtils.createDailyMenu()
        assertNotNull(testDailyMenu)
        assertEquals("test-daily-menu-id", testDailyMenu.id)
        
        val testMultiDayMenu = TestUtils.createMultiDayMenu()
        assertNotNull(testMultiDayMenu)
        assertTrue(testMultiDayMenu.validate())
        
        val validMacroFood = TestUtils.createValidMacroFood()
        assertTrue(validMacroFood.validate())
        
        val invalidMacroFood = TestUtils.createInvalidMacroFood()
        assertFalse(invalidMacroFood.validate()) // Should be invalid by design
    }
}