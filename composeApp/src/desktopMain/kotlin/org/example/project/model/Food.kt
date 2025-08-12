package org.example.project.model

import kotlinx.serialization.Serializable

@Serializable
data class Food(
    val name: String,
    val category: FoodCategory,
    val components: Map<String, Float> = emptyMap(),
    val proteins: Float? = null,
    val carbs: Float? = null,
    val fats: Float? = null,
    val waterMassPercentage: Float? = null,
    val usageCount: Int = 0
) {
    val isBasicFood: Boolean
        get() = components.isEmpty()
    
    val isCompoundFood: Boolean
        get() = components.isNotEmpty()
    
    fun validate(): Boolean {
        if (name.isBlank()) return false
        
        if (isBasicFood) {
            val p = proteins ?: return false
            val c = carbs ?: return false
            val f = fats ?: return false
            val w = waterMassPercentage ?: return false
            
            if (p < 0 || c < 0 || f < 0 || w < 0) return false
            if (p + c + f + w > 100f) return false
        } else {
            if (components.any { it.value <= 0 }) return false
        }
        
        return usageCount >= 0
    }
}