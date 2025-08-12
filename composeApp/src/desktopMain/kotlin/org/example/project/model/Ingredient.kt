package org.example.project.model
import kotlinx.serialization.Serializable

@Serializable
data class Ingredient(
    val id: String, //here we use UUID
    val name: String,
    val proteins: Float,
    val carbs: Float,
    val fats: Float,
    val waterMassPercentage: Float,
    // there will also be a category field, which I don't know how to store:
    // enum, string? I don't know yet if one ingredient can have multiple categories
    // at once.
) {
    val calories: Float
        get() = "%.1f".format(proteins * 4 + carbs * 4 + fats * 9).toFloat()
}
