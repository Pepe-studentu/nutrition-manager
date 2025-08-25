package org.example.project.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Food(
    val name: String,
    val category: FoodCategory? = null,
    val categories: Set<FoodCategory> = emptySet(),
    val components: Map<String, Float> = emptyMap(),
    val proteins: Float? = null,
    val carbs: Float? = null,
    val fats: Float? = null,
    val waterMassPercentage: Float? = null,
    val usageCount: Int = 0,
    val tags: Set<String> = emptySet()
) {
    val isBasicFood: Boolean
        get() = components.isEmpty()
    
    val isCompoundFood: Boolean
        get() = components.isNotEmpty()
    
    // Computed properties for effective categories and tags (including inheritance)
    @Transient
    private var _effectiveCategories: Set<FoodCategory>? = null
    
    @Transient
    private var _effectiveTags: Set<String>? = null
    
    fun getEffectiveCategories(getFoodByName: (String) -> Food?): Set<FoodCategory> {
        if (_effectiveCategories == null) {
            _effectiveCategories = if (isBasicFood) {
                // For basic foods, use migration logic: category -> categories
                if (categories.isNotEmpty()) categories
                else category?.let { setOf(it) } ?: emptySet()
            } else {
                // For compound foods, inherit from components
                components.keys.mapNotNull { componentName ->
                    getFoodByName(componentName)?.getEffectiveCategories(getFoodByName)
                }.flatten().toSet()
            }
        }
        return _effectiveCategories!!
    }
    
    fun getEffectiveTags(getFoodByName: (String) -> Food?): Set<String> {
        if (_effectiveTags == null) {
            _effectiveTags = if (isBasicFood) {
                tags
            } else {
                // For compound foods, inherit from components
                components.keys.mapNotNull { componentName ->
                    getFoodByName(componentName)?.getEffectiveTags(getFoodByName)
                }.flatten().toSet()
            }
        }
        return _effectiveTags!!
    }
    
    fun clearCache() {
        _effectiveCategories = null
        _effectiveTags = null
    }
    
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