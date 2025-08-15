package org.example.project.model

import kotlinx.serialization.Serializable


@Serializable
data class Meal (
    val id: String,
    val description: String,
    val foods: List<SizedFood>,
) {
    val proteins: Float by lazy {
        foods.sumOf { f ->
            val foodMacros = Model.getFoodMacros(f.foodName)
            f.grams.toDouble() / 100 * (foodMacros?.proteins ?: 0f)
        }.toFloat()
    }

    val fats: Float by lazy {
        foods.sumOf { f ->
            val foodMacros = Model.getFoodMacros(f.foodName)
            f.grams.toDouble() / 100 * (foodMacros?.fats ?: 0f)
        }.toFloat()
    }

    val carbs: Float by lazy {
        foods.sumOf { f ->
            val foodMacros = Model.getFoodMacros(f.foodName)
            f.grams.toDouble() / 100 * (foodMacros?.carbs ?: 0f)
        }.toFloat()
    }

    val calories: Float by lazy {
        "%.1f".format(proteins * 4 + carbs * 4 + fats * 9).toFloat()
    }

    val water: Float by lazy {
        foods.sumOf { f ->
            val foodMacros = Model.getFoodMacros(f.foodName)
            f.grams.toDouble() / 100 * (foodMacros?.waterMassPercentage ?: 0f)
        }.toFloat()
    }
}