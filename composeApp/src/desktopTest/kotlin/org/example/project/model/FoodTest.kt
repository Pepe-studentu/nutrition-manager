package org.example.project.model

import org.example.project.TestUtils
import kotlin.test.*

class FoodTest {

    @Test
    fun `create basic food with valid data`() {
        val food = Food(
            name = "Chicken Breast",
            category = FoodCategory.MEAT_FISH,
            proteins = 31.0f,
            carbs = 0.0f,
            fats = 3.6f,
            waterMassPercentage = 65.0f
        )
        
        assertEquals("Chicken Breast", food.name)
        assertEquals(FoodCategory.MEAT_FISH, food.category)
        assertEquals(31.0f, food.proteins)
        assertEquals(0.0f, food.carbs)
        assertEquals(3.6f, food.fats)
        assertEquals(65.0f, food.waterMassPercentage)
        assertTrue(food.isBasicFood)
        assertFalse(food.isCompoundFood)
    }

    @Test
    fun `create compound food with components`() {
        val components = mapOf("Rice" to 60.0f, "Beans" to 40.0f)
        val food = Food(
            name = "Rice and Beans",
            category = FoodCategory.CEREALS_LEGUMES,
            components = components
        )
        
        assertEquals("Rice and Beans", food.name)
        assertEquals(components, food.components)
        assertFalse(food.isBasicFood)
        assertTrue(food.isCompoundFood)
        assertNull(food.proteins) // Compound foods don't have direct macros
    }

    @Test
    fun `basic food validation passes with valid macros`() {
        val validFood = TestUtils.createValidMacroFood(
            proteins = 20.0f, carbs = 30.0f, fats = 10.0f, water = 35.0f
        )
        
        assertTrue(validFood.validate())
    }

