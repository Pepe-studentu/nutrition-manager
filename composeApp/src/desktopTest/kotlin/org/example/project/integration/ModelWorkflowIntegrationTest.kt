package org.example.project.integration

import org.example.project.ModelTestHelper
import org.example.project.TestUtils
import org.example.project.model.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.*

/**
 * Comprehensive integration tests for Model workflows and compound food scenarios.
 * These tests ensure safe refactoring by covering complex interactions and edge cases.
 * 
 * Focus areas:
 * 1. Complex food workflows with compound foods and nested dependencies
 * 2. End-to-end meal and menu operations with referential integrity
 * 3. Circular dependency detection and prevention
 */
class ModelWorkflowIntegrationTest {

    @BeforeEach
    fun setup() {
        ModelTestHelper.setupTestEnvironment()
    }

    @AfterEach
    fun teardown() {
        ModelTestHelper.teardownTestEnvironment()
    }

    // ============= CATEGORY 1: COMPLEX FOOD WORKFLOWS =============

    @Test
    fun `compound food with nested dependencies calculates macros correctly`() {
        // Given: Create a hierarchy - Basic → Compound L1 → Compound L2
        val basicFood = TestUtils.createBasicFood(
            name = "Chicken Breast", 
            proteins = 25.0f, carbs = 0.0f, fats = 5.0f, water = 70.0f
        )
        val compoundL1 = Food(
            name = "Seasoned Chicken",
            category = FoodCategory.MEAT_FISH,
            components = mapOf(
                "Chicken Breast" to 90.0f,
                // We'll add a spice later
            )
        )
        
        // When: Add basic food first
        assertTrue(Model.insertFood(basicFood))
        assertEquals(1, Model.foods.size)

        // Add a spice
        val spice = TestUtils.createBasicFood(
            name = "Salt", 
            proteins = 0.0f, carbs = 0.0f, fats = 0.0f, water = 0.0f
        )
        assertTrue(Model.insertFood(spice))
        
        // Update compound food to include spice
        val updatedCompoundL1 = compoundL1.copy(
            components = mapOf(
                "Chicken Breast" to 90.0f,
                "Salt" to 10.0f
            )
        )
        assertTrue(Model.insertFood(updatedCompoundL1))
        assertEquals(3, Model.foods.size)

        // Now create level 2 compound food
        val compoundL2 = Food(
            name = "Chicken Meal",
            category = FoodCategory.MEAT_FISH,
            components = mapOf("Seasoned Chicken" to 100.0f)
        )
        assertTrue(Model.insertFood(compoundL2))

        // Then: Verify macro calculations work through the hierarchy
        val basicMacros = Model.getFoodMacros("Chicken Breast")
        assertNotNull(basicMacros)
        assertEquals(25.0f, basicMacros.proteins)
        assertEquals(5.0f, basicMacros.fats)

        val compoundL1Macros = Model.getFoodMacros("Seasoned Chicken")
        assertNotNull(compoundL1Macros)
        // 90% of chicken breast macros + 10% salt (zero macros)
        assertEquals(22.5f, compoundL1Macros.proteins, 0.01f) // 25 * 0.9
        assertEquals(4.5f, compoundL1Macros.fats, 0.01f) // 5 * 0.9

        val compoundL2Macros = Model.getFoodMacros("Chicken Meal")
        assertNotNull(compoundL2Macros)
        // Should equal L1 macros since it's 100%
        assertEquals(compoundL1Macros.proteins, compoundL2Macros.proteins, 0.01f)
        assertEquals(compoundL1Macros.fats, compoundL2Macros.fats, 0.01f)
    }

