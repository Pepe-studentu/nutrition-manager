package org.example.project.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

object Model {
    // File storage for persistence
    private val ingredientsFile = File("ingredients.json")
    private val mealsFile = File("meals.json")
    private val menusFile = File("menus.json")

    // Reactive state for UI observation
    private val _ingredients = mutableStateListOf<Ingredient>()
    val ingredients: List<Ingredient> get() = _ingredients.toList()

    private val _meals = mutableStateListOf<Meal>()
    val meals: List<Meal> get() = _meals.toList()

    private val _menus = mutableStateListOf<DailyMenu>()
    val menus: List<DailyMenu> get() = _menus.toList()

    // Current screen state
    var currentScreen by mutableStateOf(Screen.Ingredients)

    // Helper functions to resolve references
    fun getIngredientById(id: String): Ingredient? {
        return _ingredients.find { it.id == id }
    }

    fun getMealById(id: String): Meal? {
        return _meals.find { it.id == id }
    }

    // Serialization and deserialization
    private fun parseIngredients(): List<Ingredient> {
        return if (ingredientsFile.exists()) {
            Json.decodeFromString(ingredientsFile.readText())
        } else {
            emptyList()
        }
    }

    private fun dumpIngredients(ingredients: List<Ingredient>) {
        ingredientsFile.writeText(Json.encodeToString(ingredients))
    }

