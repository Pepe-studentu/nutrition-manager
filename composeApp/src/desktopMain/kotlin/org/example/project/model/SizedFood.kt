package org.example.project.model

import kotlinx.serialization.Serializable

@Serializable
data class SizedFood(
    val foodName: String,
    val grams: Float
) {
    fun validate(): Boolean {
        return foodName.isNotBlank() && grams > 0f
    }
}