    @Test
    fun `usage count tracking works correctly through complex operations`() {
        // Given: Basic food and compound food
        val basicFood = TestUtils.createBasicFood("Base Ingredient")
        val compoundFood = Food(
            name = "Mixed Food",
            category = FoodCategory.CEREALS_LEGUMES,
            components = mapOf("Base Ingredient" to 100.0f)
        )

        assertTrue(Model.insertFood(basicFood))
        assertTrue(Model.insertFood(compoundFood))

        // Verify initial usage counts
        assertEquals(0, Model.getFoodByName("Base Ingredient")!!.usageCount)
        assertEquals(0, Model.getFoodByName("Mixed Food")!!.usageCount)

        // When: Create a meal using the compound food
        val success = Model.insertMeal("Test Meal", listOf(
            SizedFood("Mixed Food", 150.0f)
        ))
        assertTrue(success)
        
        // Then: Usage counts should be incremented correctly
        // Base ingredient gets incremented because it's a component of Mixed Food
        assertEquals(1, Model.getFoodByName("Base Ingredient")!!.usageCount)
        assertEquals(1, Model.getFoodByName("Mixed Food")!!.usageCount)

        // When: Update the meal to change quantities
        val mealId = Model.meals.first().id
        val updateSuccess = Model.updateMeal(mealId, "Updated Meal", listOf(
            SizedFood("Mixed Food", 200.0f) // Different quantity
        ))
        assertTrue(updateSuccess)

        // Then: Usage counts should remain the same (still used once)
        assertEquals(1, Model.getFoodByName("Base Ingredient")!!.usageCount)
        assertEquals(1, Model.getFoodByName("Mixed Food")!!.usageCount)

        // When: Remove the meal
        assertTrue(Model.deleteMeal(mealId))

        // Then: Usage counts should be decremented
        assertEquals(0, Model.getFoodByName("Base Ingredient")!!.usageCount)
        assertEquals(0, Model.getFoodByName("Mixed Food")!!.usageCount)
    }

    @Test
    fun `macro cache consistency during food updates`() {
        // Given: Compound food with cached macros
        val baseFood = TestUtils.createBasicFood(
            name = "Cacheable Food", 
            proteins = 20.0f, 
            carbs = 30.0f, 
            fats = 10.0f, 
            water = 40.0f  // Total = 100%
        )
        val compoundFood = Food(
            name = "Cache Test Food",
            category = FoodCategory.DAIRY,
            components = mapOf("Cacheable Food" to 100.0f)
        )
        
        assertTrue(Model.insertFood(baseFood))
        assertTrue(Model.insertFood(compoundFood))

        // Get macros to populate cache
        val initialMacros = Model.getFoodMacros("Cache Test Food")
        assertNotNull(initialMacros)
        assertEquals(20.0f, initialMacros.proteins)

        // When: Update base food macros (keep total ≤ 100%)
        val updatedBaseFood = baseFood.copy(proteins = 25.0f, waterMassPercentage = 35.0f) // 25+30+10+35=100
        assertTrue(Model.updateFood("Cacheable Food", updatedBaseFood))

        // Then: Cached macros should be cleared and recalculated
        val updatedMacros = Model.getFoodMacros("Cache Test Food")
        assertNotNull(updatedMacros)
        assertEquals(25.0f, updatedMacros.proteins, "Cache should be cleared and macros recalculated")
    }

    // ============= CIRCULAR DEPENDENCY TESTS =============

    @Test
    fun `direct circular dependency is prevented in insertFood`() {
        // Given: Basic food to reference
        val baseFood = TestUtils.createBasicFood("Base Food")
        assertTrue(Model.insertFood(baseFood))

        // When: Try to create food that references itself
        val selfReferencingFood = Food(
            name = "Self Ref",
            category = FoodCategory.DAIRY,
            components = mapOf("Self Ref" to 100.0f)
        )

        // Then: Should be rejected
        assertFalse(Model.insertFood(selfReferencingFood), "Self-referencing food should be rejected")
        assertEquals(1, Model.foods.size, "Food should not be added")
    }

