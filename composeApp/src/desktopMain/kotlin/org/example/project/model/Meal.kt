package org.example.project.model

import kotlinx.serialization.Serializable


@Serializable
data class Meal (
    val id: String,
    val name: String,
    val ingredients: List<SizedIngredient>,
) {
    val proteins: Float by lazy {
        ingredients.sumOf { i ->
            i.grams.toDouble() / 100 * (Model.getIngredientById(i.ingredientId)?.proteins ?: 0f)
        }.toFloat()
    }

    val fats: Float by lazy {
        ingredients.sumOf { i ->
            i.grams.toDouble() / 100 * (Model.getIngredientById(i.ingredientId)?.fats ?: 0f)
        }.toFloat()
    }

    val carbs: Float by lazy {
        ingredients.sumOf { i ->
            i.grams.toDouble() / 100 * (Model.getIngredientById(i.ingredientId)?.carbs ?: 0f)
        }.toFloat()
    }

    val calories: Float by lazy {
        "%.1f".format(proteins * 4 + carbs * 4 + fats * 9).toFloat()
    }

    val water: Float by lazy {
        ingredients.sumOf { i ->
            i.grams.toDouble() / 100 * (Model.getIngredientById(i.ingredientId)?.waterMassPercentage ?: 0f)
        }.toFloat()
    }

}
