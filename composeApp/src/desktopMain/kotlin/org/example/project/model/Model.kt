package org.example.project.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.*
import java.io.File
import java.util.UUID

object Model {
    // File storage for persistence - can be redirected for testing
    private var foodsFile = File("foods.json")
    private var mealsFile = File("meals.json")
    private var multiDayMenusFile = File("multi_day_menus.json")

    // Reactive state for UI observation
    private val _foods = mutableStateListOf<Food>()
    val foods: List<Food> get() = _foods.toList()

    private val _meals = mutableStateListOf<Meal>()
    val meals: List<Meal> get() = _meals.toList()

    private val _multiDayMenus = mutableStateListOf<MultiDayMenu>()
    val multiDayMenus: List<MultiDayMenu> get() = _multiDayMenus.toList()

    // Current screen state
    var currentScreen by mutableStateOf(Screen.Foods)

    // Cache for macro calculations to avoid repeated recursive computation
    private val macroCache = mutableMapOf<String, FoodMacros>()

    // Helper functions to resolve references
    fun getFoodByName(name: String): Food? {
        return _foods.find { it.name == name }
    }

    fun getMealById(id: String): Meal? {
        return _meals.find { it.id == id }
    }

    fun getMultiDayMenuById(id: String): MultiDayMenu? {
        return _multiDayMenus.find { it.id == id }
    }

    // Recursive macro calculation with caching
    fun getFoodMacros(foodName: String): FoodMacros? {
        if (macroCache.containsKey(foodName)) {
            return macroCache[foodName]
        }

        val food = getFoodByName(foodName) ?: return null
        
        val macros = if (food.isBasicFood) {
            FoodMacros(
                proteins = food.proteins ?: 0f,
                carbs = food.carbs ?: 0f,
                fats = food.fats ?: 0f,
                waterMassPercentage = food.waterMassPercentage ?: 0f
            )
        } else {
            // Recursively calculate macros for compound foods
            var totalProteins = 0f
            var totalCarbs = 0f
            var totalFats = 0f
            var totalWater = 0f

            for ((componentName, percentage) in food.components) {
                val componentMacros = getFoodMacros(componentName)
                if (componentMacros != null) {
                    val factor = percentage / 100f
                    totalProteins += componentMacros.proteins * factor
                    totalCarbs += componentMacros.carbs * factor
                    totalFats += componentMacros.fats * factor
                    totalWater += componentMacros.waterMassPercentage * factor
                }
            }

            FoodMacros(totalProteins, totalCarbs, totalFats, totalWater)
        }

        macroCache[foodName] = macros
        return macros
    }

    // Clear macro cache when foods are modified
    private fun clearMacroCache() {
        macroCache.clear()
        // Also clear food category/tag caches
        _foods.forEach { it.clearCache() }
    }

    // Test configuration methods - only for testing
    fun setTestFilePaths(testFoodsFile: File, testMealsFile: File, testMultiDayMenusFile: File) {
        foodsFile = testFoodsFile
        mealsFile = testMealsFile
        multiDayMenusFile = testMultiDayMenusFile
    }
    
    fun clearAllData() {
        _foods.clear()
        _meals.clear()
        _multiDayMenus.clear()
        clearMacroCache()
    }
    
    fun loadFromFiles() {
        loadFoods()
        loadMeals() 
        loadMultiDayMenus()
    }

    // Persistence functions
    private fun parseFoods(): List<Food> {
        return if (foodsFile.exists()) {
            Json.decodeFromString(foodsFile.readText())
        } else {
            emptyList()
        }
    }

    private fun dumpFoods(foods: List<Food>) {
        foodsFile.writeText(Json.encodeToString(foods))
    }

    fun loadFoods() {
        _foods.clear()
        val parsedFoods = parseFoods()
        // Apply migration: convert old category field to categories set
        val migratedFoods = parsedFoods.map { food ->
            if (food.categories.isEmpty() && food.category != null) {
                food.copy(categories = setOf(food.category))
            } else {
                food
            }
        }
        _foods.addAll(migratedFoods)
        clearMacroCache()
        
        // Clear cache for all foods after loading
        _foods.forEach { it.clearCache() }
    }

    private fun parseMeals(): List<Meal> {
        return if (mealsFile.exists()) {
            Json.decodeFromString(mealsFile.readText())
        } else {
            emptyList()
        }
    }

