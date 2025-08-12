package org.example.project.model

import kotlinx.serialization.Serializable

@Serializable
enum class FoodCategory(val displayName: String) {
    DAIRY("Dairy"),
    MEAT_FISH("Meat & Fish"),
    EGGS("Eggs"),
    VEGGIES("Vegetables"),
    CEREALS_LEGUMES("Cereals & Legumes"),
    SUGARS("Sugars"),
    FATS("Fats"),
    DRINKS("Drinks")
}