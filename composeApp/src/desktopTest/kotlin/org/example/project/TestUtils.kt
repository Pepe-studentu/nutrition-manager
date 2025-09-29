package org.example.project

import org.example.project.model.*

object TestUtils {
    
    fun createTestFoodMacros(
        proteins: Float = 20.0f,
        carbs: Float = 30.0f,
        fats: Float = 10.0f,
        water: Float = 35.0f
    ): FoodMacros {
        return FoodMacros(proteins, carbs, fats, water)
    }
    
    fun createBasicFood(
        name: String = "Test Food",
        category: FoodCategory = FoodCategory.DAIRY,
        proteins: Float = 20.0f,
        carbs: Float = 30.0f,
        fats: Float = 10.0f,
        water: Float = 35.0f
    ): Food {
        return Food(
            name = name,
            category = category,
            proteins = proteins,
            carbs = carbs,
            fats = fats,
            waterMassPercentage = water
        )
    }
    
    fun createCompoundFood(
        name: String = "Test Compound Food",
        category: FoodCategory = FoodCategory.CEREALS_LEGUMES,
        components: Map<String, Float> = mapOf("Test Food" to 100.0f)
    ): Food {
        return Food(name = name, category = category, components = components)
    }
    
    fun createSizedFood(
        food: Food = createBasicFood(),
        grams: Float = 100.0f
    ): SizedFood {
        return SizedFood(foodName = food.name, grams = grams)
    }
    
    fun createMeal(
        id: String = "test-meal-id",
        description: String = "Test Meal",
        foods: List<SizedFood> = listOf(createSizedFood())
    ): Meal {
        return Meal(id = id, description = description, foods = foods)
    }
    
    fun createDailyMenu(
        id: String = "test-daily-menu-id",
        name: String = "Test Daily Menu",
        breakfastId: String = "breakfast-meal-id",
        snack1Id: String = "snack1-meal-id",
        lunchId: String = "lunch-meal-id",
        snack2Id: String = "snack2-meal-id",
        dinnerId: String = "dinner-meal-id"
    ): DailyMenu {
        return DailyMenu(
            id = id,
            name = name,
            breakfastId = breakfastId,
            snack1Id = snack1Id,
            lunchId = lunchId,
            snack2Id = snack2Id,
            dinnerId = dinnerId
        )
    }
    
    fun createMultiDayMenu(
        id: String = "test-multi-day-menu-id",
        description: String = "Test Multi-Day Menu",
        days: Int = 7,
        dailyMenus: List<DailyMenu> = listOf(createDailyMenu())
    ): MultiDayMenu {
        return MultiDayMenu(
            id = id,
            description = description,
            days = days,
            dailyMenus = dailyMenus
        )
    }
    
    // Helper to create foods with specific macro percentages that sum correctly
    fun createValidMacroFood(
        name: String = "Valid Macro Food",
        proteins: Float = 20.0f,
        carbs: Float = 40.0f, 
        fats: Float = 15.0f,
        water: Float = 25.0f
    ): Food {
        require(proteins + carbs + fats + water <= 100.0f) { 
            "Total macros must not exceed 100%" 
        }
        return Food(
            name = name,
            category = FoodCategory.DAIRY,
            proteins = proteins,
            carbs = carbs,
            fats = fats,
            waterMassPercentage = water
        )
    }
    
    // Helper to create foods with invalid macros for testing validation
    fun createInvalidMacroFood(
        name: String = "Invalid Macro Food",
        proteins: Float = 50.0f,
        carbs: Float = 50.0f,
        fats: Float = 50.0f,
        water: Float = 50.0f
    ): Food {
        return Food(
            name = name,
            category = FoodCategory.DAIRY,
            proteins = proteins,
            carbs = carbs,
            fats = fats,
            waterMassPercentage = water
        )
    }
}