    private fun dumpMeals(meals: List<Meal>) {
        mealsFile.writeText(Json.encodeToString(meals))
    }

    fun loadMeals() {
        _meals.clear()
        _meals.addAll(parseMeals())
    }

    private fun parseMultiDayMenus(): List<MultiDayMenu> {
        return if (multiDayMenusFile.exists()) {
            Json.decodeFromString(multiDayMenusFile.readText())
        } else {
            emptyList()
        }
    }

    private fun dumpMultiDayMenus(menus: List<MultiDayMenu>) {
        multiDayMenusFile.writeText(Json.encodeToString(menus))
    }

    fun loadMultiDayMenus() {
        _multiDayMenus.clear()
        _multiDayMenus.addAll(parseMultiDayMenus())
    }

    // Food CRUD operations
    fun insertFood(food: Food): Boolean {
        if (!food.validate()) return false
        if (_foods.any { it.name == food.name }) return false // Name must be unique

        // Validate component references for compound foods
        if (food.isCompoundFood) {
            if (food.components.keys.any { componentName -> getFoodByName(componentName) == null }) {
                return false // Referenced food doesn't exist
            }
        }

        _foods.add(food)
        dumpFoods(_foods)
        clearMacroCache()
        return true
    }

    fun updateFood(originalName: String, updatedFood: Food): Boolean {
        if (!updatedFood.validate()) return false
        
        val index = _foods.indexOfFirst { it.name == originalName }
        if (index == -1) return false

        // If changing name, check uniqueness
        if (originalName != updatedFood.name && _foods.any { it.name == updatedFood.name }) {
            return false
        }

        // Validate component references for compound foods
        if (updatedFood.isCompoundFood) {
            if (updatedFood.components.keys.any { componentName -> 
                componentName != updatedFood.name && getFoodByName(componentName) == null 
            }) {
                return false
            }
        }

        _foods[index] = updatedFood
        dumpFoods(_foods)
        clearMacroCache()
        return true
    }

    fun deleteFood(foodName: String): Boolean {
        // Check if food is used in other compound foods
        if (_foods.any { food -> food.isCompoundFood && food.components.containsKey(foodName) }) {
            return false
        }

        // Check if food is used in any meal
        if (_meals.any { meal -> meal.foods.any { it.foodName == foodName } }) {
            return false
        }

        // Decrement usage count for all components
        val foodToDelete = getFoodByName(foodName)
        if (foodToDelete?.isCompoundFood == true) {
            foodToDelete.components.keys.forEach { componentName ->
                decrementFoodUsage(componentName)
            }
        }

        val removed = _foods.removeIf { it.name == foodName }
        if (removed) {
            dumpFoods(_foods)
            clearMacroCache()
        }
        return removed
    }

    // Usage counting functions
    private fun incrementFoodUsage(foodName: String) {
        val index = _foods.indexOfFirst { it.name == foodName }
        if (index != -1) {
            _foods[index] = _foods[index].copy(usageCount = _foods[index].usageCount + 1)
        }
    }

    private fun decrementFoodUsage(foodName: String) {
        val index = _foods.indexOfFirst { it.name == foodName }
        if (index != -1 && _foods[index].usageCount > 0) {
            _foods[index] = _foods[index].copy(usageCount = _foods[index].usageCount - 1)
        }
    }

    // Meal CRUD operations
    fun insertMeal(name: String, sizedFoods: List<SizedFood>): Boolean {
        if (name.isBlank()) return false
        if (sizedFoods.any { !it.validate() }) return false
        if (sizedFoods.any { getFoodByName(it.foodName) == null }) return false

        val newMeal = Meal(
            id = UUID.randomUUID().toString(),
            description = name.trim(),
            foods = sizedFoods
        )

        // Increment usage count for all foods used in this meal
        sizedFoods.forEach { sizedFood ->
            incrementFoodUsageInMenus(sizedFood.foodName)
        }

        _meals.add(newMeal)
        dumpMeals(_meals)
        dumpFoods(_foods)
        return true
    }