    @Test
    fun `basic food validation fails with macros exceeding 100%`() {
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
    fun `basic food validation fails with negative macros`() {
        val invalidFood = Food(
            name = "Negative Food",
            category = FoodCategory.DAIRY,
            proteins = -5.0f,
            carbs = 30.0f,
            fats = 10.0f,
            waterMassPercentage = 35.0f
        )
        
        assertFalse(invalidFood.validate())
    }

    @Test
    fun `basic food validation fails with missing macros`() {
        val incompleteFood = Food(
            name = "Incomplete Food",
            category = FoodCategory.DAIRY,
            proteins = 20.0f,
            // Missing other macros
        )
        
        assertFalse(incompleteFood.validate())
    }

    @Test
    fun `compound food validation passes with positive components`() {
        val validCompound = Food(
            name = "Mixed Salad",
            category = FoodCategory.VEGGIES,
            components = mapOf("Lettuce" to 50.0f, "Tomato" to 30.0f, "Carrot" to 20.0f)
        )
        
        assertTrue(validCompound.validate())
    }

    @Test
    fun `compound food validation fails with zero or negative components`() {
        val invalidCompound = Food(
            name = "Invalid Mix",
            category = FoodCategory.VEGGIES,
            components = mapOf("Lettuce" to 50.0f, "Tomato" to 0.0f, "Carrot" to -10.0f)
        )
        
        assertFalse(invalidCompound.validate())
    }

    @Test
    fun `food validation fails with blank name`() {
        val blankNameFood = Food(
            name = "",
            category = FoodCategory.DAIRY,
            proteins = 20.0f,
            carbs = 30.0f,
            fats = 10.0f,
            waterMassPercentage = 35.0f
        )
        
        assertFalse(blankNameFood.validate())
    }

    @Test
    fun `food validation fails with negative usage count`() {
        val negativeUsageFood = Food(
            name = "Test Food",
            category = FoodCategory.DAIRY,
            proteins = 20.0f,
            carbs = 30.0f,
            fats = 10.0f,
            waterMassPercentage = 35.0f,
            usageCount = -1
        )
        
        assertFalse(negativeUsageFood.validate())
    }

    @Test
    fun `food can have multiple categories`() {
        val food = Food(
            name = "Nutritional Yeast",
            categories = setOf(FoodCategory.NUTS, FoodCategory.CEREALS_LEGUMES),
            proteins = 45.0f,
            carbs = 35.0f,
            fats = 5.0f,
            waterMassPercentage = 10.0f
        )
        
        assertEquals(2, food.categories.size)
        assertTrue(food.categories.contains(FoodCategory.NUTS))
        assertTrue(food.categories.contains(FoodCategory.CEREALS_LEGUMES))
    }

    @Test
    fun `food can have tags`() {
        val food = Food(
            name = "Organic Milk",
            category = FoodCategory.DAIRY,
            tags = setOf("organic", "whole", "grass-fed"),
            proteins = 3.4f,
            carbs = 4.8f,
            fats = 3.3f,
            waterMassPercentage = 87.0f
        )
        
        assertEquals(3, food.tags.size)
        assertTrue(food.tags.contains("organic"))
        assertTrue(food.tags.contains("grass-fed"))
    }

    @Test
    fun `basic food effective categories uses categories field when available`() {
        val food = Food(
            name = "Multi-Category Food",
            category = FoodCategory.DAIRY,
            categories = setOf(FoodCategory.MEAT_FISH, FoodCategory.NUTS),
            proteins = 20.0f,
            carbs = 10.0f,
            fats = 15.0f,
            waterMassPercentage = 50.0f
        )
        
        val dummyLookup: (String) -> Food? = { null }
        val effectiveCategories = food.getEffectiveCategories(dummyLookup)
        
        assertEquals(2, effectiveCategories.size)
        assertTrue(effectiveCategories.contains(FoodCategory.MEAT_FISH))
        assertTrue(effectiveCategories.contains(FoodCategory.NUTS))
        // Should use categories field, not category field
        assertFalse(effectiveCategories.contains(FoodCategory.DAIRY))
    }

    @Test
    fun `basic food effective categories falls back to category when categories is empty`() {
        val food = Food(
            name = "Single Category Food",
            category = FoodCategory.DAIRY,
            proteins = 20.0f,
            carbs = 10.0f,
            fats = 15.0f,
            waterMassPercentage = 50.0f
        )
        
        val dummyLookup: (String) -> Food? = { null }
        val effectiveCategories = food.getEffectiveCategories(dummyLookup)
        
        assertEquals(1, effectiveCategories.size)
        assertTrue(effectiveCategories.contains(FoodCategory.DAIRY))
    }

    @Test
    fun `basic food effective tags returns direct tags`() {
        val food = Food(
            name = "Tagged Food",
            category = FoodCategory.DAIRY,
            tags = setOf("organic", "local"),
            proteins = 20.0f,
            carbs = 10.0f,
            fats = 15.0f,
            waterMassPercentage = 50.0f
        )
        
        val dummyLookup: (String) -> Food? = { null }
        val effectiveTags = food.getEffectiveTags(dummyLookup)
        
        assertEquals(2, effectiveTags.size)
        assertTrue(effectiveTags.contains("organic"))
        assertTrue(effectiveTags.contains("local"))
    }

    @Test
    fun `compound food inherits categories from components`() {
        val component1 = Food(
            name = "Component1",
            categories = setOf(FoodCategory.MEAT_FISH),
            proteins = 25.0f, carbs = 0.0f, fats = 5.0f, waterMassPercentage = 70.0f
        )
        val component2 = Food(
            name = "Component2", 
            categories = setOf(FoodCategory.VEGGIES),
            proteins = 5.0f, carbs = 10.0f, fats = 1.0f, waterMassPercentage = 84.0f
        )
        
        val compound = Food(
            name = "Meat and Veggie Mix",
            components = mapOf("Component1" to 60.0f, "Component2" to 40.0f)
        )
        
        val lookup: (String) -> Food? = { name ->
            when (name) {
                "Component1" -> component1
                "Component2" -> component2
                else -> null
            }
        }
        
        val effectiveCategories = compound.getEffectiveCategories(lookup)
        assertEquals(2, effectiveCategories.size)
        assertTrue(effectiveCategories.contains(FoodCategory.MEAT_FISH))
        assertTrue(effectiveCategories.contains(FoodCategory.VEGGIES))
    }

    @Test
    fun `compound food inherits tags from components`() {
        val component1 = Food(
            name = "Component1",
            tags = setOf("organic", "local"),
            category = FoodCategory.MEAT_FISH,
            proteins = 25.0f, carbs = 0.0f, fats = 5.0f, waterMassPercentage = 70.0f
        )
        val component2 = Food(
            name = "Component2",
            tags = setOf("fresh", "local"),
            category = FoodCategory.VEGGIES,
            proteins = 5.0f, carbs = 10.0f, fats = 1.0f, waterMassPercentage = 84.0f
        )
        
        val compound = Food(
            name = "Mixed Ingredients",
            components = mapOf("Component1" to 60.0f, "Component2" to 40.0f)
        )
        
        val lookup: (String) -> Food? = { name ->
            when (name) {
                "Component1" -> component1
                "Component2" -> component2
                else -> null
            }
        }
        
        val effectiveTags = compound.getEffectiveTags(lookup)
        assertEquals(3, effectiveTags.size) // "organic", "local", "fresh"
        assertTrue(effectiveTags.contains("organic"))
        assertTrue(effectiveTags.contains("local"))
        assertTrue(effectiveTags.contains("fresh"))
    }

    @Test
    fun `cache is cleared properly`() {
        val food = Food(
            name = "Cached Food",
            categories = setOf(FoodCategory.DAIRY),
            proteins = 20.0f, carbs = 10.0f, fats = 15.0f, waterMassPercentage = 50.0f
        )
        
        val dummyLookup: (String) -> Food? = { null }
        
        // First call populates cache
        val categories1 = food.getEffectiveCategories(dummyLookup)
        assertEquals(1, categories1.size)
        
        // Clear cache
        food.clearCache()
        
        // Second call should work correctly (recompute from cleared cache)
        val categories2 = food.getEffectiveCategories(dummyLookup)
        assertEquals(categories1, categories2)
    }

    @Test
    fun `food equality works correctly`() {
        val food1 = TestUtils.createBasicFood("Milk", FoodCategory.DAIRY)
        val food2 = TestUtils.createBasicFood("Milk", FoodCategory.DAIRY)
        val food3 = TestUtils.createBasicFood("Cheese", FoodCategory.DAIRY)
        
        assertEquals(food1, food2)
        assertNotEquals(food1, food3)
    }

    @Test
    fun `food with zero usage count is valid`() {
        val food = Food(
            name = "Zero Usage Food",
            category = FoodCategory.DAIRY,
            proteins = 20.0f,
            carbs = 30.0f,
            fats = 10.0f,
            waterMassPercentage = 35.0f,
            usageCount = 0
        )
        assertTrue(food.validate())
    }

    @Test
    fun `food can have exactly 100% total macros`() {
        val food = Food(
            name = "Perfect Food",
            category = FoodCategory.DAIRY,
            proteins = 25.0f,
            carbs = 25.0f,
            fats = 25.0f,
            waterMassPercentage = 25.0f // Total = 100%
        )
        
        assertTrue(food.validate())
    }
}