    @Test
    fun `indirect circular dependency is prevented`() {
        // Given: Create basic foods first, then build the circular chain
        val basicFood = TestUtils.createBasicFood("Basic")
        assertTrue(Model.insertFood(basicFood))

        // Create first compound food referencing basic food
        val compoundA = Food(
            name = "Compound A", 
            category = FoodCategory.DAIRY,
            components = mapOf("Basic" to 100.0f)
        )
        assertTrue(Model.insertFood(compoundA))

        // Create second compound food referencing first compound
        val compoundB = Food(
            name = "Compound B",
            category = FoodCategory.DAIRY, 
            components = mapOf("Compound A" to 100.0f) // B → A → Basic
        )
        assertTrue(Model.insertFood(compoundB))

        // When: Try to update A to reference B (creating cycle: A→B→A)
        val updatedA = compoundA.copy(components = mapOf("Compound B" to 100.0f))

        // Then: Should be rejected due to circular dependency A→B→A
        assertFalse(Model.updateFood("Compound A", updatedA), "Circular dependency should be prevented")
        
        // Verify A still has original components
        val retrievedA = Model.getFoodByName("Compound A")
        assertEquals("Basic", retrievedA!!.components.keys.first())
    }

    @Test
    fun `circular dependency detection works with updateFood`() {
        // Given: Three separate compound foods
        val foodA = Food("Food A", FoodCategory.DAIRY, components = mapOf("Basic" to 100.0f))
        val foodB = Food("Food B", FoodCategory.DAIRY, components = mapOf("Basic" to 100.0f))
        val basic = TestUtils.createBasicFood("Basic")

        assertTrue(Model.insertFood(basic))
        assertTrue(Model.insertFood(foodA))
        assertTrue(Model.insertFood(foodB))

        // When: Try to update Food A to reference Food B, and Food B to reference Food A
        val updatedA = foodA.copy(components = mapOf("Food B" to 100.0f))
        assertTrue(Model.updateFood("Food A", updatedA), "First update should succeed")

        val updatedB = foodB.copy(components = mapOf("Food A" to 100.0f))
        
        // Then: Second update should be rejected due to circular dependency
        assertFalse(Model.updateFood("Food B", updatedB), "Circular update should be rejected")
    }

    @Test
    fun `getFoodMacros handles circular dependency gracefully`() {
        // Given: Force a circular dependency by directly manipulating the data
        // This tests the getFoodMacros safety net in case validation fails
        val basicFood = TestUtils.createBasicFood("Safe Food", proteins = 10.0f)
        val foodA = Food("Circular A", FoodCategory.DAIRY, components = mapOf("Circular B" to 100.0f))
        val foodB = Food("Circular B", FoodCategory.DAIRY, components = mapOf("Circular A" to 100.0f))
        
        assertTrue(Model.insertFood(basicFood))
        // Use low-level access to create circular dependency (bypassing validation)
        Model::class.java.getDeclaredField("_foods").apply {
            isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val foodsList = get(Model) as MutableList<Food>
            foodsList.add(foodA)
            foodsList.add(foodB)
        }

        // When: Try to get macros for circular food
        val macros = Model.getFoodMacros("Circular A")

        // Then: Should return zero macros without crashing
        assertNotNull(macros, "Should return macros object, not null")
        assertEquals(0.0f, macros.proteins, "Should return zero for circular dependency")
        assertEquals(0.0f, macros.carbs, "Should return zero for circular dependency")
        assertEquals(0.0f, macros.fats, "Should return zero for circular dependency")
    }

    // ============= CATEGORY 2: END-TO-END MEAL & MENU OPERATIONS =============