    fun updateMeal(id: String, name: String, sizedFoods: List<SizedFood>): Boolean {
        if (name.isBlank()) return false
        if (sizedFoods.any { !it.validate() }) return false
        if (sizedFoods.any { getFoodByName(it.foodName) == null }) return false

        val index = _meals.indexOfFirst { it.id == id }
        if (index == -1) return false

        val oldMeal = _meals[index]
        
        // Update usage counts
        oldMeal.foods.forEach { sizedFood ->
            decrementFoodUsageInMenus(sizedFood.foodName)
        }
        sizedFoods.forEach { sizedFood ->
            incrementFoodUsageInMenus(sizedFood.foodName)
        }

        _meals[index] = Meal(
            id = id,
            description = name.trim(),
            foods = sizedFoods
        )
        dumpMeals(_meals)
        dumpFoods(_foods)
        return true
    }

    fun deleteMeal(mealId: String): Boolean {
        // Check if meal is used in any multi-day menu
        if (_multiDayMenus.any { multiDayMenu ->
            multiDayMenu.dailyMenus.any { dailyMenu ->
                dailyMenu.breakfastId == mealId || dailyMenu.snack1Id == mealId ||
                        dailyMenu.lunchId == mealId || dailyMenu.snack2Id == mealId || 
                        dailyMenu.dinnerId == mealId
            }
        }) {
            return false
        }

        val mealToDelete = _meals.find { it.id == mealId }
        if (mealToDelete != null) {
            // Decrement usage count for all foods in this meal
            mealToDelete.foods.forEach { sizedFood ->
                decrementFoodUsageInMenus(sizedFood.foodName)
            }
        }

        val removed = _meals.removeIf { it.id == mealId }
        if (removed) {
            dumpMeals(_meals)
            dumpFoods(_foods)
        }
        return removed
    }

    // Helper functions for tracking food usage in menus
    private fun incrementFoodUsageInMenus(foodName: String) {
        incrementFoodUsage(foodName)
        // Also increment for compound food components
        val food = getFoodByName(foodName)
        if (food?.isCompoundFood == true) {
            food.components.keys.forEach { componentName ->
                incrementFoodUsageInMenus(componentName)
            }
        }
    }

    private fun decrementFoodUsageInMenus(foodName: String) {
        decrementFoodUsage(foodName)
        // Also decrement for compound food components
        val food = getFoodByName(foodName)
        if (food?.isCompoundFood == true) {
            food.components.keys.forEach { componentName ->
                decrementFoodUsageInMenus(componentName)
            }
        }
    }

    // Multi-day menu CRUD operations
    fun insertMultiDayMenu(description: String, days: Int): Boolean {
        if (description.isBlank() || days <= 0) return false

        val emptyDailyMenus = (1..days).map { dayIndex ->
            DailyMenu(
                id = UUID.randomUUID().toString(),
                name = "Day $dayIndex",
                breakfastId = "",
                snack1Id = "",
                lunchId = "",
                snack2Id = "",
                dinnerId = ""
            )
        }

        val newMultiDayMenu = MultiDayMenu(
            description = description.trim(),
            days = days,
            dailyMenus = emptyDailyMenus
        )

        _multiDayMenus.add(newMultiDayMenu)
        dumpMultiDayMenus(_multiDayMenus)
        return true
    }

    fun deleteMultiDayMenu(menuId: String): Boolean {
        val removed = _multiDayMenus.removeIf { it.id == menuId }
        if (removed) {
            dumpMultiDayMenus(_multiDayMenus)
        }
        return removed
    }

    fun updateDailyMenuMeal(dailyMenuId: String, mealSlot: String, mealId: String): Boolean {
        val multiDayMenuIndex = _multiDayMenus.indexOfFirst { multiDayMenu ->
            multiDayMenu.dailyMenus.any { it.id == dailyMenuId }
        }
        if (multiDayMenuIndex == -1) return false

        val multiDayMenu = _multiDayMenus[multiDayMenuIndex]
        val dailyMenuIndex = multiDayMenu.dailyMenus.indexOfFirst { it.id == dailyMenuId }
        if (dailyMenuIndex == -1) return false

        val dailyMenu = multiDayMenu.dailyMenus[dailyMenuIndex]
        val updatedDailyMenu = when (mealSlot) {
            "breakfast" -> dailyMenu.copy(breakfastId = mealId)
            "snack1" -> dailyMenu.copy(snack1Id = mealId)
            "lunch" -> dailyMenu.copy(lunchId = mealId)
            "snack2" -> dailyMenu.copy(snack2Id = mealId)
            "dinner" -> dailyMenu.copy(dinnerId = mealId)
            else -> return false
        }

        val updatedDailyMenus = multiDayMenu.dailyMenus.toMutableList()
        updatedDailyMenus[dailyMenuIndex] = updatedDailyMenu

        _multiDayMenus[multiDayMenuIndex] = multiDayMenu.copy(dailyMenus = updatedDailyMenus)
        dumpMultiDayMenus(_multiDayMenus)
        return true
    }

