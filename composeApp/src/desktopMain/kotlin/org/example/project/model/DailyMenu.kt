package org.example.project.model

import kotlinx.serialization.Serializable

@Serializable
data class DailyMenu(
    val id: String,
    val name: String?,
    val breakfastId: String,
    val snack1Id: String,
    val lunchId: String,
    val snack2Id: String,
    val dinnerId: String
) {
    private val allMeals: List<Meal> by lazy {
        listOfNotNull(
            Model.getMealById(breakfastId),
            Model.getMealById(snack1Id),
            Model.getMealById(lunchId),
            Model.getMealById(snack2Id),
            Model.getMealById(dinnerId)
        )
    }

    val proteins: Float by lazy {
        allMeals.sumOf { it.proteins.toDouble() }.toFloat()
    }

    val fats: Float by lazy {
        allMeals.sumOf { it.fats.toDouble() }.toFloat()
    }

    val carbs: Float by lazy {
        allMeals.sumOf { it.carbs.toDouble() }.toFloat()
    }

    val calories: Float by lazy {
        "%.1f".format(proteins * 4 + carbs * 4 + fats * 9).toFloat()
    }


}
