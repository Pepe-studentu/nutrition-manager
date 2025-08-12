package org.example.project.model

import kotlinx.serialization.Serializable

@Serializable
data class SizedIngredient (
    val ingredientId: String,
    val grams: Float
)