    // Search functions with relevance scoring
    fun filterFoods(query: String, foods: List<Food> = _foods): List<Food> {
        return if (query.isBlank()) {
            // Return all foods without any sorting (let caller decide sorting)
            foods
        } else {
            // Calculate relevance scores and sort by relevance
            foods.mapNotNull { food ->
                val score = calculateRelevanceScore(food, query)
                if (score > 0) food to score else null
            }.sortedByDescending { it.second }
             .map { it.first }
        }
    }

    private fun calculateRelevanceScore(food: Food, query: String): Int {
        var score = 0
        val lowerQuery = query.lowercase()
        val lowerFoodName = food.name.lowercase()
        
        // Exact name match gets highest priority
        // claude: the first 2 cases should increase the results count
        if (lowerFoodName == lowerQuery) {
            score += 100
        } else if (lowerFoodName.startsWith(lowerQuery)) {
            score += 50
        } else if (lowerFoodName.contains(lowerQuery)) {
            score += 20
        }
        
        // Search in effective tags (includes inherited tags for compound foods)
        food.getEffectiveTags(::getFoodByName).forEach { tag ->
            val lowerTag = tag.lowercase()
            if (lowerTag == lowerQuery) {
                score += 30
            } else if (lowerTag.contains(lowerQuery)) {
                score += 10
            }
        }
        
        // Search in effective categories (includes inherited categories for compound foods)
        food.getEffectiveCategories(::getFoodByName).forEach { category ->
            val lowerCategory = category.displayName.lowercase()
            if (lowerCategory == lowerQuery) {
                score += 25
            } else if (lowerCategory.contains(lowerQuery)) {
                score += 8
            }
        }
        
        // Search in component food names (for compound foods)
        food.components.keys.forEach { componentName ->
            val lowerComponent = componentName.lowercase()
            if (lowerComponent == lowerQuery) {
                score += 15
            } else if (lowerComponent.contains(lowerQuery)) {
                score += 5
            }
        }

        return score
    }

    fun filterMeals(query: String, meals: List<Meal> = _meals): List<Meal> {
        return if (query.isBlank()) {
            meals
        } else {
            meals.filter { it.description.contains(query, ignoreCase = true) }
        }
    }

    fun filterMultiDayMenus(query: String, menus: List<MultiDayMenu> = _multiDayMenus): List<MultiDayMenu> {
        return if (query.isBlank()) {
            menus
        } else {
            menus.filter { it.description.contains(query, ignoreCase = true) }
        }
    }

    // Synchronous sorting functions
    fun sortFoods(
        foods: List<Food> = _foods,
        column: String? = null,
        ascending: Boolean = true
    ): List<Food> {
        // Pre-calculate all macros once to avoid repeated calculations during sorting
        val macrosMap = foods.associateWith { getFoodMacros(it.name) }
        
        val comparator: Comparator<Food> = when (column) {
            "food" -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            "protein" -> compareBy { macrosMap[it]?.proteins ?: 0f }
            "fat" -> compareBy { macrosMap[it]?.fats ?: 0f }
            "carbs" -> compareBy { macrosMap[it]?.carbs ?: 0f }
            "calories" -> compareBy { food ->
                macrosMap[food]?.let { it.proteins * 4 + it.carbs * 4 + it.fats * 9 } ?: 0f
            }
            null -> compareByDescending { it.usageCount } // NONE state = usage sort
            else -> compareByDescending { it.usageCount } // Fallback to usage sort
        }
        
        return if (column == null) {
            // Usage sort ignores ascending flag - always descending by usage
            foods.sortedWith(comparator)
        } else if (ascending) {
            foods.sortedWith(comparator)
        } else {
            foods.sortedWith(comparator.reversed())
        }
    }
}