    @Test
    fun `complete workflow - foods to meals to multi-day menus`() {
        // Given: Create basic and compound foods
        val protein = TestUtils.createBasicFood(
            name = "Chicken", 
            proteins = 25.0f, 
            carbs = 0.0f, 
            fats = 5.0f, 
            water = 70.0f
        )
        val carbs = TestUtils.createBasicFood(
            name = "Rice", 
            proteins = 5.0f, 
            carbs = 25.0f, 
            fats = 2.0f, 
            water = 68.0f
        )
        val compound = Food(
            name = "Chicken Rice",
            category = FoodCategory.MEAT_FISH,
            components = mapOf("Chicken" to 60.0f, "Rice" to 40.0f)
        )

        assertTrue(Model.insertFood(protein))
        assertTrue(Model.insertFood(carbs))
        assertTrue(Model.insertFood(compound))

        // When: Create meals using these foods
        assertTrue(Model.insertMeal("Breakfast", listOf(SizedFood("Rice", 100.0f))))
        assertTrue(Model.insertMeal("Lunch", listOf(SizedFood("Chicken Rice", 200.0f))))
        assertTrue(Model.insertMeal("Dinner", listOf(
            SizedFood("Chicken", 150.0f),
            SizedFood("Rice", 100.0f)
        )))

        assertEquals(3, Model.meals.size)

        // Create multi-day menu
        assertTrue(Model.insertMultiDayMenu("Weekly Plan", 7))
        assertEquals(1, Model.multiDayMenus.size)

        val menu = Model.multiDayMenus.first()
        assertEquals(7, menu.dailyMenus.size)

        // Assign meals to first day
        val firstDayId = menu.dailyMenus.first().id
        val breakfastId = Model.meals.find { it.description == "Breakfast" }!!.id
        val lunchId = Model.meals.find { it.description == "Lunch" }!!.id
        val dinnerId = Model.meals.find { it.description == "Dinner" }!!.id

        assertTrue(Model.updateDailyMenuMeal(firstDayId, "breakfast", breakfastId))
        assertTrue(Model.updateDailyMenuMeal(firstDayId, "lunch", lunchId))
        assertTrue(Model.updateDailyMenuMeal(firstDayId, "dinner", dinnerId))

        // Then: Verify usage counts are correct
        assertEquals(2, Model.getFoodByName("Chicken")!!.usageCount) // Used in compound + dinner
        assertEquals(3, Model.getFoodByName("Rice")!!.usageCount) // Used in compound + breakfast + dinner  
        assertEquals(1, Model.getFoodByName("Chicken Rice")!!.usageCount) // Used in lunch

        // Verify we can't delete used foods
        assertFalse(Model.deleteFood("Chicken"), "Should not delete food used in meals")
        assertFalse(Model.deleteFood("Rice"), "Should not delete food used in meals")
        
        // Verify we can't delete used meals
        assertFalse(Model.deleteMeal(lunchId), "Should not delete meal used in menu")
    }

    @Test
    fun `referential integrity prevents deletion of used components`() {
        // Given: Food hierarchy with usage
        val base = TestUtils.createBasicFood("Base")
        val compound = Food("Compound", FoodCategory.DAIRY, components = mapOf("Base" to 100.0f))
        val meal = Meal("meal-id", "Test Meal", listOf(SizedFood("Compound", 100.0f)))

        assertTrue(Model.insertFood(base))
        assertTrue(Model.insertFood(compound))
        
        // Manually add meal to test referential integrity
        Model::class.java.getDeclaredField("_meals").apply {
            isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val mealsList = get(Model) as MutableList<Meal>
            mealsList.add(meal)
        }

        // When/Then: Try to delete foods that are in use
        assertFalse(Model.deleteFood("Base"), "Can't delete base food used in compound")
        assertFalse(Model.deleteFood("Compound"), "Can't delete compound food used in meal")
        
        // Remove meal dependency first
        Model::class.java.getDeclaredField("_meals").apply {
            isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val mealsList = get(Model) as MutableList<Meal>
            mealsList.clear()
        }
        
        // Now compound can be deleted, but base still can't
        assertFalse(Model.deleteFood("Base"), "Base still used in compound")
        assertTrue(Model.deleteFood("Compound"), "Compound can be deleted")
        assertTrue(Model.deleteFood("Base"), "Base can now be deleted")
    }

