package org.example.project.model

import kotlinx.serialization.Serializable

@Serializable
enum class FoodCategory(val displayName: String) {
    DAIRY("Lactate"),
    MEAT_FISH("Carne & Peste"),
    EGGS("Oua"),
    VEGGIES("Legume & Fructe"),
    CEREALS_LEGUMES("Cereale & leguminoase"),
    SUGARS("Dulciuri"),
    FATS("Grasimi"),
    DRINKS("Bauturi"),
    NUTS("Nuci")
}