    fun loadIngredients() {
        _ingredients.clear()
        _ingredients.addAll(parseIngredients())
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

    private fun parseMenus(): List<DailyMenu> {
        return if (menusFile.exists()) {
            Json.decodeFromString(menusFile.readText())
        } else {
            emptyList()
        }
    }

    private fun dumpMenus(menus: List<DailyMenu>) {
        menusFile.writeText(Json.encodeToString(menus))
    }

    fun loadMenus() {
        _menus.clear()
        _menus.addAll(parseMenus())
    }

    // CRUD for Ingredients
    fun insertIngredient(
        name: String,
        proteins: String,
        fats: String,
        carbs: String,
        water: String
    ): Boolean {
        // Validate inputs
        val p = proteins.toFloatOrNull()
        val c = carbs.toFloatOrNull()
        val f = fats.toFloatOrNull()
        val w = water.toFloatOrNull()

        if (name.isBlank() || p == null || c == null || f == null || w == null) return false
        if (p < 0 || c < 0 || f < 0 || w < 0) return false
        if (p + c + f + w > 100f) return false

        val newIngredient = Ingredient(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            proteins = p,
            carbs = c,
            fats = f,
            waterMassPercentage = w
        )

        _ingredients.add(newIngredient)
        dumpIngredients(_ingredients)
        return true
    }

    fun updateIngredient(
        id: String,
        name: String,
        proteins: String,
        carbs: String,
        fats: String,
        water: String
    ): Boolean {
        val p = proteins.toFloatOrNull()
        val c = carbs.toFloatOrNull()
        val f = fats.toFloatOrNull()
        val w = water.toFloatOrNull()

        if (name.isBlank() || p == null || c == null || f == null || w == null) return false
        if (p < 0 || c < 0 || f < 0 || w < 0) return false
        if (p + c + f + w > 100f) return false

        val index = _ingredients.indexOfFirst { it.id == id }
        if (index == -1) return false

        _ingredients[index] = Ingredient(
            id = id,
            name = name.trim(),
            proteins = p,
            carbs = c,
            fats = f,
            waterMassPercentage = w
        )
        dumpIngredients(_ingredients)
        return true
    }

    fun deleteIngredient(ingredientId: String): Boolean {
        // Check if ingredient is used in any meal
        if (_meals.any { meal -> meal.ingredients.any { it.ingredientId == ingredientId } }) {
            return false // Prevent deletion if referenced
        }
        val removed = _ingredients.removeIf { it.id == ingredientId }
        if (removed) {
            dumpIngredients(_ingredients)
        }
        return removed
    }

    // CRUD for Meals
    fun insertMeal(name: String, sizedIngredients: List<SizedIngredient>): Boolean {
        if (name.isBlank()) return false
        // Validate that all ingredient IDs exist
        if (sizedIngredients.any { it.ingredientId !in _ingredients.map { ingr -> ingr.id } }) {
            return false
        }
        if (sizedIngredients.any { it.grams <= 0f }) return false

        val newMeal = Meal(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            ingredients = sizedIngredients
        )
        _meals.add(newMeal)
        dumpMeals(_meals)
        return true
    }

    fun updateMeal(id: String, name: String, sizedIngredients: List<SizedIngredient>): Boolean {
        if (name.isBlank()) return false
        if (sizedIngredients.any { it.ingredientId !in _ingredients.map { ingr -> ingr.id } }) {
            return false
        }
        if (sizedIngredients.any { it.grams <= 0f }) return false

        val index = _meals.indexOfFirst { it.id == id }
        if (index == -1) return false

        _meals[index] = Meal(
            id = id,
            name = name.trim(),
            ingredients = sizedIngredients
        )
        dumpMeals(_meals)
        return true
    }

    fun deleteMeal(mealId: String): Boolean {
        // Check if meal is used in any menu
        if (_menus.any { menu ->
                menu.breakfastId == mealId || menu.snack1Id == mealId ||
                        menu.lunchId == mealId || menu.snack2Id == mealId || menu.dinnerId == mealId
            }) {
            return false // Prevent deletion if referenced
        }
        val removed = _meals.removeIf { it.id == mealId }
        if (removed) {
            dumpMeals(_meals)
        }
        return removed
    }

    // CRUD for Menus
    fun insertMenu(
        breakfastId: String?,
        snack1Id: String?,
        lunchId: String?,
        snack2Id: String?,
        dinnerId: String?,
        name: String?
    ): Boolean {
        // Validate that provided meal IDs exist (or are null)
        if (listOfNotNull(breakfastId, snack1Id, lunchId, snack2Id, dinnerId)
                .any { it !in _meals.map { meal -> meal.id } }
        ) {
            return false
        }

        val newMenu = DailyMenu(
            id = UUID.randomUUID().toString(),
            breakfastId = breakfastId!!,
            snack1Id = snack1Id!!,
            lunchId = lunchId!!,
            snack2Id = snack2Id!!,
            dinnerId = dinnerId!!,
            name = name
        )
        _menus.add(newMenu)
        dumpMenus(_menus)
        return true
    }

    fun updateMenu(
        id: String,
        breakfastId: String?,
        snack1Id: String?,
        lunchId: String?,
        snack2Id: String?,
        dinnerId: String?,
        name: String?
    ): Boolean {
        if (listOfNotNull(breakfastId, snack1Id, lunchId, snack2Id, dinnerId)
                .any { it !in _meals.map { meal -> meal.id } }
        ) {
            return false
        }

        val index = _menus.indexOfFirst { it.id == id }
        if (index == -1) return false

        _menus[index] = DailyMenu(
            id = id,
            breakfastId = breakfastId!!,
            snack1Id = snack1Id!!,
            lunchId = lunchId!!,
            snack2Id = snack2Id!!,
            dinnerId = dinnerId!!,
            name = name
        )
        dumpMenus(_menus)
        return true
    }

    fun deleteMenu(menuId: String): Boolean {
        val removed = _menus.removeIf { it.id == menuId }
        if (removed) {
            dumpMenus(_menus)
        }
        return removed
    }

    // Search functions
    fun filterIngredients(query: String, ingredients: List<Ingredient> = _ingredients): List<Ingredient> {
        return if (query.isBlank()) {
            ingredients
        } else {
            ingredients.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    fun filterMeals(query: String, meals: List<Meal> = _meals): List<Meal> {
        return if (query.isBlank()) {
            meals
        } else {
            meals.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    fun filterMenus(query: String, menus: List<DailyMenu> = _menus): List<DailyMenu> {
        if (query.isBlank()) return menus
        return menus.filter { menu ->
            listOfNotNull(
                getMealById(menu.breakfastId)?.name,
                getMealById(menu.snack1Id)?.name,
                getMealById(menu.lunchId)?.name,
                getMealById(menu.snack2Id)?.name,
                getMealById(menu.dinnerId)?.name
            ).any { it?.contains(query, ignoreCase = true) == true }
        }
    }
}