    @Test
    fun `usage count accuracy with complex meal operations`() {
        // Given: Foods used in multiple meals
        val sharedFood = TestUtils.createBasicFood("Shared Ingredient")
        val uniqueFood = TestUtils.createBasicFood("Unique Ingredient")
        
        assertTrue(Model.insertFood(sharedFood))
        assertTrue(Model.insertFood(uniqueFood))

        // When: Create multiple meals using shared food
        assertTrue(Model.insertMeal("Meal 1", listOf(SizedFood("Shared Ingredient", 100.0f))))
        assertTrue(Model.insertMeal("Meal 2", listOf(
            SizedFood("Shared Ingredient", 150.0f),
            SizedFood("Unique Ingredient", 50.0f)
        )))
        assertTrue(Model.insertMeal("Meal 3", listOf(SizedFood("Shared Ingredient", 200.0f))))

        // Then: Usage counts should be correct
        assertEquals(3, Model.getFoodByName("Shared Ingredient")!!.usageCount)
        assertEquals(1, Model.getFoodByName("Unique Ingredient")!!.usageCount)

        // When: Delete one meal
        val meal2Id = Model.meals.find { it.description == "Meal 2" }!!.id
        assertTrue(Model.deleteMeal(meal2Id))

        // Then: Usage counts should be updated
        assertEquals(2, Model.getFoodByName("Shared Ingredient")!!.usageCount)
        assertEquals(0, Model.getFoodByName("Unique Ingredient")!!.usageCount)

        // When: Update remaining meal to remove shared ingredient
        val meal1Id = Model.meals.find { it.description == "Meal 1" }!!.id
        assertTrue(Model.updateMeal(meal1Id, "Updated Meal 1", listOf(SizedFood("Unique Ingredient", 75.0f))))

        // Then: Usage counts should reflect changes
        assertEquals(1, Model.getFoodByName("Shared Ingredient")!!.usageCount) // Only in Meal 3
        assertEquals(1, Model.getFoodByName("Unique Ingredient")!!.usageCount) // Now in updated Meal 1
    }

    @Test
    fun `multi-day menu operations maintain consistency`() {
        // Given: Meals for menu assignment
        val breakfast = Meal("breakfast-id", "Breakfast", listOf(SizedFood("Food A", 100.0f)))
        val lunch = Meal("lunch-id", "Lunch", listOf(SizedFood("Food B", 150.0f)))
        
        val foodA = TestUtils.createBasicFood("Food A")
        val foodB = TestUtils.createBasicFood("Food B")
        
        assertTrue(Model.insertFood(foodA))
        assertTrue(Model.insertFood(foodB))
        
        // Manually add meals
        Model::class.java.getDeclaredField("_meals").apply {
            isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val mealsList = get(Model) as MutableList<Meal>
            mealsList.addAll(listOf(breakfast, lunch))
        }

        // Create multi-day menu
        assertTrue(Model.insertMultiDayMenu("Test Plan", 3))
        val menu = Model.multiDayMenus.first()
        
        // When: Assign meals to different days and slots
        val day1Id = menu.dailyMenus[0].id
        val day2Id = menu.dailyMenus[1].id
        
        assertTrue(Model.updateDailyMenuMeal(day1Id, "breakfast", "breakfast-id"))
        assertTrue(Model.updateDailyMenuMeal(day1Id, "lunch", "lunch-id"))
        assertTrue(Model.updateDailyMenuMeal(day2Id, "breakfast", "breakfast-id")) // Same meal, different day
        
        // Then: Verify assignments are correct
        val updatedMenu = Model.multiDayMenus.first()
        assertEquals("breakfast-id", updatedMenu.dailyMenus[0].breakfastId)
        assertEquals("lunch-id", updatedMenu.dailyMenus[0].lunchId)
        assertEquals("breakfast-id", updatedMenu.dailyMenus[1].breakfastId)
        assertEquals("", updatedMenu.dailyMenus[1].lunchId) // Not assigned
        assertEquals("", updatedMenu.dailyMenus[2].breakfastId) // Day 3 not assigned
        
        // When: Try to delete menu
        assertTrue(Model.deleteMultiDayMenu(menu.id))
        assertEquals(0, Model.multiDayMenus.size)
    }
}